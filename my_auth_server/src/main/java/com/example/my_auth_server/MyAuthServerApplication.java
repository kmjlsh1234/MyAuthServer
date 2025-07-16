package com.example.my_auth_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MyAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyAuthServerApplication.class, args);
    }

}
