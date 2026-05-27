package com.selvendran.smarthome;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Local Ollama {@code POST /api/generate}: text chat and vision (Plant Health) via {@code images}.
 */
@Service
public class OllamaService {

    /** Vision models (llava) often need longer than text-only on CPU. */
    private static final Duration OLLAMA_TIMEOUT = Duration.ofMinutes(15);

    /** UI order: default phi3 first, then llama3. */
    public static final List<String> CHAT_MODELS = List.of("phi3", "llama3");

    private static final Set<String> CHAT_MODEL_SET = Set.copyOf(CHAT_MODELS);

    private static final String PLANT_DOCTOR_PROMPT = "You are an expert plant pathologist and AI Plant Doctor. "
            + "Analyze this image of a plant. "
            + "1. Identify the plant if possible. "
            + "2. Identify any visible diseases, pests, or nutrient deficiencies. "
            + "3. Provide step-by-step actionable solutions or treatments to cure it. "
            + "Reply in a clear, formatted style using Markdown (headers, bullets, bold).";

    private final WebClient ollamaWebClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String visionModel;
    private final String baseUrl;

    public OllamaService(
            WebClient ollamaWebClient,
            ObjectMapper objectMapper,
            @Value("${ollama.base.url:http://localhost:11434}") String baseUrl,
            @Value("${ollama.model:phi3}") String model,
            @Value("${ollama.vision.model:llava}") String visionModel) {
        this.ollamaWebClient = ollamaWebClient;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.model = model;
        this.visionModel = visionModel;
    }

    public String getModel() {
        return model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getGenerateUrl() {
        return baseUrl + "/api/generate";
    }

    public String getVisionModel() {
        return visionModel;
    }

    /**
     * Plant Health: tries {@code ollama.vision.model} first, then common vision tags if Ollama returns “model not found”.
     */
    public String analyzePlantWithImage(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return "Error: Empty image.";
        }
        String b64 = Base64.getEncoder().encodeToString(imageBytes);
        List<String> candidates = visionModelCandidates();
        String lastError = null;

        for (String m : candidates) {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("model", m);
            request.put("prompt", PLANT_DOCTOR_PROMPT);
            request.put("images", List.of(b64));
            request.put("stream", Boolean.FALSE);

            try {
                Map<String, Object> json = blockGenerate(request);
                String out = extractResponseText(json, m);
                if (responseIndicatesMissingModel(out)) {
                    lastError = out;
                    continue;
                }
                return out;
            } catch (WebClientResponseException e) {
                int code = e.getStatusCode().value();
                String body = e.getResponseBodyAsString();
                if (code == 404 && bodyIndicatesMissingModel(body)) {
                    lastError = formatHttpError(code, body);
                    continue;
                }
                return formatHttpError(code, body);
            } catch (Exception e) {
                return "Cannot reach Ollama at " + baseUrl
                        + ". Start Ollama, then run: ollama pull llava — " + e.getMessage();
            }
        }

        return (lastError != null ? lastError + "\n\n" : "")
                + "Install a vision model (example): ollama pull llava\n"
                + "Then check the name with: ollama list\n"
                + "Set ollama.vision.model in application.properties to that exact name.";
    }

    private List<String> visionModelCandidates() {
        List<String> order = new ArrayList<>();
        order.add(visionModel);
        for (String alt : List.of("llava:latest", "llava", "llava:7b", "moondream")) {
            if (!order.contains(alt)) {
                order.add(alt);
            }
        }
        return order;
    }

    private static boolean bodyIndicatesMissingModel(String body) {
        if (body == null) {
            return false;
        }
        String s = body.toLowerCase();
        return s.contains("not found") && s.contains("model");
    }

    private static boolean responseIndicatesMissingModel(String text) {
        if (text == null) {
            return false;
        }
        String s = text.toLowerCase();
        return s.contains("not found") && s.contains("model");
    }

    /**
     * Text chat using default {@code ollama.model} from configuration.
     */
    public String generate(String prompt) {
        return generate(prompt, null);
    }

    /**
     * Text chat using {@code requestedChatModel} when non-blank and allowed ({@link #CHAT_MODELS}), else default config model.
     *
     * @return assistant text only, or a clear error if model is unknown or not installed.
     */
    public String generate(String prompt, String requestedChatModel) {
        if (prompt == null || prompt.isBlank()) {
            return "Please enter a question.";
        }
        String resolved = resolveChatModel(requestedChatModel);
        if (resolved.startsWith("Invalid")) {
            return resolved;
        }

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", resolved);
        request.put("prompt", prompt.strip());
        request.put("stream", Boolean.FALSE);
        return postGenerate(request, resolved);
    }

    /**
     * Normalizes optional UI model id; invalid values return an error line (does not call Ollama).
     */
    public String resolveChatModel(String requestedChatModel) {
        if (requestedChatModel == null || requestedChatModel.isBlank()) {
            return model.strip();
        }
        String id = requestedChatModel.strip().toLowerCase(Locale.ROOT);
        if (!CHAT_MODEL_SET.contains(id)) {
            return "Invalid model: choose one of " + String.join(", ", CHAT_MODELS) + ".";
        }
        return id;
    }

    private Map<String, Object> blockGenerate(Map<String, Object> requestBody) {
        return ollamaWebClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(OLLAMA_TIMEOUT)
                .block();
    }

    private String postGenerate(Map<String, Object> requestBody, String modelForHelpText) {
        try {
            Map<String, Object> json = blockGenerate(requestBody);
            return extractResponseText(json, modelForHelpText);
        } catch (WebClientResponseException e) {
            return formatHttpError(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (Exception e) {
            return "Cannot reach Ollama at " + baseUrl
                    + ". Is Ollama running? Try: ollama pull " + modelForHelpText + " — " + e.getMessage();
        }
    }

    private String extractResponseText(Map<String, Object> json, String modelHint) {
        if (json == null) {
            return "Empty response from Ollama.";
        }
        Object err = json.get("error");
        if (err != null) {
            return friendlyModelHint(err.toString(), modelHint);
        }
        Object r = json.get("response");
        if (r == null || r.toString().isBlank()) {
            return "No answer text from Ollama. Try `ollama pull " + modelHint + "` or pick another model.";
        }
        return r.toString().strip();
    }

    private String formatHttpError(int status, String body) {
        String detail = trimBody(body);
        String fromJson = tryParseErrorField(detail);
        if (fromJson != null) {
            detail = fromJson;
        }
        return friendlyModelHint("Ollama HTTP " + status + (detail.isBlank() ? "" : ": " + detail), null);
    }

    private String tryParseErrorField(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode err = root.get("error");
            if (err != null && !err.isNull()) {
                return err.asText();
            }
        } catch (Exception ignored) {
            /* not JSON */
        }
        return null;
    }

    private static String trimBody(String raw) {
        if (raw == null) {
            return "";
        }
        String t = raw.strip();
        return t.length() > 400 ? t.substring(0, 400) + "…" : t;
    }

    private String friendlyModelHint(String message, String modelForPlant) {
        if (message == null || !message.contains("not found")) {
            return message;
        }
        if (modelForPlant != null && visionModel.equals(modelForPlant)) {
            return message + " Install a vision model: `ollama pull llava`, then set ollama.vision.model.";
        }
        if (modelForPlant != null && CHAT_MODEL_SET.contains(modelForPlant.toLowerCase(Locale.ROOT))) {
            return message + " Install this chat model: `ollama pull " + modelForPlant + "` then check `ollama list`.";
        }
        return message + " Run `ollama list` and pull the model name Ollama expects.";
    }
}
