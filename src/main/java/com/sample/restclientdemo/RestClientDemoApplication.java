package com.sample.restclientdemo;

import java.util.Map;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class RestClientDemoApplication {
	@Bean
	ApplicationRunner init() {
		return args -> {
			// https://open.er-api.com/v6/latest
			RestTemplate rt = new RestTemplate();
			Map<String, Map<String, Double>> response = rt.getForObject("https://open.er-api.com/v6/latest", Map.class);
			System.out.println(response.get("rates").get("KRW")); // 읽기만 한다면 간단하지만, 쓰거나 세팅을 하려고 하면 번거롭다.
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(RestClientDemoApplication.class, args);
	}
}
