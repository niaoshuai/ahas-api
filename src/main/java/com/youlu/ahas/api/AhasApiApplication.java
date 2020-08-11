package com.youlu.ahas.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
//Access-Control-Request-Headers: content-type
//        Access-Control-Request-Method: GET
public class AhasApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AhasApiApplication.class, args);
    }

}
