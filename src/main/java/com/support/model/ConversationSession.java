package com.support.model;

import java.util.ArrayList;
import java.util.List;

public class ConversationSession {
    private final List<Message> history = new ArrayList<>();
    private AgentType currentAgent = null;

    public void addMessage(String role, String content) {
        history.add(new Message(role, content));
    }

    // Returns only clean user/assistant text turns safe to send to any agent
    public List<Message> getHistory() {
        List<Message> clean = new ArrayList<>();
        for (Message m : history) {
            if ("user".equals(m.getRole()) || "assistant".equals(m.getRole())) {
                String content = m.getContent();
                if (content != null && !content.isBlank()
                        && !content.trim().startsWith("{")
                        && !content.trim().startsWith("[")) {
                    clean.add(m);
                }
            }
        }
        return clean;
    }

    // Returns full unfiltered history
    public List<Message> getRawHistory() {
        return history;
    }

    public AgentType getCurrentAgent()           { return currentAgent; }
    public void setCurrentAgent(AgentType agent) { this.currentAgent = agent; }
}