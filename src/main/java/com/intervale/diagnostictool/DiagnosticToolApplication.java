package com.intervale.diagnostictool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.intervale.diagnostictool",
    "com.intervale.diagnostictool.controller",
    "com.intervale.diagnostictool.config"
})
public class DiagnosticToolApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiagnosticToolApplication.class, args);
    }
}
