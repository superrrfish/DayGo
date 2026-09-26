package com.shellhackers.daygo;

import com.shellhackers.daygo.model.Event;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class EventStore {

    @Bean
    public Map<String, Event> eventMap() {
        return new ConcurrentHashMap<>();
    }
}