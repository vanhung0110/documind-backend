package com.documindai.utils;

import com.documindai.exception.BadRequestException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class để xử lý và extract text từ các loại document
 */
@Component
public class DocumentProcessor {

    /**
     * Extract text từ file dựa vào extension
     */
    public String extractText(File file, String extension) {
        try {
            return switch (extension.toLowerCase()) {
                case "pdf" -> extractTextFromPDF(file);
                case "docx" -> extractTextFromDOCX(file);
                case "doc" -> extractTextFromDOC(file);
                case "txt" -> extractTextFromTXT(file);
                default -> throw new BadRequestException("Định dạng file không được hỗ trợ: " + extension);
            };
        } catch (IOException e) {
            throw new BadRequestException("Không thể đọc nội dung file: " + e.getMessage());
        }
    }

    /**
     * Extract text từ PDF file
     */
    private String extractTextFromPDF(File file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return cleanText(text);
        }
    }

    /**
     * Extract text từ DOCX file (Word 2007+)
     */
    private String extractTextFromDOCX(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
                XWPFDocument document = new XWPFDocument(fis)) {

            StringBuilder text = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            for (XWPFParagraph paragraph : paragraphs) {
                text.append(paragraph.getText()).append("\n");
            }

            return cleanText(text.toString());
        }
    }

    /**
     * Extract text từ DOC file (Word 97-2003)
     */
    private String extractTextFromDOC(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
                HWPFDocument document = new HWPFDocument(fis)) {

            WordExtractor extractor = new WordExtractor(document);
            String text = extractor.getText();
            extractor.close();

            return cleanText(text);
        }
    }

    /**
     * Extract text từ TXT file
     */
    private String extractTextFromTXT(File file) throws IOException {
        String text = Files.readString(file.toPath());
        return cleanText(text);
    }

    /**
     * Clean và normalize text
     * Loại bỏ ký tự đặc biệt, khoảng trắng thừa, v.v.
     */
    private String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Loại bỏ các ký tự control characters
        text = text.replaceAll("\\p{C}", " ");

        // Thay thế nhiều khoảng trắng liên tiếp bằng 1 khoảng trắng
        text = text.replaceAll("\\s+", " ");

        // Loại bỏ khoảng trắng đầu cuối
        text = text.trim();

        return text;
    }

    /**
     * Chia text thành các chunks nhỏ hơn để xử lý
     * Sử dụng cho embedding và context window
     */
    public List<String> splitTextIntoChunks(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        // Ensure overlap is less than chunkSize to avoid infinite loop
        if (overlap >= chunkSize) {
            overlap = chunkSize / 2;
        }

        int textLength = text.length();
        int start = 0;

        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);

            // Tìm vị trí kết thúc câu gần nhất để tránh cắt giữa câu
            if (end < textLength) {
                int searchStart = Math.max(start, end - 200); // Search within last 200 chars
                int lastPeriod = text.lastIndexOf('.', end);
                int lastQuestion = text.lastIndexOf('?', end);
                int lastExclamation = text.lastIndexOf('!', end);

                int lastSentenceEnd = Math.max(lastPeriod, Math.max(lastQuestion, lastExclamation));

                // Only adjust if found within reasonable range
                if (lastSentenceEnd > searchStart && lastSentenceEnd > start) {
                    end = lastSentenceEnd + 1;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // Ensure we always make progress to avoid infinite loop
            int nextStart = end - overlap;
            if (nextStart <= start) {
                nextStart = start + Math.max(1, chunkSize / 2);
            }
            start = nextStart;
            
            // Safety check: limit max chunks to prevent memory issues
            if (chunks.size() > 10000) {
                break;
            }
        }

        return chunks;
    }

    /**
     * Tạo summary ngắn gọn từ text (lấy N câu đầu tiên)
     */
    public String generateQuickSummary(String text, int maxSentences) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Tách text thành các câu
        String[] sentences = text.split("[.!?]+");

        StringBuilder summary = new StringBuilder();
        int count = 0;

        for (String sentence : sentences) {
            if (count >= maxSentences) {
                break;
            }

            sentence = sentence.trim();
            if (!sentence.isEmpty()) {
                summary.append(sentence).append(". ");
                count++;
            }
        }

        return summary.toString().trim();
    }

    /**
     * Đếm số từ trong text
     */
    public int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    /**
     * Estimate số tokens (xấp xỉ: 1 token ≈ 4 characters)
     */
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        return text.length() / 4;
    }
}
