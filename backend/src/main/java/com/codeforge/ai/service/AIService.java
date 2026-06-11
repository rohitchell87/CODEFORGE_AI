package com.codeforge.ai.service;

import com.codeforge.ai.dto.AiResponse;
import com.codeforge.ai.entity.Problem;
import com.codeforge.ai.exception.ResourceNotFoundException;
import com.codeforge.ai.repository.ProblemRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    private final ProblemRepository problemRepository;
    private final String geminiApiKey;
    private final String model;

    public AIService(ProblemRepository problemRepository,
                     @Value("${ai.api-key}") String geminiApiKey,
                     @Value("${ai.model}") String model) {
        this.problemRepository = problemRepository;
        this.geminiApiKey = geminiApiKey;
        this.model = model;
    }

    public AiResponse generateHint(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        String prompt = "Provide a helpful hint for this coding problem:\n\n" +
                "Title: " + problem.getTitle() + "\n" +
                "Description: " + problem.getDescription() + "\n" +
                "Difficulty: " + problem.getDifficulty() + "\n" +
                "Category: " + problem.getCategory() + "\n\n" +
                "Give a concise hint without revealing the complete solution.";

        return callGeminiAPI(prompt);
    }

    public AiResponse explainSolution(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        String prompt = "Explain the solution approach for this coding problem:\n\n" +
                "Title: " + problem.getTitle() + "\n" +
                "Description: " + problem.getDescription() + "\n" +
                "Solution: " + problem.getSampleSolution() + "\n" +
                "Difficulty: " + problem.getDifficulty() + "\n\n" +
                "Provide a clear explanation of the approach and time/space complexity.";

        return callGeminiAPI(prompt);
    }

    private AiResponse callGeminiAPI(String prompt) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + 
                         ":generateContent?key=" + geminiApiKey;

            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");

            JsonObject requestBody = new JsonObject();
            JsonObject contents = new JsonObject();
            JsonObject parts = new JsonObject();

            parts.addProperty("text", prompt);
            contents.add("parts", com.google.gson.JsonParser.parseString("[" + parts + "]"));
            requestBody.add("contents", com.google.gson.JsonParser.parseString("[" + contents + "]"));

            httpPost.setEntity(new StringEntity(requestBody.toString()));

            return httpClient.execute(httpPost, response -> {
                String responseBody = EntityUtils.toString(response.getEntity());
                return parseGeminiResponse(responseBody);
            });

        } catch (Exception e) {
            return AiResponse.builder()
                    .content("Unable to generate AI response. Please try again later.")
                    .type("HINT")
                    .success(false)
                    .build();
        }
    }

    private AiResponse parseGeminiResponse(String responseBody) {
        try {
            Gson gson = new Gson();
            JsonObject response = gson.fromJson(responseBody, JsonObject.class);

            if (response.has("candidates") && response.getAsJsonArray("candidates").size() > 0) {
                String content = response.getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();

                return AiResponse.builder()
                        .content(content)
                        .type("HINT")
                        .success(true)
                        .build();
            }
        } catch (Exception e) {
            // Fall through to default response
        }

        return AiResponse.builder()
                .content("Unable to generate AI response.")
                .type("HINT")
                .success(false)
                .build();
    }
}
