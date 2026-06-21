package com.clearinghouse.e2e;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.binder.test.EnableTestBinder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@EnableTestBinder
@EnableWireMock(@ConfigureWireMock(
		name = "currency-api",
		baseUrlProperties = "currency-api.url"
))
@Import(IntegrationTestBase.FixedClockConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class IntegrationTestBase {

	@Autowired
	protected InputDestination input;

	@Autowired
	protected OutputDestination output;

	@TestConfiguration
	public static class FixedClockConfig {
		// Tuesday 2030-01-01 10:00 UTC — keeps Friday-cutoff rule dormant and
		// puts the trade's 2030-01-01 settlement within the T+2 window.
		@Bean
		@Primary
		Clock clock() {
			return Clock.fixed(Instant.parse("2030-01-01T10:00:00Z"), ZoneOffset.UTC);
		}
	}
}
