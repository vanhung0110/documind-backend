package com.documindai.service.impl;

import com.documindai.model.Document;
import com.documindai.model.DocumentChunk;
import com.documindai.repository.DocumentChunkRepository;
import com.documindai.service.DocumentChunkingService;
import com.documindai.utils.DocumentProcessor;
import com.documindai.utils.EmbeddingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của DocumentChunkingService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentChunkingServiceImpl implements DocumentChunkingService {

    private final DocumentChunkRepository chunkRepository;
    private final DocumentProcessor documentProcessor;

    @Value("${app.document.chunk.size:1000}")
    private int chunkSize;

    @Value("${app.document.chunk.overlap:200}")
    private int chunkOverlap;

    @Override
    @Transactional
    public List<DocumentChunk> chunkDocument(Document document, String extractedText) {
        log.info("Chunking document: {} (ID: {})", document.getFilename(), document.getId());

        // Chia text thành chunks
        List<String> textChunks = documentProcessor.splitTextIntoChunks(
                extractedText,
                chunkSize,
                chunkOverlap);

        log.info("Created {} chunks for document {}", textChunks.size(), document.getId());

        List<DocumentChunk> chunks = new ArrayList<>();
        int position = 0;

        for (int i = 0; i < textChunks.size(); i++) {
            String chunkText = textChunks.get(i);

            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocument(document);
            chunk.setChunkIndex(i);
            chunk.setContent(chunkText);
            chunk.setStartPosition(position);
            chunk.setEndPosition(position + chunkText.length());
            chunk.setTokenCount(documentProcessor.estimateTokens(chunkText));

            chunks.add(chunk);
            position += chunkText.length() - chunkOverlap;
        }

        // Lưu tất cả chunks vào database
        List<DocumentChunk> savedChunks = chunkRepository.saveAll(chunks);
        log.info("Saved {} chunks to database", savedChunks.size());

        return savedChunks;
    }

    @Override
    public List<DocumentChunk> getDocumentChunks(Long documentId) {
        return chunkRepository.findByDocumentIdOrderByChunkIndexAsc(documentId);
    }

    @Override
    @Transactional
    public void deleteDocumentChunks(Long documentId) {
        log.info("Deleting chunks for document ID: {}", documentId);
        chunkRepository.deleteByDocumentId(documentId);
    }

    @Override
    @Transactional
    public List<DocumentChunk> saveChunks(List<DocumentChunk> chunks) {
        return chunkRepository.saveAll(chunks);
    }

    @Override
    public List<DocumentChunk> findSimilarChunks(String queryEmbedding, int limit, double threshold) {
        // Lấy tất cả chunks có embedding
        List<DocumentChunk> allChunks = chunkRepository.findAllWithEmbeddings();

        log.info("Total chunks with embeddings from DB: {}", allChunks.size());
        
        if (allChunks.isEmpty()) {
            log.warn("No chunks with embeddings found");
            return new ArrayList<>();
        }

        // Log first chunk for debugging
        if (!allChunks.isEmpty()) {
            DocumentChunk first = allChunks.get(0);
            log.info("First chunk ID: {}, Doc ID: {}, Embedding length: {}", 
                first.getId(), first.getDocument().getId(), 
                first.getEmbedding() != null ? first.getEmbedding().length() : 0);
            
            // Calculate similarity for first chunk to debug
            double firstSim = EmbeddingUtils.calculateSimilarity(queryEmbedding, first.getEmbedding());
            log.info("Query embedding length: {}, First chunk similarity: {}", 
                queryEmbedding != null ? queryEmbedding.length() : 0, firstSim);
        }

        // Tính similarity cho mỗi chunk
        List<ChunkWithSimilarity> chunksWithSimilarity = allChunks.stream()
                .map(chunk -> {
                    double similarity = EmbeddingUtils.calculateSimilarity(
                            queryEmbedding,
                            chunk.getEmbedding());
                    if (similarity > 0.3) {
                        log.debug("Chunk {} similarity: {}", chunk.getId(), similarity);
                    }
                    return new ChunkWithSimilarity(chunk, similarity);
                })
                .filter(cws -> cws.similarity >= threshold)
                .sorted(Comparator.comparingDouble(ChunkWithSimilarity::similarity).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        log.info("Found {} similar chunks above threshold {}", chunksWithSimilarity.size(), threshold);

        return chunksWithSimilarity.stream()
                .map(ChunkWithSimilarity::chunk)
                .collect(Collectors.toList());
    }

    /**
     * Helper record để lưu chunk với similarity score
     */
    private record ChunkWithSimilarity(DocumentChunk chunk, double similarity) {
    }
}
