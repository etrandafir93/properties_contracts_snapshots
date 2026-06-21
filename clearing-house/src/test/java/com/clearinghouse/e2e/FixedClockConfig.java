package com.clearinghouse.e2e;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
class FixedClockConfig {
	// Tuesday 2030-01-01 10:00 UTC — keeps Friday-cutoff rule dormant and
	// puts the trade's 2030-01-01 settlement within the T+2 window.
	@Bean
	@Primary
	Clock clock() {
		return Clock.fixed(Instant.parse("2030-01-01T10:00:00Z"), ZoneOffset.UTC);
	}
}
