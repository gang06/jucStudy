package com.example.demo.juc.future;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author ligegang
 * @title: FutureTaskForMultiCompute
 * @projectName jucStudy
 * @description: TODO
 * @date 2019/12/23 11:04
 */
public class FutureTaskForMultiCompute {

    public static void main(String[] args) {
        // 创建任务集合
        List<FutureTask<Integer>> taskList = new ArrayList<FutureTask<Integer>>();

        // 创建线程池
        ExecutorService exec = Executors.newFixedThreadPool(5);
        for(int i =0;i<10; i++){
            FutureTask<Integer> ft = new FutureTask<>(new ComputeTask(i,i + ""));
            taskList.add(ft);
            exec.submit(ft);
        }

        System.out.println("所有计算任务提交完毕, 主线程接着干其他事情！");

        //统计任务结果
        Long countStartTime = System.currentTimeMillis();
        // 开始统计各计算线程计算结果
        Integer totalResult = 0;
        for (FutureTask<Integer> ft : taskList) {
            try {
                //FutureTask的get方法会自动阻塞,直到获取计算结果为止
                totalResult = totalResult + ft.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        Long countEndTime = System.currentTimeMillis();
        Long spendTime = countEndTime - countStartTime;
        System.out.println("统计花费时长: " + spendTime);
        // 关闭线程池
        exec.shutdown();
        System.out.println("多任务计算后的总结果是:" + totalResult);
    }



    static class ComputeTask implements Callable<Integer> {

        private Integer result = 0;
        private String taskName = "";

        public ComputeTask(Integer iniResult, String taskName){
            result = iniResult;
            this.taskName = taskName;
            System.out.println("生成子线程计算任务: "+taskName);
        }

        public String getTaskName(){
            return this.taskName;
        }

        @Override
        public Integer call() throws Exception {
            // TODO Auto-generated method stub

            for (int i = 0; i < 100; i++) {
                result += i;
            }
            // 休眠5秒钟，观察主线程行为，预期的结果是主线程会继续执行，到要取得FutureTask的结果是等待直至完成。
            Thread.sleep(2000);
            System.out.println("子线程计算任务: "+taskName+" 执行完成,结果为：" + result);
            return result;
        }
    }
}
