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
    
}
