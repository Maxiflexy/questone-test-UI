package com.fundquest.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FundQuestAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(FundQuestAuthApplication.class, args);
	}
}