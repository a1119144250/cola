package com.xiaowang.cola.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.xiaowang.cola.gateway")
public class ColaGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ColaGatewayApplication.class, args);
    }

}
