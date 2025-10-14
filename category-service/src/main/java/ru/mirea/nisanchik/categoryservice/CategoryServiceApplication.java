package ru.mirea.nisanchik.categoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class CategoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CategoryServiceApplication.class, args);
    }
}