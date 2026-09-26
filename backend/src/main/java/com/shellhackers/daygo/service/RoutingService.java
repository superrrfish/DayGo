package com.shellhackers.daygo.service;

import com.shellhackers.daygo.model.TransportMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RoutingService {

    @Value("${ors.api.key}")
    private String orsApiKey;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String DEFAULT_ORIGIN = "Miami Lakes, FL";

    public int getTravelMinutes(String origin, String destination, TransportMode mode) {
        try {
            String from = (origin != null && !origin.isBlank()) ? origin : DEFAULT_ORIGIN;
            double[] fromCoords = geocode(from);
            double[] toCoords = geocode(destination);

            if (fromCoords == null || toCoords == null) {
                return fallback(mode);
            }

            return fetchTravelMinutes(fromCoords, toCoords, mode);
        } catch (Exception e) {
            System.err.println("Routing failed, using fallback: " + e.getMessage());
            return fallback(mode);
        }
    }

    private double[] geocode(String location) throws Exception {
        String encoded = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String url = "https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=json&limit=1";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "DayGo/1.0 shellhackers@hackathon")
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode arr = mapper.readTree(res.body());

        if (arr.isEmpty()) return null;

        double lat = arr.get(0).get("lat").asDouble();
        double lon = arr.get(0).get("lon").asDouble();
        return new double[]{lon, lat}; // ORS expects [lng, lat]
    }

    private int fetchTravelMinutes(double[] from, double[] to, TransportMode mode) throws Exception {
        String profile = switch (mode) {
            case CAR, RIDESHARE -> "driving-car";
            case BIKE -> "cycling-regular";
            case WALK -> "foot-walking";
            case TRANSIT -> null; // ORS doesn't support transit, fall back
        };

        if (profile == null) return fallback(mode);

        String body = String.format(
                "{\"coordinates\":[[%f,%f],[%f,%f]]}",
                from[0], from[1], to[0], to[1]
        );

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openrouteservice.org/v2/directions/" + profile))
                .header("Authorization", orsApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(res.body());

        // duration is in seconds
        double seconds = root
                .path("routes").get(0)
                .path("summary")
                .path("duration")
                .asDouble();

        return (int) Math.ceil(seconds / 60.0);
    }

    private int fallback(TransportMode mode) {
        return switch (mode) {
            case CAR -> 20;
            case RIDESHARE -> 25;
            case TRANSIT -> 35;
            case BIKE -> 30;
            case WALK -> 50;
        };
    }
}