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
		logger.info("Starting Movie Tracker API application...");
		logger.info("Active profiles: {}", System.getProperty("spring.profiles.active"));
		logger.info("Port: {}", System.getenv("PORT"));
		logger.info("Database URL present: {}", System.getenv("DATABASE_URL") != null);
		logger.info("JWT Secret present: {}", System.getenv("JWT_SECRET") != null);
		
		SpringApplication.run(MovieTrackerApiApplication.class, args);
		logger.info("Movie Tracker API application started successfully!");
	}

	@Component
	public static class StartupLogger {
		private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

		@EventListener
		public void handleContextRefresh(ContextRefreshedEvent event) {
			Environment env = event.getApplicationContext().getEnvironment();
			log.info("=== APPLICATION STARTED SUCCESSFULLY ===");
			log.info("Server port: {}", env.getProperty("server.port"));
			log.info("Active profiles: {}", String.join(",", env.getActiveProfiles()));
			log.info("Health endpoint should be available at /health");
			log.info("GraphQL endpoint available at /graphql");
			log.info("==========================================");
		}
	}
}
