package com.skinforge.upgrader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class UpgraderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UpgraderServiceApplication.class, args);
	}

}
