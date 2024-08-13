package com.example.demo.hello;

public interface HelloService {
    default String sayHello(String prefix, String name) {
        return prefix + ", " + name + "!";
    };

    default String sayHello(String name) {
        return "Hello, " + name + "!";
    };
}
