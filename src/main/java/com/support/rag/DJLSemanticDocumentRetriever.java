package com.support.rag;

import java.util.*;

public class DJLSemanticDocumentRetriever {

    private final List<DocumentChunk> chunks;
    private final List<float[]> chunkVectors;
    private final DJLEmbeddingClient embeddingClient;

    public DJLSemanticDocumentRetriever(List<DocumentChunk> chunks,
                                         DJLEmbeddingClient embeddingClient) throws Exception {
        this.chunks          = chunks;
        this.embeddingClient = embeddingClient;

        System.out.println("Computing embeddings for " + chunks.size() + " chunks...");
        this.chunkVectors = new ArrayList<>();
        for (DocumentChunk chunk : chunks) {
            chunkVectors.add(embeddingClient.embed(chunk.getContent()));
        }
        System.out.println("Embeddings ready.");
    }

    public List<DocumentChunk> retrieve(String query, int topK) throws Exception {
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