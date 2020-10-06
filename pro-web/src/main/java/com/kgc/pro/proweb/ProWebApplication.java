package com.kgc.pro.proweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ProWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProWebApplication.class, args);
    }

}
