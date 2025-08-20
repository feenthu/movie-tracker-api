package com.movietracker.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "MovieTrackerAPI");
        response.put("port", System.getenv().getOrDefault("PORT", "8080"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "MovieTrackerAPI");
        response.put("status", "Running");
        response.put("endpoints", Map.of(
            "health", "/health",
            "graphql", "/graphql",
            "graphiql", "/graphiql"
        ));
        return ResponseEntity.ok(response);
    }
}
