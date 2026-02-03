package com.documindai.utils;

import com.documindai.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * Utility class để validate file upload
 */
@Component
public class FileValidator {
    
    /**
     * Validate file upload
     * Kiểm tra: file không null, không rỗng, đúng extension, đúng size
     */
    public void validateFile(MultipartFile file) {
        // Kiểm tra file null hoặc rỗng
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File không được để trống");
        }
        
        // Kiểm tra tên file
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BadRequestException("Tên file không hợp lệ");
        }
        
        // Kiểm tra extension
        String extension = getFileExtension(originalFilename);
        if (!isValidExtension(extension)) {
            throw new BadRequestException(
                "File không đúng định dạng. Chỉ chấp nhận: " + 
                String.join(", ", Constants.ALLOWED_EXTENSIONS)
            );
        }
        
        // Kiểm tra kích thước file
        if (file.getSize() > Constants.MAX_FILE_SIZE) {
            throw new BadRequestException(
                "File vượt quá dung lượng cho phép (50MB). " +
                "Kích thước file: " + formatFileSize(file.getSize())
            );
        }
        
        // Kiểm tra content type
        String contentType = file.getContentType();
        if (contentType == null || !isValidContentType(contentType)) {
            throw new BadRequestException("Loại file không được hỗ trợ");
        }
    }
    
    /**
     * Lấy extension của file
     */
    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * Kiểm tra extension có hợp lệ không
     */
    public boolean isValidExtension(String extension) {
        return Arrays.asList(Constants.ALLOWED_EXTENSIONS).contains(extension.toLowerCase());
    }
    
    /**
     * Kiểm tra content type có hợp lệ không
     */
    private boolean isValidContentType(String contentType) {
        return contentType.equals("application/pdf") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
               contentType.equals("text/plain");
    }
    
    /**
     * Format file size thành dạng dễ đọc (KB, MB, GB)
     */
    public String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Tạo tên file unique để tránh trùng lặp
     */
    public String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String nameWithoutExtension = originalFilename.substring(0, 
            originalFilename.lastIndexOf('.'));
        
        // Loại bỏ ký tự đặc biệt
        nameWithoutExtension = nameWithoutExtension.replaceAll("[^a-zA-Z0-9-_]", "_");
        
        // Thêm timestamp để đảm bảo unique
        long timestamp = System.currentTimeMillis();
        
        return nameWithoutExtension + "_" + timestamp + "." + extension;
    }
}
