package com.shellhackers.daygo.service;

import com.shellhackers.daygo.model.TransportMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class RoutingService {

    @Value("${mapbox.api.key}")
    private String mapboxApiKey;

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
        String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/" + encoded
                + ".json?limit=1&access_token=" + mapboxApiKey;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(res.body());
        JsonNode features = root.path("features");

        if (features.isEmpty()) return null;

        JsonNode coords = features.get(0).path("geometry").path("coordinates");
        double lon = coords.get(0).asDouble();
        double lat = coords.get(1).asDouble();
        return new double[]{lon, lat};
    }

    private int fetchTravelMinutes(double[] from, double[] to, TransportMode mode) throws Exception {
        String profile = switch (mode) {
            case CAR, TRANSIT -> "driving";
            case BIKE -> "cycling";
            case WALK -> "walking";
        };

        String url = String.format(
                "https://api.mapbox.com/directions/v5/mapbox/%s/%f,%f;%f,%f?access_token=%s",
                profile, from[0], from[1], to[0], to[1], mapboxApiKey
        );

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(res.body());

        JsonNode routes = root.path("routes");
        if (routes.isEmpty()) {
            System.err.println("Mapbox returned no routes. Response: " + res.body());
            return fallback(mode);
        }

        double seconds = routes.get(0).path("duration").asDouble();
        int minutes = (int) Math.ceil(seconds / 60.0);

        return (mode == TransportMode.TRANSIT)
                ? (int) Math.ceil(minutes * 1.6) + 10
                : minutes;
    }

    private int fallback(TransportMode mode) {
        return switch (mode) {
            case CAR -> 20;
            case TRANSIT -> 35;
            case BIKE -> 30;
            case WALK -> 50;
        };
    }
}