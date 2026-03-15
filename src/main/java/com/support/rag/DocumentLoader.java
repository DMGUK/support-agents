package com.support.rag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DocumentLoader {
    public List<DocumentChunk> loadAll(String docsDirectory) throws IOException {
    List<DocumentChunk> chunks = new ArrayList<>();
    Path dir = Paths.get(docsDirectory);

    if (!Files.exists(dir)) {
        System.err.println("WARNING: docs directory not found: " + docsDirectory);
        return chunks;
    }

    try (var stream = Files.walk(dir)) {
        stream.filter(p -> p.toString().endsWith(".md") || p.toString().endsWith(".txt"))
                .forEach(p -> {
                        try {
                            String content = Files.readString(p);
                            String filename = p.getFileName().toString();
                            String[] paragraphs = content.split("\\n\\n+");
                            for (String para : paragraphs) {
                                String trimmed = para.strip();
                                if (trimmed.length() > 30) {
                                    chunks.add(new DocumentChunk(filename, trimmed));
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Failed to read: " + p + " — " + e.getMessage());
                        }
                    });
        }
        System.out.println("Loaded " + chunks.size() + " chunks from " + docsDirectory);
        return chunks;
    }
}
