package com.evently.evt_core_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EvtCoreServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EvtCoreServiceApplication.class, args);
	}

}
