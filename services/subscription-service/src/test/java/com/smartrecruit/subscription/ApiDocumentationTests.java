package com.smartrecruit.subscription;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "identity.jwt.secret=local-test-secret-key-at-least-32-characters")
@AutoConfigureMockMvc
class ApiDocumentationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void openApiIsPublicAndApiRoutesRequireJwt() throws Exception {
		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("SmartRecruit Subscription Service API"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
		mockMvc.perform(get("/api/v1/protected-probe"))
				.andExpect(status().isUnauthorized());
	}
}
