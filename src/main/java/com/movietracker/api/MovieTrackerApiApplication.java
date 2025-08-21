package com.movietracker.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableConfigurationProperties
public class MovieTrackerApiApplication {

	private static final Logger logger = LoggerFactory.getLogger(MovieTrackerApiApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Movie Tracker API...");
		SpringApplication.run(MovieTrackerApiApplication.class, args);
	}

	@Component
	public static class StartupLogger {
		private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

		@EventListener
		public void handleContextRefresh(ContextRefreshedEvent event) {
			Environment env = event.getApplicationContext().getEnvironment();
			log.info("=== APPLICATION STARTED ===");
			log.info("Port: {}", env.getProperty("server.port"));
			log.info("Health: /health");
			log.info("GraphQL: /graphql");
		}
	}
}
