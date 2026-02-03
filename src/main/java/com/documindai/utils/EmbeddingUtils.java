package com.documindai.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Utility class cho xử lý embeddings
 */
@Slf4j
public class EmbeddingUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Tính cosine similarity giữa hai vectors
     * 
     * @param vector1 Vector thứ nhất
     * @param vector2 Vector thứ hai
     * @return Cosine similarity (0-1)
     */
    public static double cosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1 == null || vector2 == null || vector1.size() != vector2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += Math.pow(vector1.get(i), 2);
            norm2 += Math.pow(vector2.get(i), 2);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Serialize embedding vector thành JSON string
     */
    public static String serializeEmbedding(List<Double> embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (Exception e) {
            log.error("Error serializing embedding", e);
            return null;
        }
    }

    /**
     * Deserialize JSON string thành embedding vector
     */
    public static List<Double> deserializeEmbedding(String embeddingJson) {
        try {
            return objectMapper.readValue(embeddingJson, new TypeReference<List<Double>>() {
            });
        } catch (Exception e) {
            log.error("Error deserializing embedding", e);
            return null;
        }
    }

    /**
     * Tính độ tương đồng giữa query embedding và document embedding
     */
    public static double calculateSimilarity(String queryEmbeddingJson, String docEmbeddingJson) {
        List<Double> queryVector = deserializeEmbedding(queryEmbeddingJson);
        List<Double> docVector = deserializeEmbedding(docEmbeddingJson);

        if (queryVector == null || docVector == null) {
            return 0.0;
        }

        return cosineSimilarity(queryVector, docVector);
    }
}
