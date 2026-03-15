package com.support.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.support.model.Message;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ClaudeClient {
    
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-20250514";
    private static final int MAX_TOKENS = 1024;
    private static final MediaType JSON_TYPE = MediaType.get("application/json"); 

    private final OkHttpClient http;
    private final ObjectMapper mapper;
    private final String apiKey;

    public ClaudeClient(String apiKey) {
        this.apiKey  = apiKey;
        this.http    = new OkHttpClient();
        this.mapper  = new ObjectMapper();
    }

    // Used by: AgentRouter, TechnicalAgent
    public String complete(String systemPrompt, List<Message> history) throws IOException {
        ObjectNode body = buildBaseBody(systemPrompt, history);
        JsonNode root = mapper.readTree(post(body));

        for (JsonNode block : root.path("content")) {
            if ("text".equals(block.path("type").asText())) {
                return block.path("text").asText();
            }
        }
        throw new IOException("No text block in response: " + root);
    }

    // Used by: BillingAgent (first call)
    public JsonNode completeWithTools(String systemPrompt,
                                       List<Message> history,
                                       List<Map<String, Object>> tools) throws IOException {
        ObjectNode body = buildBaseBody(systemPrompt, history);

        ArrayNode toolsNode = mapper.createArrayNode();
        for (Map<String, Object> tool : tools) {
            toolsNode.add(mapper.valueToTree(tool));
        }
        body.set("tools", toolsNode);

        return mapper.readTree(post(body));
    }

    // Used by: BillingAgent (second call, after tool execution)
    public String continueWithToolResult(String systemPrompt,
                                        List<Message> history,
                                        JsonNode assistantMessage,
                                        String toolUseId,
                                        String toolResult,
                                        List<Map<String, Object>> tools) throws IOException {
        // Build a fresh minimal conversation:
        // just the last user message + assistant tool_use + tool_result
        // This avoids any stale tool_use blocks from previous turns
        ObjectNode body = mapper.createObjectNode();
        body.put("model", MODEL);
        body.put("max_tokens", MAX_TOKENS);
        body.put("system", systemPrompt);

        ArrayNode messages = mapper.createArrayNode();

        // Add only the last user message
        for (int i = history.size() - 1; i >= 0; i--) {
            Message m = history.get(i);
            if ("user".equals(m.getRole())) {
                ObjectNode userMsg = mapper.createObjectNode();
                userMsg.put("role", "user");
                userMsg.put("content", m.getContent());
                messages.add(userMsg);
                break;
            }
        }

        // Add assistant turn with tool_use block
        ObjectNode assistantTurn = mapper.createObjectNode();
        assistantTurn.put("role", "assistant");
        assistantTurn.set("content", assistantMessage.path("content"));
        messages.add(assistantTurn);

        // Add tool_result turn
        ObjectNode toolResultBlock = mapper.createObjectNode();
        toolResultBlock.put("type", "tool_result");
        toolResultBlock.put("tool_use_id", toolUseId);
        toolResultBlock.put("content", toolResult);

        ArrayNode toolResultContent = mapper.createArrayNode();
        toolResultContent.add(toolResultBlock);

        ObjectNode userTurn = mapper.createObjectNode();
        userTurn.put("role", "user");
        userTurn.set("content", toolResultContent);
        messages.add(userTurn);

        body.set("messages", messages);

        // Add tools
        ArrayNode toolsNode = mapper.createArrayNode();
        for (Map<String, Object> tool : tools) {
            toolsNode.add(mapper.valueToTree(tool));
        }
        body.set("tools", toolsNode);

        JsonNode root = mapper.readTree(post(body));
        for (JsonNode block : root.path("content")) {
            if ("text".equals(block.path("type").asText())) {
                return block.path("text").asText();
            }
        }
        throw new IOException("No text block after tool result: " + root);
    }
    private ObjectNode buildBaseBody(String systemPrompt, List<Message> history) {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", MODEL);
        body.put("max_tokens", MAX_TOKENS);
        body.put("system", systemPrompt);

        ArrayNode messages = mapper.createArrayNode();
        for (Message m : history) {
            String content = m.getContent();
            if (content == null || content.isBlank()) continue;
            // Skip any messages that look like raw JSON tool responses
            // (these are internal API artifacts, not real conversation turns)
            if (content.trim().startsWith("{") || content.trim().startsWith("[")) continue;

            ObjectNode msg = mapper.createObjectNode();
            msg.put("role", m.getRole());
            msg.put("content", content);
            messages.add(msg);
        }
        body.set("messages", messages);
        return body;
    }
    
    private String post(ObjectNode body) throws IOException {
        RequestBody requestBody = RequestBody.create(
                mapper.writeValueAsString(body), JSON_TYPE);

        Request request = new Request.Builder()
                .url(API_URL)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = http.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("API error " + response.code() + ": " + responseBody);
            }
            return responseBody;
        }
    }
}
