package com.example.demo.hello;

import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceHelloService implements HelloService {
    private final Resource resource;

    public ResourceHelloService(Resource resource) {
        this.resource = resource;
    }

    @Override
    public String sayHello(String name) {
        try {
            try (InputStream in = this.resource.getInputStream()) {
                String prefix = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
                return sayHello(prefix, name);
            }
        }
        catch (IOException ex) {
            throw new IllegalStateException("Failed to read resource " + resource, ex);
        }
    }
}
