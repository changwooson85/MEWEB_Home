package com.MESWebServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MesWebServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MesWebServerApplication.class, args);
	}

}
