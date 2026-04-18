package com.srg.smartexpenseapi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class DocumentStorageService {

    private final Path rootPath = Paths.get("uploads/tax_proofs");

    public DocumentStorageService() {
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage folder", e);
        }
    }

    public String storeFile(MultipartFile file, Long userId) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
        String uniqueFileName = UUID.randomUUID().toString() + "_" + userId + extension;

        Path targetLocation = rootPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return targetLocation.toString();
    }

    public Path loadFile(String filePath) {
        return Paths.get(filePath);
    }
    
    public void deleteFile(String filePath) throws IOException {
        Files.deleteIfExists(Paths.get(filePath));
    }
}
