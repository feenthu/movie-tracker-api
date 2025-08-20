package com.movietracker.api.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import org.springframework.stereotype.Component;

@DgsComponent
public class HealthDataFetcher {
    
    @DgsQuery
    public String health() {
        return "Movie Tracker API is running! 🎬";
    }
}
