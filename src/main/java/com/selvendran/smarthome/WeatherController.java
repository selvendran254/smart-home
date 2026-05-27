package com.selvendran.smarthome;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class WeatherController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${smarthome.weather.latitude:13.0827}")
    private double latitude;

    @Value("${smarthome.weather.longitude:80.2707}")
    private double longitude;

    @Value("${smarthome.weather.openweather.api-key:}")
    private String openWeatherApiKey;

    @GetMapping("/api/weather")
    public Map<String, Object> weather() {
        if (openWeatherApiKey != null && !openWeatherApiKey.isBlank()) {
            Map<String, Object> ow = fetchOpenWeather();
            if (ow != null) {
                return ow;
            }
        }
        return fetchOpenMeteo();
    }

    private Map<String, Object> fetchOpenWeather() {
        try {
            String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude
                    + "&lon=" + longitude + "&units=metric&appid=" + openWeatherApiKey.trim();
            JsonNode root = objectMapper.readTree(restTemplate.getForObject(url, String.class));
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("provider", "openweather");
            out.put("temp", root.path("main").path("temp").asDouble());
            out.put("humidity", root.path("main").path("humidity").asInt());
            out.put("description", root.path("weather").get(0).path("description").asText("—"));
            out.put("icon", weatherEmoji(root.path("weather").get(0).path("main").asText("")));
            out.put("city", root.path("name").asText("Your area"));
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> fetchOpenMeteo() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("provider", "open-meteo");
        try {
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude
                    + "&longitude=" + longitude
                    + "&current=temperature_2m,relative_humidity_2m,weather_code";
            JsonNode root = objectMapper.readTree(restTemplate.getForObject(url, String.class));
            JsonNode cur = root.path("current");
            out.put("temp", cur.path("temperature_2m").asDouble());
            out.put("humidity", cur.path("relative_humidity_2m").asInt());
            int code = cur.path("weather_code").asInt(0);
            out.put("description", wmoDescription(code));
            out.put("icon", wmoEmoji(code));
            out.put("city", "Chennai area");
        } catch (Exception e) {
            out.put("temp", "—");
            out.put("humidity", "—");
            out.put("description", "Unavailable");
            out.put("icon", "🌡️");
            out.put("city", "Weather");
        }
        return out;
    }

    private static String weatherEmoji(String main) {
        return switch (main.toLowerCase()) {
            case "clear" -> "☀️";
            case "clouds" -> "☁️";
            case "rain", "drizzle" -> "🌧️";
            case "thunderstorm" -> "⛈️";
            case "snow" -> "❄️";
            case "mist", "fog", "haze" -> "🌫️";
            default -> "🌤️";
        };
    }

    private static String wmoEmoji(int code) {
        if (code == 0) return "☀️";
        if (code <= 3) return "⛅";
        if (code <= 48) return "🌫️";
        if (code <= 67) return "🌧️";
        if (code <= 77) return "❄️";
        if (code <= 82) return "🌦️";
        if (code <= 99) return "⛈️";
        return "🌤️";
    }

    private static String wmoDescription(int code) {
        if (code == 0) return "Clear sky";
        if (code <= 3) return "Partly cloudy";
        if (code <= 48) return "Foggy";
        if (code <= 67) return "Rain";
        if (code <= 77) return "Snow";
        if (code <= 82) return "Showers";
        if (code <= 99) return "Thunderstorm";
        return "Variable";
    }
}
