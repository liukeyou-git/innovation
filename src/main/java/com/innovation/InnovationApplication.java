package com.innovation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.innovation.mapper")
public class InnovationApplication {

	public static void main(String[] args) {
		SpringApplication.run(InnovationApplication.class, args);
	}

}
