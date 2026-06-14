package com.clearinghouse;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@TestConfiguration
class TestConfig {
	@Bean(name = "integrationConversionService")
	GenericConversionService integrationConversionService() {
		return new DefaultConversionService();
	}

	@Bean
	TaskScheduler taskScheduler() {
		return new ThreadPoolTaskScheduler();
	}
}