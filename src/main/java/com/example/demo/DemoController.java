package com.example.demo;

import com.example.demo.hello.HelloService;
import com.example.demo.hello.ResourceHelloService;
import com.example.demo.hello.SimpleHelloService;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Optional;

@RestController
@ImportRuntimeHints(DemoController.DemoControllerRuntimeHints.class)
public class DemoController {
    private final ObjectProvider<HelloService> helloServices;

    DemoController(ObjectProvider<HelloService> helloServices) {
        this.helloServices = helloServices;
    }

    @GetMapping("/hello")
    HelloResponse hello(@RequestParam(required = false) String mode) throws Exception {
        String message = getHelloMessage(mode, "Native");
        return new HelloResponse(message);
    }

    private String getHelloMessage(String mode, String name) throws Exception {
        if (mode == null) {
            return "No option provided";
        }
        else if (mode.equals("bean")) {
            HelloService service = this.helloServices.getIfUnique();
            return (service != null) ? service.sayHello(name) : "No bean found";
        }
        else if (mode.equals("reflection")) {
            String implementationName = Optional.ofNullable(getDefaultHelloServiceImplementation())
                    .orElse(SimpleHelloService.class.getName());
            Class<?> implementationClass = ClassUtils.forName(implementationName, getClass().getClassLoader());
            Method method = implementationClass.getMethod("sayHello", String.class);
            Object instance = BeanUtils.instantiateClass(implementationClass);
            return (String) ReflectionUtils.invokeMethod(method, instance, name);
        }
        else if (mode.equals("resource")) {
            ResourceHelloService helloService = new ResourceHelloService(new ClassPathResource("hello.txt"));
            return helloService.sayHello(name);
        }

        return "Unknown mode: " + mode;
    }

    public record HelloResponse(String message) {
    }

    protected String getDefaultHelloServiceImplementation() {
        return null;
    }

    // Sprint AOT: RuntimeHintsRegistrar
    static class DemoControllerRuntimeHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // fix reflection issue
            hints.reflection()
                    .registerConstructor(SimpleHelloService.class.getDeclaredConstructors()[0], ExecutableMode.INVOKE)
                    .registerMethod(ReflectionUtils.findMethod(SimpleHelloService.class, "sayHello", String.class), ExecutableMode.INVOKE);

            // fix resource issue
            hints.resources()
                    .registerPattern("hello.txt");
        }
    }
}
