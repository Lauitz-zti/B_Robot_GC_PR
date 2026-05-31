package com.brazor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestServerApp {

    public static void main(String[] args) {
        //encender el servidor web REST con Spring Boot
        SpringApplication.run(RestServerApp.class, args);
    }
}