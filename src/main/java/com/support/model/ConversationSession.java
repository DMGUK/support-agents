package com.support.model;

import java.util.ArrayList;
import java.util.List;

public class ConversationSession {
    private final List<Message> history = new ArrayList<>(); 
    private AgentType currentAgent = null;

    public void addMessage(String role, String content){
        history.add(new Message(role, content));
    }

    public List<Message> getHistory(){
        return history;
    }

    public AgentType getCurrentAgent(){
        return currentAgent;
    }

    public void setCurrentAgent(AgentType agent){
        this.currentAgent = agent;
    }
}
