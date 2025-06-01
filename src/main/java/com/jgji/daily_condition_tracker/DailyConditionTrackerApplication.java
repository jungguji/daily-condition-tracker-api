package com.jgji.daily_condition_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan
@EnableConfigurationProperties
@EnableScheduling
@SpringBootApplication
public class DailyConditionTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DailyConditionTrackerApplication.class, args);
	}

}