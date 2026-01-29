package com.example.OPD_allocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OpdAllocationApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpdAllocationApplication.class, args);
        System.out.println("=================================================");
        System.out.println("OPD Token Allocation System Started Successfully");
        System.out.println("=================================================");
        System.out.println("Server running on: http://localhost:8080");
        System.out.println("API Documentation available at /api/docs");
        System.out.println("=================================================");
    }
}

