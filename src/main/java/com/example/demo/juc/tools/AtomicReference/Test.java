package com.example.demo.juc.tools.AtomicReference;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ligegang
 * @title: Test
 * @projectName jucStudy
 * @description: TODO
 * @date 2019/12/23 17:51
 */
public class Test {
    // 普通引用
    private static Person person;

    //原子性引用
    private static AtomicReference<Person> personAtomicReference;

    public static void main(String[] args) throws InterruptedException {
        person = new Person("Tom", 18);
        personAtomicReference = new AtomicReference<>(person);

        System.out.println("Person is " + person.toString());

        Thread t1 = new Thread(new Task1());
        Thread t2 = new Thread(new Task2());

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Now Person is " + personAtomicReference.get().toString());
    }

    static class Task1 implements Runnable {
        @Override
        public void run() {
            personAtomicReference.getAndSet(new Person("Tom1",personAtomicReference.get().getAge()+1));

            System.out.println("Thread1 Values "
                    + personAtomicReference.get().toString());
        }
    }

    static class Task2 implements Runnable {
        @Override
        public void run() {
            personAtomicReference.getAndSet(new Person("Tom2",personAtomicReference.get().getAge()+2));

            System.out.println("Thread2 Values "
                    + personAtomicReference.get().toString());
        }
    }
}
