package com.support.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.llm.ClaudeClient;
import com.support.model.Message;
import com.support.tools.BillingTools;

import java.io.IOException;
import java.util.*;

public class BillingAgent {

    private final ClaudeClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public BillingAgent(ClaudeClient client) {
        this.client = client;
    }

    public String respond(String userMessage, List<Message> history) throws IOException {
        List<Map<String, Object>> tools = buildToolDefinitions();
        String systemPrompt = buildSystemPrompt();
        List<Message> turns = filterTurns(history);

        // Call 1 — Claude decides which tool to use
        JsonNode response = client.completeWithTools(systemPrompt, turns, tools);
        String stopReason = response.path("stop_reason").asText();

        if ("tool_use".equals(stopReason)) {
            for (JsonNode block : response.path("content")) {
                if ("tool_use".equals(block.path("type").asText())) {
                    String toolName  = block.path("name").asText();
                    String toolUseId = block.path("id").asText();

                    Map<String, String> args = new HashMap<>();
                    block.path("input").fields()
                         .forEachRemaining(e -> args.put(e.getKey(), e.getValue().asText()));

                    System.out.println("  [tool_call] " + toolName + "(" + args + ")");
                    String toolResult = BillingTools.dispatch(toolName, args);

                    // Call 2 — feed result back, get final answer
                    return client.continueWithToolResult(
                            systemPrompt, turns, response, toolUseId, toolResult, tools);
                }
            }
        }

        // Claude answered directly without calling a tool
        for (JsonNode block : response.path("content")) {
            if ("text".equals(block.path("type").asText())) {
                return block.path("text").asText();
            }
        }
        throw new IOException("Unexpected BillingAgent response: " + response);
    }

    private List<Map<String, Object>> buildToolDefinitions() {
        return List.of(
            tool("get_plan_info",
                 "Retrieve the customer's current subscription plan, price, billing cycle, and status.",
                 Map.of("customer_id", param("string", "The customer's unique ID."))),

            tool("get_billing_history",
                 "Retrieve the last 3 invoices for a customer.",
                 Map.of("customer_id", param("string", "The customer's unique ID."))),

            tool("open_refund_request",
                 "Open a new refund request case for a customer.",
                 Map.of("customer_id", param("string", "The customer's unique ID."),
                        "reason",      param("string", "Reason for the refund."))),

            tool("send_refund_form",
                 "Send a refund request form to the customer's email.",
                 Map.of("customer_id", param("string", "The customer's unique ID."),
                        "email",       param("string", "Customer email address."))),

            tool("get_refund_policy",
                 "Return the refund policy including eligibility, timelines, and exceptions.",
                 Map.of())
        );
    }

    private Map<String, Object> tool(String name, String description,
                                      Map<String, Map<String, String>> properties) {
        Map<String, Object> inputSchema = new LinkedHashMap<>();
        inputSchema.put("type", "object");
        inputSchema.put("properties", properties);
        if (!properties.isEmpty()) inputSchema.put("required", new ArrayList<>(properties.keySet()));
        return Map.of("name", name, "description", description, "input_schema", inputSchema);
    }

    private Map<String, String> param(String type, String description) {
        return Map.of("type", type, "description", description);
    }

    private String buildSystemPrompt() {
        return """
               You are a Billing Specialist for a SaaS company.
               You handle questions about subscription plans, invoices, refunds, and billing history.
               Use the available tools to look up real data before answering.
               When a customer ID is needed and not provided, ask for it politely.
               Always be professional, concise, and empathetic.
               """;
    }

    private List<Message> filterTurns(List<Message> history) {
        List<Message> filtered = new ArrayList<>();
        for (Message m : history) {
            if ("user".equals(m.getRole()) || "assistant".equals(m.getRole())) {
                if (m.getContent() != null && !m.getContent().isBlank()) {
                    filtered.add(m);
                }
            }
        }
        // Keep only last 6 turns to avoid tool_use conflicts across turns
        if (filtered.size() > 6) {
            return filtered.subList(filtered.size() - 6, filtered.size());
        }
        return filtered;
    }
}