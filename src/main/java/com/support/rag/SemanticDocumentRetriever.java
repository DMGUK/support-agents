package com.support.rag;

import java.io.IOException;
import java.util.*;

public class SemanticDocumentRetriever {

    private final List<DocumentChunk> chunks;
    private final List<float[]> chunkVectors;
    private final EmbeddingClient embeddingClient;

    public SemanticDocumentRetriever(List<DocumentChunk> chunks,
                                      EmbeddingClient embeddingClient) throws IOException {
        this.chunks          = chunks;
        this.embeddingClient = embeddingClient;

        System.out.println("Computing embeddings for " + chunks.size() + " chunks...");
        List<String> texts = new ArrayList<>();
        for (DocumentChunk chunk : chunks) texts.add(chunk.getContent());
        this.chunkVectors = embeddingClient.embedBatch(texts);
        System.out.println("Embeddings ready.");
    }

    public List<DocumentChunk> retrieve(String query, int topK) throws IOException {
        float[] queryVector = embeddingClient.embed(query);

        List<Map.Entry<DocumentChunk, Double>> scored = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            double score = cosineSimilarity(queryVector, chunkVectors.get(i));
            scored.add(Map.entry(chunks.get(i), score));
        }

        scored.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<DocumentChunk> results = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, scored.size()); i++) {
            results.add(scored.get(i).getKey());
        }
        return results;
    }

    // cosine similarity = dot product / (magnitude A * magnitude B)
    private double cosineSimilarity(float[] a, float[] b) {
        double dotProduct = 0.0;
        double normA      = 0.0;
        double normB      = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA      += a[i] * a[i];
            normB      += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}