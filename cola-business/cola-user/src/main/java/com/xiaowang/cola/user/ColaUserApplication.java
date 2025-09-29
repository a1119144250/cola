package com.xiaowang.cola.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.xiaowang.cola.user")
public class ColaUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(ColaUserApplication.class, args);
    }

}
