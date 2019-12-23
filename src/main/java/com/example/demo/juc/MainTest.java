package com.example.demo.juc;

/**
 * @author ligegang
 * @title: MainTest
 * @projectName jucStudy
 * @description: TODO
 * @date 2019/12/23 11:40
 */
public class MainTest {
    public static void main(String[] args) {
        int result = 5;
        for (int i = 0; i < 100; i++) {
            result =+ i;
            //result = result + i;

        }

        System.out.println(result);
    }
}
