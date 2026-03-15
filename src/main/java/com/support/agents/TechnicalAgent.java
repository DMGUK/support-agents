package com.support.agents;

import com.support.llm.ClaudeClient;
import com.support.model.Message;
import com.support.rag.DocumentChunk;
import com.support.rag.DocumentRetriever;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TechnicalAgent {
    private static final int TOP_K = 3;

    private final ClaudeClient client;
    private final DocumentRetriever retriever;

    public TechnicalAgent(ClaudeClient client, DocumentRetriever retriever) {
        this.client    = client;
        this.retriever = retriever;
    }

    public String respond(String userMessage, List<Message> history) throws IOException {
        List<DocumentChunk> relevant = retriever.retrieve(userMessage, TOP_K);
        String systemPrompt = buildSystemPrompt(relevant);
        List<Message> turns = filterTurns(history);
        return client.complete(systemPrompt, turns);
    }

    private String buildSystemPrompt(List<DocumentChunk> docs) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a Technical Support Specialist.\n");
        sb.append("Answer ONLY using the documentation excerpts provided below.\n");
        sb.append("If the answer is not present in the excerpts:\n");
        sb.append("  - Ask the user for clarification, OR\n");
        sb.append("  - Clearly state that the information is not available.\n");
        sb.append("Do NOT guess, invent, or hallucinate any information.\n\n");

        if (docs.isEmpty()) {
            sb.append("No relevant documentation was found for this query.\n");
        } else {
            sb.append("--- RELEVANT DOCUMENTATION ---\n\n");
            for (DocumentChunk chunk : docs) {
                sb.append("[Source: ").append(chunk.getSource()).append("]\n");
                sb.append(chunk.getContent()).append("\n\n");
            }
            sb.append("--- END OF DOCUMENTATION ---\n");
        }
        return sb.toString();
    }

    private List<Message> filterTurns(List<Message> history) {
        List<Message> filtered = new ArrayList<>();
        for (Message m : history) {
            if ("user".equals(m.getRole()) || "assistant".equals(m.getRole())) {
                filtered.add(m);
            }
        }
        return filtered;
    }
}
