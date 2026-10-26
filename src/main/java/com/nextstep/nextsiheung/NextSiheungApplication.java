package com.nextstep.nextsiheung;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class NextSiheungApplication {

	public static void main(String[] args) {
		SpringApplication.run(NextSiheungApplication.class, args);
	}

}
