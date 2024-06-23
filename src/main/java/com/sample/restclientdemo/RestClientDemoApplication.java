package com.sample.restclientdemo;

import java.util.Map;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootApplication
public class RestClientDemoApplication {
	@Bean
	ApplicationRunner init(ErApi erApiWebClient, ErApi erApiRestClient) {
		return args -> {
			// https://open.er-api.com/v6/latest
			RestTemplate rt = new RestTemplate();
			Map<String, Map<String, Double>> response = rt.getForObject("https://open.er-api.com/v6/latest", Map.class);
			System.out.println(response.get("rates").get("KRW")); // 읽기만 한다면 간단하지만, 쓰거나 세팅을 하려고 하면 번거롭다.

			WebClient client = WebClient.create("https://open.er-api.com");
			Map<String, Map<String, Double>> response2 = client.get().uri("/v6/latest").retrieve().bodyToMono(Map.class).block();
			System.out.println(response2.get("rates").get("KRW"));

			//여기까지가 기존의 Spring. 취향 차이

			//아래는 Http Interface를 만들어서 사용하는 방법. Proxy 객체를 만들어서 사용해야 한다.
			//6.1 이전에는 WebClientAdapter를 넣어서 사용해야 했다.
			HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(client)).build();
			ErApi erApi = factory.createClient(ErApi.class);
			Map<String, Map<String, Double>> response3 = erApi.getLatest();
			System.out.println(response3.get("rates").get("KRW"));

			//6.1 이후에는 사용하는 메서드 이름이랑 방식이 조금 달라졌다.
			HttpServiceProxyFactory factory2 = HttpServiceProxyFactory.builder().exchangeAdapter(WebClientAdapter.create(client)).build();
			ErApi erApi2 = factory2.createClient(ErApi.class);
			Map<String, Map<String, Double>> response4 = erApi2.getLatest();
			System.out.println(response4.get("rates").get("KRW"));

			//HttpInterfact(WebClient) Bean으로 등록해서 사용하는 방법
			Map<String, Map<String, Double>> response5 = erApiWebClient.getLatest();
			System.out.println(response5.get("rates").get("KRW"));

			RestClient restClient = RestClient.create();
			// final String body = restClient.get().uri("https://open.er-api.com/v6/latest").retrieve().body(String.class);
			final RestClient.ResponseSpec retrieve = restClient.get().uri("https://open.er-api.com/v6/latest").retrieve();

			// ResponseEntity로 받을 수도 있다.
			final ResponseEntity<String> entity = retrieve.toEntity(String.class);
			System.out.println(entity.getStatusCode());
			System.out.println(entity.getHeaders());
			System.out.println(entity.getBody());

			// 혹은 바로 Body를 읽어서 사용할 수도 있다.
			// Map<String, Map<String, Double>> response6 = retrieve.body(Map.class);
			// System.out.println(response6.get("rates").get("KRW"));

			// JSON 변환도 가능하다.
			// Pet pet = restClient.get().uri("https://petclinic.example.com/pets/{id}", id).accept(APPLICATION_JSON).retrieve().body(Pet.class);

			// POST 요청
			// Pet pet = ...
			// ResponseEntity response = restClient.post().uri("https://petclinic.example.com/pets/new").contentType(APPLICATION_JSON).body(pet).retrieve().toBodilessEntity();

			// HttpInterface(RestClient) Bean 사용 (WebClient 대신에 RestClient를 사용)
			Map<String, Map<String, Double>> response7 = erApiRestClient.getLatest();
			System.out.println("HttpInterface(RestClient): " + response7.get("rates").get("KRW"));

		};
	}

	@Bean
	ErApi erApiWebClient() {
		WebClient client = WebClient.create("https://open.er-api.com");

		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(client)).build();

		return factory.createClient(ErApi.class);
	}

	@Bean
	ErApi erApiRestClient() {
		RestClient client = RestClient.create("https://open.er-api.com");
		// RestClient client = RestClient.create();

		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
			.exchangeAdapter(RestClientAdapter.create(client))
			.build();

		return factory.createClient(ErApi.class);
	}

	interface ErApi{
		//Client에서 BASE URL을 지정한 경우 interface에서는 뒤에 붙을 상세 주소만 지정해주면 됨
		@GetExchange("/v6/latest")
		Map getLatest();

		//Client에서 BASE URL을 지정하지 않은 경우 interface에서 전체 URL을 지정하면 에러가 나지 않음
		@GetExchange("https://open.er-api.com/v6/latest")
		Map getLatestExceptNoUrlClient();
	}

	public static void main(String[] args) {
		SpringApplication.run(RestClientDemoApplication.class, args);
	}
}
