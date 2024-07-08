package org.murilonerdx.productms.config;

import org.murilonerdx.productms.modules.sales.client.SalesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class HttpInterfaceConfig {


	@Value("${app-config.services.sales}")
	private String baseUrl;

	@Bean
	public SalesClient salesClient() {
		return WebClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader("Authorization", "Bearer your_token") // Replace with your actual token
				.build()
				.get()
				.uri("/api/sales") // Replace with the actual endpoint
				.retrieve()
				.bodyToMono(SalesClient.class).block();
	}
}
