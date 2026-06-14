package com.codeforge.ai.service;

import com.codeforge.ai.dto.CodeRunRequest;
import com.codeforge.ai.dto.CodeRunResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class Judge0Service {

    @Value("${judge0.baseUrl:${judge0.url:https://ce.judge0.com}}")
    private String judge0Url;

    @Value("${judge0.apiKey:}")
    private String apiKey;

    @Value("${judge0.apiHost:}")
    private String apiHost;

    @Value("${app.mock-mode:false}")
    private boolean mockMode;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final RestTemplate rest = new RestTemplate();

    private static final Logger LOGGER = LoggerFactory.getLogger(Judge0Service.class);
    private static final Pattern SOLUTION_CLASS_PATTERN = Pattern.compile("\\bclass\\s+Solution\\b");
    private static final Pattern MAIN_CLASS_PATTERN = Pattern.compile("\\bclass\\s+Main\\b");
    private static final Pattern SOLUTION_METHOD_PATTERN = Pattern.compile("public\\s+(?:static\\s+)?([\\w\\[\\]<>]+)\\s+([A-Za-z_][$\\w]*)\\s*\\(([^)]*)\\)");

    private int languageIdFor(String language) {
        if (language == null) return 62; // default Java
        String l = language.trim().toLowerCase();
        return switch (l) {
            case "java", "java11", "java17" -> 62;
            case "c++", "cpp", "cplusplus" -> 54;
            case "python", "python3", "py" -> 71;
            default -> 62;
        };
    }

    public CodeRunResponse run(CodeRunRequest request) {
        try {
            String createUrl = judge0Url + "/submissions?base64_encoded=true";

            String sourceCode = request.getCode() == null ? "" : request.getCode();
            String stdin = request.getCustomInput() == null ? "" : request.getCustomInput();
            int languageId = languageIdFor(request.getLanguage());

            String finalSource = sourceCode;
            if (shouldWrapJavaSolution(sourceCode, request.getLanguage())) {
                finalSource = buildJavaMainWrapper(sourceCode, stdin);
                log.info("Generated Main.java wrapper for Solution code:\n{}", finalSource);
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("language_id", languageId);
            payload.put("source_code", Base64.getEncoder().encodeToString(finalSource.getBytes()));
            payload.put("stdin", Base64.getEncoder().encodeToString(stdin.getBytes()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            boolean hasApiKey = apiKey != null && !apiKey.isBlank();
            if (hasApiKey) {
                headers.set("X-RapidAPI-Key", apiKey);
            }
            boolean hasApiHost = apiHost != null && !apiHost.isBlank();
            if (hasApiHost) {
                headers.set("X-RapidAPI-Host", apiHost);
            }

            String payloadJson = payload.toString();
            try {
                payloadJson = OBJECT_MAPPER.writeValueAsString(payload);
            } catch (JsonProcessingException ignored) {
            }

            String apiKeyMask = hasApiKey ? apiKey.substring(0, Math.min(5, apiKey.length())) + "..." : "(none)";
            boolean usingRapidApi = hasApiKey || hasApiHost || judge0Url.contains("rapidapi.com");

            if (mockMode) {
                log.info("MOCK_MODE enabled: bypassing Judge0 and returning fake response for connectivity testing.");
                return new CodeRunResponse("[0,1]", "52 ms", "43 MB", "Accepted");
            }

            log.info("Starting Judge0 execution request: languageId={} language={} sourceLength={} stdinLength={} createUrl={} apiKeySet={} apiHostSet={}", languageId, request.getLanguage(), sourceCode.length(), stdin.length(), createUrl, hasApiKey, hasApiHost);
            log.info("Judge0 config: url={} apiHost={} apiKeyPrefix={} rapidApiMode={}", judge0Url, apiHost == null ? "(none)" : apiHost, apiKeyMask, usingRapidApi);
            log.info("Submitting to Judge0 create endpoint: {}", createUrl);
            log.debug("Judge0 request baseUrl={} language={} codeLength={} stdinLength={} apiKeySet={} apiHostSet={}", judge0Url, languageId, sourceCode.length(), stdin.length(), hasApiKey, hasApiHost);
            if (usingRapidApi && (!hasApiKey || !hasApiHost)) {
                log.warn("Judge0 RapidAPI mode detected but judge0.apiKey or judge0.apiHost is missing. This will cause Access Denied.");
            }
            if (judge0Url.contains("rapidapi.com") && !hasApiHost) {
                log.warn("Judge0 URL appears to be RapidAPI but judge0.apiHost is not configured. Set judge0.apiHost=judge0-ce.p.rapidapi.com or judge0-extra-ce.p.rapidapi.com.");
            }
            log.debug("Judge0 request payload={}", payloadJson);
            log.debug("Judge0 request headers={}", headers.toSingleValueMap());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> createResp;
            try {
                createResp = rest.exchange(createUrl, HttpMethod.POST, entity, Map.class);
            } catch (HttpStatusCodeException ex) {
                String responseBody = ex.getResponseBodyAsString();
                log.error("Judge0 create HTTP error url={} status={} body={}", createUrl, ex.getStatusCode(), responseBody, ex);
                return new CodeRunResponse("Judge0 create failed: " + ex.getStatusText(), "0 s", "0 KB", "Error");
            } catch (ResourceAccessException ex) {
                String message = extractNetworkError(ex);
                log.error("Judge0 create network error url={} message={}", createUrl, message, ex);
                return new CodeRunResponse("Judge0 network error: " + message, "0 s", "0 KB", "Error");
            } catch (Exception ex) {
                log.error("Judge0 create unexpected error url={}", createUrl, ex);
                return new CodeRunResponse("Judge0 create unexpected error: " + ex.getMessage(), "0 s", "0 KB", "Error");
            }
            int statusCode = createResp.getStatusCodeValue();
            Map createBody = createResp.getBody();
            log.debug("Judge0 create response status={} body={}", statusCode, createBody);

            if (statusCode == 401) {
                log.warn("Judge0 create returned 401 Unauthorized, body={}", createBody);
                return new CodeRunResponse("Judge0 returned 401 Unauthorized. Set judge0.apiKey in application.properties or environment, or run a local Judge0 CE instance and set judge0.url.", "0 s", "0 KB", "Unauthorized");
            }

            if (!createResp.getStatusCode().is2xxSuccessful() || createBody == null) {
                log.warn("Judge0 create failed: {} body={}", statusCode, createBody);
                String message = createBody != null && createBody.get("message") != null ? String.valueOf(createBody.get("message")) : "Execution failed due to Judge0 create error.";
                return new CodeRunResponse(message, "0 s", "0 KB", "Error");
            }

            Object tokenObj = createBody.get("token");
            if (tokenObj == null) {
                log.warn("Judge0 create returned no token, body={}", createBody);
                return new CodeRunResponse("Execution failed due to missing submission token.", "0 s", "0 KB", "Error");
            }

            String token = String.valueOf(tokenObj);
            log.info("Judge0 submission token: {}", token);
            log.info("Waiting for Judge0 result for token={} at polling url={}", token, judge0Url + "/submissions/" + token + "?base64_encoded=true");

            String pollUrl = judge0Url + "/submissions/" + token + "?base64_encoded=true";

            int maxAttempts = 60;
            int attempt = 0;
            Map pollBody = null;
            String lastPollError = null;

            while (attempt < maxAttempts) {
                attempt++;
                try {
                    log.debug("Polling Judge0 (attempt {}) url={} headers={}", attempt, pollUrl, headers.toSingleValueMap());
                    ResponseEntity<Map> pollResp = rest.exchange(pollUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
                    int pollStatus = pollResp.getStatusCodeValue();
                    Map body = pollResp.getBody();
                    if (pollResp.getStatusCode().is2xxSuccessful() && body != null) {
                        pollBody = body;
                        Map status = (Map) body.get("status");
                        Integer statusId = status != null && status.get("id") != null ? Integer.parseInt(String.valueOf(status.get("id"))) : null;
                        String statusDesc = status != null ? String.valueOf(status.get("description")) : "Unknown";
                        log.debug("Judge0 poll response status={} statusId={} statusDesc={} body={}", pollStatus, statusId, statusDesc, body);
                        if (statusId != null && statusId != 1 && statusId != 2) {
                            break;
                        }
                    } else {
                        lastPollError = "Judge0 poll non-2xx: " + pollStatus;
                        log.warn("{} body={}", lastPollError, body);
                    }
                } catch (HttpStatusCodeException ex) {
                    String responseBody = ex.getResponseBodyAsString();
                    lastPollError = "Judge0 poll HTTP error: " + ex.getStatusCode();
                    log.error("Judge0 poll HTTP error url={} status={} body={}", pollUrl, ex.getStatusCode(), responseBody, ex);
                    break;
                } catch (ResourceAccessException ex) {
                    String message = extractNetworkError(ex);
                    lastPollError = "Judge0 poll network error: " + message;
                    log.error("Judge0 poll network error url={} message={}", pollUrl, message, ex);
                    break;
                } catch (Exception ex) {
                    lastPollError = "Error polling Judge0: " + ex.getMessage();
                    log.error(lastPollError, ex);
                    break;
                }

                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (pollBody == null) {
                log.warn("Judge0 polling failed without result. lastPollError={}", lastPollError);
                CodeRunResponse timeoutResponse = new CodeRunResponse();
                timeoutResponse.setOutput(lastPollError != null ? lastPollError : "Judge0 polling timed out.");
                timeoutResponse.setStdout(null);
                timeoutResponse.setStderr(null);
                timeoutResponse.setCompileOutput(null);
                timeoutResponse.setRuntime("0 s");
                timeoutResponse.setMemory("0 KB");
                timeoutResponse.setStatus("Error");
                return timeoutResponse;
            }

            String stdout = decodeBase64(pollBody.get("stdout"));
            String stderr = decodeBase64(pollBody.get("stderr"));
            String compileOutput = decodeBase64(pollBody.get("compile_output"));
            Map status = (Map) pollBody.get("status");
            String statusDesc = status != null ? String.valueOf(status.get("description")) : "Unknown";
            String time = pollBody.get("time") != null ? String.valueOf(pollBody.get("time")) : "0";
            String memory = pollBody.get("memory") != null ? String.valueOf(pollBody.get("memory")) : "0";

            String output = stdout != null ? stdout : "";
            if (compileOutput != null && !compileOutput.isEmpty()) {
                statusDesc = "Compilation Error";
                output = compileOutput;
            } else if (stderr != null && !stderr.isEmpty()) {
                if (!statusDesc.toLowerCase().contains("error")) {
                    statusDesc = "Runtime Error";
                }
                output = stderr;
            }

            CodeRunResponse res = new CodeRunResponse();
            res.setOutput(output);
            res.setStdout(stdout);
            res.setStderr(stderr);
            res.setCompileOutput(compileOutput);
            res.setRuntime(time + " s");
            res.setMemory(memory + " KB");
            res.setStatus(statusDesc);
            log.info("Judge0 final status: {} runtime={} memory={}", statusDesc, time, memory);
            return res;
        } catch (Exception ex) {
            log.error("Error invoking Judge0", ex);
            String message = extractNetworkError(ex);
            return new CodeRunResponse("Execution failed: " + message, "0 s", "0 KB", "Error");
        }
    }

    private boolean shouldWrapJavaSolution(String sourceCode, String language) {
        if (language == null) {
            return false;
        }
        String lang = language.trim().toLowerCase();
        if (!lang.startsWith("java")) {
            return false;
        }
        if (!SOLUTION_CLASS_PATTERN.matcher(sourceCode).find()) {
            return false;
        }
        return !MAIN_CLASS_PATTERN.matcher(sourceCode).find();
    }

    static String buildJavaMainWrapper(String sourceCode, String stdin) {
        Matcher methodMatcher = SOLUTION_METHOD_PATTERN.matcher(sourceCode);
        String methodName = "solve";
        String returnType = "void";
        String methodArgs = "";
        boolean found = false;
        while (methodMatcher.find()) {
            String type = methodMatcher.group(1);
            String name = methodMatcher.group(2);
            String args = methodMatcher.group(3).trim();
            if (!name.equals("Solution") && !name.equals("main")) {
                methodName = name;
                returnType = type;
                methodArgs = args;
                found = true;
                break;
            }
        }

        String invocation;
        if (!found || methodArgs.isEmpty()) {
            invocation = "System.out.println(new Solution()." + methodName + "());";
        } else {
            String[] argParts = methodArgs.split(",");
            StringBuilder setupCode = new StringBuilder();
            String[] argNames = new String[argParts.length];
            boolean useLines = argParts.length > 1;

            if (useLines) {
                setupCode.append("String[] lines = input.split(\"\\r?\\n\");\n        ");
            }

            for (int i = 0; i < argParts.length; i++) {
                String arg = argParts[i].trim();
                String[] argTokens = arg.split("\\s+");
                String argType = argTokens.length > 1 ? argTokens[0] : "String";
                String argName = argTokens.length > 1 ? argTokens[1] : "arg";
                argNames[i] = argName;

                String argInput;
                if (!useLines) {
                    argInput = "input";
                } else if (i == argParts.length - 1) {
                    argInput = "String.join(\"\\n\", java.util.Arrays.copyOfRange(lines, " + i + ", lines.length))";
                } else {
                    argInput = "(lines.length > " + i + " ? lines[" + i + "] : \"\")";
                }

                setupCode.append(buildJavaArgumentInitialization(argType, argName, argInput));
                setupCode.append("\n        ");
            }

            String joinedArgs = String.join(", ", java.util.Arrays.asList(argNames));
            String resultExpression = "new Solution()." + methodName + "(" + joinedArgs + ")";
            if ("void".equals(returnType)) {
                invocation = setupCode.toString() + "new Solution()." + methodName + "(" + joinedArgs + ");";
            } else {
                invocation = setupCode.toString() + "System.out.println(" + buildJavaOutputExpression(returnType, resultExpression) + ");";
            }
        }

        return sourceCode + "\n\npublic class Main {\n    public static void main(String[] args) {\n        String input = readInput();\n        " + invocation + "\n    }\n\n    private static String readInput() {\n        try (java.util.Scanner scanner = new java.util.Scanner(System.in).useDelimiter(\"\\\\A\")) {\n            return scanner.hasNext() ? scanner.next() : \"\";\n        }\n    }\n}\n";
    }

    private static String buildJavaArgumentInitialization(String argType, String argName, String argInput) {
        return switch (argType) {
            case "String" -> "String " + argName + " = " + argInput + ".trim();";
            case "int" -> "int " + argName + " = Integer.parseInt(" + argInput + ".trim());";
            case "long" -> "long " + argName + " = Long.parseLong(" + argInput + ".trim());";
            case "boolean" -> "boolean " + argName + " = Boolean.parseBoolean(" + argInput + ".trim());";
            case "String[]" -> "String[] " + argName + " = " + argInput + ".trim().isEmpty() ? new String[0] : " + argInput + ".trim().split(\"[\\\\s,]+\");";
            case "int[]" -> "int[] " + argName + " = " + argInput + ".trim().isEmpty() ? new int[0] : java.util.Arrays.stream(" + argInput + ".trim().split(\"[\\\\s,]+\")).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray();";
            case "long[]" -> "long[] " + argName + " = " + argInput + ".trim().isEmpty() ? new long[0] : java.util.Arrays.stream(" + argInput + ".trim().split(\"[\\\\s,]+\")).filter(s -> !s.isEmpty()).mapToLong(Long::parseLong).toArray();";
            case "char[]" -> "char[] " + argName + " = " + argInput + ".replaceAll(\"\\\\r?\\\\n\", \"\").toCharArray();";
            case "int[][]" -> "String[] " + argName + "Rows = " + argInput + ".trim().isEmpty() ? new String[0] : " + argInput + ".trim().split(\"\\\\r?\\\\n\");\n        int[][] " + argName + " = java.util.Arrays.stream(" + argName + "Rows).map(row -> java.util.Arrays.stream(row.trim().split(\"[\\\\s,]+\")).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray()).toArray(int[][]::new);";
            case "long[][]" -> "String[] " + argName + "Rows = " + argInput + ".trim().isEmpty() ? new String[0] : " + argInput + ".trim().split(\"\\\\r?\\\\n\");\n        long[][] " + argName + " = java.util.Arrays.stream(" + argName + "Rows).map(row -> java.util.Arrays.stream(row.trim().split(\"[\\\\s,]+\")).filter(s -> !s.isEmpty()).mapToLong(Long::parseLong).toArray()).toArray(long[][]::new);";
            case "String[][]" -> "String[] " + argName + "Rows = " + argInput + ".trim().isEmpty() ? new String[0] : " + argInput + ".trim().split(\"\\\\r?\\\\n\");\n        String[][] " + argName + " = java.util.Arrays.stream(" + argName + "Rows).map(row -> row.trim().isEmpty() ? new String[0] : row.trim().split(\"[\\\\s,]+\")).toArray(String[][]::new);";
            case "char[][]" -> "String[] " + argName + "Rows = " + argInput + ".split(\"\\r?\\n\");\n        char[][] " + argName + " = java.util.Arrays.stream(" + argName + "Rows).map(row -> row.trim().toCharArray()).toArray(char[][]::new);";
            case "List<String>" -> "java.util.List<String> " + argName + " = " + argInput + ".trim().isEmpty() ? java.util.Collections.emptyList() : java.util.Arrays.asList(" + argInput + ".trim().split(\"[\\\\s,]+\"));";
            case "List<Integer>" -> "java.util.List<Integer> " + argName + " = " + argInput + ".trim().isEmpty() ? java.util.Collections.emptyList() : java.util.Arrays.stream(" + argInput + ".trim().split(\"[\\\\s,]+\")).filter(s -> !s.isEmpty()).map(Integer::parseInt).boxed().toList();";
            default -> "String " + argName + " = " + argInput + ";";
        };
    }

    private static String buildJavaOutputExpression(String returnType, String expression) {
        return switch (returnType) {
            case "int", "long", "boolean", "String" -> expression;
            case "char[]" -> "new String(" + expression + ")";
            case "String[]", "int[]", "long[]" -> "java.util.Arrays.toString(" + expression + ")";
            case "char[][]", "String[][]", "int[][]", "long[][]" -> "java.util.Arrays.deepToString(" + expression + ")";
            default -> expression;
        };
    }

    private String extractNetworkError(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof ConnectException) {
                return "ConnectException: " + current.getMessage();
            }
            if (current instanceof SocketTimeoutException) {
                return "SocketTimeoutException: " + current.getMessage();
            }
            if (current instanceof UnknownHostException) {
                return "UnknownHostException: " + current.getMessage();
            }
            if (current instanceof SSLException) {
                return "SSLException: " + current.getMessage();
            }
            if (current instanceof ResourceAccessException) {
                current = current.getCause();
                continue;
            }
            current = current.getCause();
        }
        return exception.getMessage() != null ? exception.getMessage() : "Unknown network error";
    }

    private String decodeBase64(Object maybe) {
        if (maybe == null) return null;
        try {
            String s = String.valueOf(maybe);
            if (s.isEmpty()) return null;
            byte[] decoded = Base64.getDecoder().decode(s);
            return new String(decoded);
        } catch (Exception e) {
            return String.valueOf(maybe);
        }
    }
}
