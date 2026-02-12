package com.ltp.sudomaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.ltp.sudomaster.repository")
@ComponentScan(basePackages = "com.ltp.sudomaster")
@EnableScheduling
public class SudomasterApplication {

	public static void main(String[] args) {
		SpringApplication.run(SudomasterApplication.class, args);
	}

}
