package com.example.demo.hello;

public class SimpleHelloService implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello, " + name + "!";
    }
}
