package com.xiaowang.cola.tools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.xiaowang.cola.tools")
public class ColaToolsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ColaToolsApplication.class, args);
    }

}
