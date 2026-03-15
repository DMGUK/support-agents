package com.support.rag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DocumentRetriever {
    private final List<DocumentChunk> chunks;
    private static final Set<String> STOP_WORDS = Set.of(
        "i", "me", "my", "a", "an", "the", "is", "it", "in", "on", "at",
        "to", "do", "for", "of", "and", "or", "with", "how", "why", "what",
        "can", "get", "have", "be", "was", "are", "that", "this", "not"
    );

    public DocumentRetriever(List<DocumentChunk> chunks) {
        this.chunks = chunks;
    }

    public List<DocumentChunk> retrieve(String query, int topK) {
        Set<String> queryWords = tokenize(query);

        List<Map.Entry<DocumentChunk, Double>> scored = new ArrayList<>();
        for (DocumentChunk chunk : chunks) {
            Set<String> chunkWords = tokenize(chunk.getContent());
            double score = overlap(queryWords, chunkWords);
            if (score > 0) scored.add(Map.entry(chunk, score));
        }

        scored.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<DocumentChunk> results = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, scored.size()); i++) {
            results.add(scored.get(i).getKey());
        }
        return results;
    }

    private Set<String> tokenize(String text) {
        Set<String> words = new HashSet<>();
        for (String w : text.toLowerCase().split("[^a-z0-9]+")) {
            if (!w.isEmpty() && !STOP_WORDS.contains(w)) words.add(w);
        }
        return words;
    }

    // score = |intersection| / |query words|
    private double overlap(Set<String> query, Set<String> doc) {
        if (query.isEmpty()) return 0;
        long common = query.stream().filter(doc::contains).count();
        return (double) common / query.size();
    }
}
