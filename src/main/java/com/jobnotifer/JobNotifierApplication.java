package com.jobnotifer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobNotifierApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobNotifierApplication.class, args);
	}

}
