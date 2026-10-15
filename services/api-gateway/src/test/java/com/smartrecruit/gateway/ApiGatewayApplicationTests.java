package com.smartrecruit.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;

@SpringBootTest(properties = {
		"identity.jwt.secret=test-secret-key-at-least-32-characters",
		"identity.jwt.issuer=smartrecruit-identity"
})
class ApiGatewayApplicationTests {
	@Autowired
	private RouteLocator routes;

	@Test
	void routesRequestsToEveryBackendService() {
		List<String> routeIds = routes.getRoutes()
				.map(route -> route.getId())
				.collectList()
				.block();

		assertThat(routeIds).containsExactlyInAnyOrder(
				"identity-service",
				"recruitment-service",
				"candidate-ai-service",
				"subscription-service",
				"interview-service",
				"notification-service",
				"analytics-service");
	}
}
