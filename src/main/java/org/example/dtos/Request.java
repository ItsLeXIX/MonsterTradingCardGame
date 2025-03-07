package org.example.dtos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Request {
    private String body;
    private Map<String, String> params;

    // Constructor
    public Request(String body, Map<String, String> params) {
        this.body = body;
        this.params = params;
    }

    public String getBody() {
        return body;
    }

    public String getParam(String key) {
        return params.get(key);
    }

    // Manual JSON Parsing (Very Basic)
    public <T> T getBodyAs(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            String content = body.replaceAll("[{}\"]", ""); // Remove braces and quotes
            String[] keyValues = content.split(",");

            Map<String, String> map = new HashMap<>();
            for (String pair : keyValues) {
                String[] entry = pair.split(":");
                map.put(entry[0].trim(), entry[1].trim());
            }

            // Use reflection to set fields
            for (var field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (map.containsKey(field.getName())) {
                    if (field.getType() == int.class || field.getType() == Integer.class) {
                        field.set(instance, Integer.parseInt(map.get(field.getName())));
                    } else if (field.getType() == double.class || field.getType() == Double.class) {
                        field.set(instance, Double.parseDouble(map.get(field.getName())));
                    } else {
                        field.set(instance, map.get(field.getName()));
                    }
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse request body: " + e.getMessage(), e);
        }
    }

    // Example for userId extraction
    public UUID getUserId() {
        UUID userId = UUID.fromString(params.getOrDefault("userId", ""));
        return userId;
    }
}