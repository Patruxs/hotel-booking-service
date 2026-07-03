package org.example.hotelbookingservice.services;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface IFileStorageService {
    String uploadFile(MultipartFile file);
    default String uploadFile(MultipartFile file, String publicId) {
        return uploadFile(file);
    }
    List<String> uploadFiles(List<MultipartFile> files);
    void validateImageFile(MultipartFile file);
    void validateImageFiles(List<MultipartFile> files);
    default void deleteFile(String publicId) {
    }
}
