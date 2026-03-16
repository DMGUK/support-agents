package com.support;

import com.support.agents.BillingAgent;
import com.support.agents.TechnicalAgent;
import com.support.llm.ClaudeClient;
import com.support.model.AgentType;
import com.support.model.ConversationSession;
import com.support.rag.*;
import com.support.router.AgentRouter;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String OUT_OF_SCOPE_REPLY =
        "I'm sorry, but I cannot assist with that request. " +
        "Please contact our general support team at support@example.com.";

    public static void main(String[] args) throws IOException {
        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("ERROR: ANTHROPIC_API_KEY environment variable is not set.");
            System.exit(1);
        }

        String docsPath = args.length > 0 ? args[0] : "docs";

        ClaudeClient claude = new ClaudeClient(apiKey);
        List<DocumentChunk> chunks = new DocumentLoader().loadAll(docsPath);

        TechnicalAgent.Retriever retriever = buildRetriever(chunks);

        AgentRouter    router         = new AgentRouter(claude);
        TechnicalAgent technicalAgent = new TechnicalAgent(claude, retriever);
        BillingAgent   billingAgent   = new BillingAgent(claude);
        ConversationSession session   = new ConversationSession();

        System.out.println("=======================================================");
        System.out.println("  Conversational Support System — type 'exit' to quit  ");
        System.out.println("=======================================================\n");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You: ");
            String userInput = scanner.nextLine().trim();

            if (userInput.equalsIgnoreCase("exit") || userInput.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye!");
                scanner.close();
                break;
            }
            if (userInput.isEmpty()) continue;

            session.addMessage("user", userInput);

            try {
                AgentType routed = router.route(userInput, session.getHistory());
                AgentType agentType;

                if (routed == AgentType.OUT_OF_SCOPE && session.getCurrentAgent() != null) {
                    agentType = session.getCurrentAgent();
                } else {
                    agentType = routed;
                }

                if (agentType != session.getCurrentAgent() && agentType != AgentType.OUT_OF_SCOPE) {
                    String label = agentType == AgentType.TECHNICAL
                            ? "Technical Specialist" : "Billing Specialist";
                    System.out.println("  [Routing to " + label + "]");
                    session.setCurrentAgent(agentType);
                }

                String reply = switch (agentType) {
                    case TECHNICAL    -> technicalAgent.respond(userInput, session.getHistory());
                    case BILLING      -> billingAgent.respond(userInput, session.getHistory());
                    case OUT_OF_SCOPE -> {
                        session.setCurrentAgent(null);
                        yield OUT_OF_SCOPE_REPLY;
                    }
                };

                System.out.println("\nAgent: " + reply + "\n");
                session.addMessage("assistant", reply);

            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
                System.out.println("Agent: I'm experiencing a technical issue. Please try again.\n");
            }
        }
    }

    private static TechnicalAgent.Retriever buildRetriever(List<DocumentChunk> chunks) {
        try {
            DJLEmbeddingClient djlClient = new DJLEmbeddingClient();
            DJLSemanticDocumentRetriever djlRetriever =
                    new DJLSemanticDocumentRetriever(chunks, djlClient);
            System.out.println("[Retriever] Semantic search enabled (DJL local embeddings)");
            return djlRetriever::retrieve;
        } catch (Exception e) {
            System.err.println("[Retriever] DJL failed to load: " + e.getMessage());
            System.err.println("[Retriever] Falling back to keyword search.");
            DocumentRetriever keyword = new DocumentRetriever(chunks);
            return keyword::retrieve;
        }
    }
}