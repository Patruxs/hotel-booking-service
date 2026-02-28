package org.example.hotelbookingservice.services;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface IFileStorageService {
    String uploadFile(MultipartFile file);
    List<String> uploadFiles(List<MultipartFile> files);
    void validateImageFile(MultipartFile file);
    void validateImageFiles(List<MultipartFile> files);
}
