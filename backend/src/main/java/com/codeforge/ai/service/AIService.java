package com.codeforge.ai.service;

import com.codeforge.ai.dto.AiHintRequest;
import com.codeforge.ai.dto.AiResponse;
import com.codeforge.ai.entity.Problem;
import com.codeforge.ai.exception.ResourceNotFoundException;
import com.codeforge.ai.repository.ProblemRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    private final ProblemRepository problemRepository;
    private final String provider;
    private final String geminiApiKey;
    private final String model;

    public AIService(ProblemRepository problemRepository,
                     @Value("${ai.provider}") String provider,
                     @Value("${ai.api-key}") String geminiApiKey,
                     @Value("${ai.model}") String model) {
        this.problemRepository = problemRepository;
        this.provider = provider;
        this.geminiApiKey = geminiApiKey;
        this.model = model;
    }

    public AiResponse generateHint(AiHintRequest request) {
        Problem problem = findProblem(request);
        String hintType = getHintType(request.getHintType());
        String prompt = buildPrompt(problem, request, hintType, false);
        return callGeminiAPI(prompt, hintType);
    }

    public AiResponse explainSolution(AiHintRequest request) {
        Problem problem = findProblem(request);
        String prompt = buildPrompt(problem, request, "Solution Explanation", true);
        return callGeminiAPI(prompt, "Solution Explanation");
    }

    private Problem findProblem(AiHintRequest request) {
        if (request.getProblemId() != null) {
            return problemRepository.findById(request.getProblemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));
        }
        if (request.getProblemTitle() != null && request.getProblemDescription() != null) {
            Problem stub = new Problem();
            stub.setTitle(request.getProblemTitle());
            stub.setDescription(request.getProblemDescription());
            return stub;
        }
        throw new ResourceNotFoundException("Problem information is required");
    }

    private String getHintType(String rawHintType) {
        if (rawHintType == null) {
            return "Optimization Tips";
        }
        switch (rawHintType.trim().toLowerCase()) {
            case "algorithm choice":
            case "algorithm_choice":
            case "algorithmchoice":
                return "Algorithm Choice";
            case "optimization tips":
            case "optimization_tips":
            case "optimizationtips":
                return "Optimization Tips";
            case "debug suggestions":
            case "debug_suggestions":
            case "debugsuggestions":
                return "Debug Suggestions";
            case "complexity review":
            case "complexity_review":
            case "complexityreview":
                return "Complexity Review";
            default:
                return "Optimization Tips";
        }
    }

    private String buildPrompt(Problem problem, AiHintRequest request, String hintType, boolean explanationOnly) {
        String title = problem != null ? problem.getTitle() : request.getProblemTitle();
        String description = problem != null ? problem.getDescription() : request.getProblemDescription();
        String difficulty;
        if (request.getDifficulty() != null) {
            difficulty = request.getDifficulty();
        } else if (problem != null && problem.getDifficulty() != null) {
            difficulty = problem.getDifficulty().name();
        } else {
            difficulty = "Not specified";
        }
        String userCode = request.getUserCode();

        StringBuilder prompt = new StringBuilder();
        prompt.append("Please provide ").append(hintType).append(" for the following coding problem:\n\n");
        prompt.append("Title: ").append(title).append("\n");
        prompt.append("Description: ").append(description).append("\n");
        if (difficulty != null) {
            prompt.append("Difficulty: ").append(difficulty).append("\n");
        }
        if (userCode != null && !userCode.isBlank()) {
            prompt.append("User code:\n```").append(userCode).append("```\n");
        }
        prompt.append("\nProvide a concise and actionable response based on the selected hint type.\n");

        if (!explanationOnly) {
            prompt.append("Use the selected category as the primary focus.\n");
        } else {
            prompt.append("Explain the solution approach, mention time and space complexity, and describe why this approach works.\n");
        }

        prompt.append("If the API key is invalid or the request cannot be completed, return a helpful error message.");
        return prompt.toString();
    }

    private AiResponse callGeminiAPI(String prompt, String responseType) {
        if (provider == null || !provider.equalsIgnoreCase("gemini")) {
            return buildErrorResponse("AI provider is not configured for Gemini.", responseType);
        }
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return buildErrorResponse("Gemini API key is missing or invalid.", responseType);
        }
        if (model == null || model.isBlank()) {
            return buildErrorResponse("Gemini model is not configured.", responseType);
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + 
                    ":generateContent?key=" + geminiApiKey;

            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(Timeout.ofSeconds(10))
                    .setResponseTimeout(Timeout.ofSeconds(20))
                    .build();
            httpPost.setConfig(requestConfig);

            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();

            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            parts.add(part);

            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);

            System.out.println("=== GEMINI REQUEST ===");
            System.out.println("MODEL = " + model);
            System.out.println(requestBody.toString());

            httpPost.setEntity(
                    new StringEntity(
                            requestBody.toString(),
                            ContentType.APPLICATION_JSON
                    )
            );

            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String responseBody = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "";
                if (statusCode == 401 || statusCode == 403) {
                    return buildErrorResponse("Invalid Gemini API key or insufficient permissions.", responseType);
                }
                if (statusCode >= 400) {
                    System.out.println("=== GEMINI ERROR ===");
                    System.out.println(responseBody);

                    return buildErrorResponse(
                            "Gemini API request failed with status "
                                    + statusCode
                                    + ": "
                                    + responseBody,
                            responseType
                    );
                }
                return parseGeminiResponse(responseBody, responseType);
            });
        } catch (Exception e) {
            String failureMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            if (failureMessage.toLowerCase().contains("timeout")) {
                return buildErrorResponse("Gemini API request timed out.", responseType);
            }
            return buildErrorResponse("Unable to generate AI response. " + failureMessage, responseType);
        }
    }

    private AiResponse parseGeminiResponse(String responseBody, String responseType) {
        try {
            Gson gson = new Gson();
            JsonObject response = gson.fromJson(responseBody, JsonObject.class);
            if (response == null) {
                return buildErrorResponse("Gemini returned an empty response.", responseType);
            }

            if (response.has("candidates") && response.getAsJsonArray("candidates").size() > 0) {
                JsonObject candidate = response.getAsJsonArray("candidates").get(0).getAsJsonObject();
                if (candidate.has("content") && candidate.getAsJsonObject("content").has("parts")) {
                    JsonArray parts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
                    if (parts.size() > 0) {
                        String content = parts.get(0).getAsJsonObject().get("text").getAsString();
                        return AiResponse.builder()
                                .response(content)
                                .content(content)
                                .type(responseType)
                                .success(true)
                                .build();
                    }
                }
            }

            if (response.has("error")) {
                return buildErrorResponse(response.getAsJsonObject("error").toString(), responseType);
            }
        } catch (Exception e) {
            // Fall through
        }
        return buildErrorResponse("Unable to parse Gemini response.", responseType);
    }

    private AiResponse buildErrorResponse(String message, String responseType) {
        return AiResponse.builder()
                .response(message)
                .content(message)
                .type(responseType)
                .success(false)
                .build();
    }
}
