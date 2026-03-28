package com.example.inventory_management.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.inventory_management.exception.BadRequestException;

@Service
public class ImageStorageService {

    private static final Path UPLOAD_ROOT = Paths.get("uploads");

    public String storeProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        String originalName = file.getOriginalFilename();
        String extension = getSafeExtension(originalName);
        String generatedName = UUID.randomUUID() + extension;

        try {
            Files.createDirectories(UPLOAD_ROOT);
            Path target = UPLOAD_ROOT.resolve(generatedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store image", ex);
        }

        return "/uploads/" + generatedName;
    }

    public String normalizeExternalImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        try {
            URI uri = new URI(imageUrl.trim());
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                throw new BadRequestException("Image URL must start with http or https");
            }
            return imageUrl.trim();
        } catch (URISyntaxException ex) {
            throw new BadRequestException("Invalid image URL format");
        }
    }

    private String getSafeExtension(String fileName) {
        if (fileName == null) {
            return ".png";
        }
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            return ".png";
        }
        String ext = fileName.substring(idx).toLowerCase();
        return ext.matches("\\.[a-z0-9]{1,6}") ? ext : ".png";
    }
}
