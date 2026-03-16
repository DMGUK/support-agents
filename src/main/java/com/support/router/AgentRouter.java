package com.support.router;

import com.support.llm.ClaudeClient;
import com.support.model.AgentType;
import com.support.model.Message;

import java.io.IOException;
import java.util.List;

public class AgentRouter {

    private final ClaudeClient client;

    private static final String SYSTEM_PROMPT = """
            You are a routing classifier for a customer support system.
            You will receive the conversation history and the latest user message.
            Output EXACTLY one of these three words based on the full context:
              TECHNICAL    — setup, configuration, integrations, APIs, errors, bugs.
              BILLING      — charges, invoices, plans, pricing, refunds, payments, customer IDs.
              OUT_OF_SCOPE — does not fit either category.
            IMPORTANT: Questions about API rate limits, endpoints, or technical documentation
            are TECHNICAL even if they mention plan names like Pro or Enterprise.
            IMPORTANT: If the user is continuing an existing conversation (e.g. providing
            a customer ID, answering a follow-up question), classify based on the topic
            of the ongoing conversation, not just the latest message alone.
            Output ONLY the single word. No punctuation. No explanation.
            """;

    public AgentRouter(ClaudeClient client) {
        this.client = client;
    }

    public AgentType route(String userMessage, List<Message> history) throws IOException {
        List<Message> context = history.size() > 4
                ? history.subList(history.size() - 4, history.size())
                : history;

        String raw = client.complete(SYSTEM_PROMPT, context).trim().toUpperCase();

        if (raw.contains("BILLING"))   return AgentType.BILLING;
        if (raw.contains("TECHNICAL")) return AgentType.TECHNICAL;
        return AgentType.OUT_OF_SCOPE;
    }
}