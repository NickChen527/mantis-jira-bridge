package com.nick.mantis_jira_bridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MantisJiraBridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MantisJiraBridgeApplication.class, args);
	}

}
