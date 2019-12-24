package com.example.demo.juc.tools.AtomicReference;

/**
 * @author ligegang
 * @title: Person
 * @projectName jucStudy
 * @description: TODO
 * @date 2019/12/23 17:49
 */
public class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "[name: " + this.name + ", age: " + this.age + "]";
    }
}
