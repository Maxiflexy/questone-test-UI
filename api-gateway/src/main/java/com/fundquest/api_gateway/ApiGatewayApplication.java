package com.fundquest.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	/**
	 * Optional: Define routes programmatically (reactive style)
	 * This is an alternative to YAML configuration
	 */
	// @Bean
	// public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
	//     return builder.routes()
	//         .route("eureka-server", r -> r.path("/eureka/**")
	//             .uri("http://localhost:8761"))
	//         .route("user-service", r -> r.path("/api/users/**")
	//             .filters(f -> f.stripPrefix(2))
	//             .uri("lb://user-service"))
	//         .build();
	// }

}
