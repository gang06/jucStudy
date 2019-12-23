package com.example.demo.juc.tools;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * @author ligegang
 * @title: SemaphoreTestCase
 * @projectName jucStudy
 * @description: TODO
 * @date 2019/12/23 12:06
 */
public class SemaphoreTestCase {

    //连接池
    abstract class Pool {

        protected Integer maxActiveConnectionNum;

        protected Integer currentActiveConnectionNum;

        protected Integer maxWaitTime;

        protected List<Connection> pools_used;

        protected BlockingQueue<Connection> pools_free;

        //构造器
        protected Pool(Integer maxActiveConnectionNum, Integer currentActiveConnectionNum, Integer maxWaitTime) {
            this.maxActiveConnectionNum = maxActiveConnectionNum;
            this.currentActiveConnectionNum = currentActiveConnectionNum;
            this.maxWaitTime = maxWaitTime;
            this.pools_free = new LinkedBlockingQueue<Connection>(maxActiveConnectionNum);
            this.pools_used = new ArrayList<Connection>(maxActiveConnectionNum);
            init();//直接创建最大的容量 本示例未做初始化  不足扩容处理
        }

        private void init() {
            for (int i = 0; i < maxActiveConnectionNum; ++i) {
                pools_free.add(new Connection());
            }
        }

        //获取连接
        public abstract Connection getConnection();

        //关闭连接
        public abstract void closeConnection(Connection connection);

    }
    //连接供体
    class Connection{
        public Connection() {System.out.println("创建了新的Connection : "+this);}
    }

    //普通连接池实现方案
    class NormalPool extends Pool{

        protected NormalPool(Integer maxActiveConnectionNum,Integer currentActiveConnectionNum, Integer maxWaitTime) {
            super(maxActiveConnectionNum, currentActiveConnectionNum, maxWaitTime);
        }

        @Override
        public Connection getConnection() {
            Connection connection = null;
            synchronized (pools_free) {
                try {
                    //case 1# init
                    connection = pools_free.poll(maxWaitTime, TimeUnit.MILLISECONDS);
                    //case 2# init
                    //for(int i=0;i<maxWaitTime;++i){
                    //  connection = pools_free.poll();
                    //  if(null!=connection) break;
                    //  wait(1);//防止过度消耗CPU
                    //}
                    //以下为case1和case2共同的部分 建议选用case1 精度级别为纳秒 case2的精度级别 毫秒
                    //long waitTime = maxWaitTime - (System.currentTimeMillis()-beginTime);
                    //wait(waitTime);
                    if(null==connection) {
                        throw new RuntimeException("Connection timepit with "+maxWaitTime+" milliseconds");
                    } else{
                        pools_used.add(connection);
                        System.out.println(Thread.currentThread().getName()+"获取连接"+connection);
                        return connection;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void closeConnection(Connection connection){

            synchronized (pools_used) {
                if(pools_used.remove(connection)){
                    pools_free.add(connection);
                }
                System.out.println(Thread.currentThread().getName()+"释放连接"+connection);
                connection = null;
            }
        }

    }



    //基于信号量的连接池
    class SemaphorePool extends Pool{

        private Semaphore semaphore;

        protected SemaphorePool(Integer maxActiveConnectionNum,Integer currentActiveConnectionNum, Integer maxWaitTime) {
            super(maxActiveConnectionNum, currentActiveConnectionNum, maxWaitTime);
            semaphore = new Semaphore(maxActiveConnectionNum, true);
        }

        @Override
        public Connection getConnection() {
            Connection connection = null;
            try {
                if(semaphore.tryAcquire(maxWaitTime,TimeUnit.MILLISECONDS)){
                    synchronized (pools_free) {
                        connection = pools_free.poll();
                        if(null == connection) {
                            throw new RuntimeException("NullPointException in connection free pools");
                        }
                        pools_used.add(connection);
                        System.out.println(Thread.currentThread().getName()+"获取连接"+connection);
                        return connection;
                    }
                }else{
                    throw new RuntimeException("Connection timepit with "+maxWaitTime+" milliseconds");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void closeConnection(Connection connection) {
            synchronized (pools_used) {
                if(pools_used.remove(connection)){
                    pools_free.add(connection);
                }
                semaphore.release();
                System.out.println(Thread.currentThread().getName()+"释放连接"+connection);
                connection = null;
            }
        }
    }

    //待执行的任务
    class Task implements Runnable{

        private Random random = new Random();

        private Pool pool;

        public Task(Pool pool) {
            this.pool = pool;
        }

        @Override
        public void run() {
            try {
                Connection connection = pool.getConnection();
                Thread.sleep(random.nextInt(1000));
                pool.closeConnection(connection);
            } catch (InterruptedException e) {
            }

        }

    }

    //启动函数
    public void start(){
        int thredCount = 20;
        Pool pool = new NormalPool(10, 10, 100);
        ExecutorService service = Executors.newCachedThreadPool();
        for(int i=0;i<thredCount;++i){
            service.execute(new Task(pool));
        }
        service.shutdown();
        pool = new SemaphorePool(10, 10, 100);
        service = Executors.newCachedThreadPool();
        for(int i=0;i<thredCount;++i){
            service.execute(new Task(pool));
        }
        service.shutdown();
    }

    public static void main(String[] args) {
        new SemaphoreTestCase().start();
    }
}