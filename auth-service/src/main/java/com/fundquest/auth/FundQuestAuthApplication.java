package com.fundquest.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableDiscoveryClient
public class FundQuestAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(FundQuestAuthApplication.class, args);
	}
}