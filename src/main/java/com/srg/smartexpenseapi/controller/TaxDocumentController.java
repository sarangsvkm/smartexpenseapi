package com.srg.smartexpenseapi.controller;

import com.srg.smartexpenseapi.entity.TaxInvestmentDoc;
import com.srg.smartexpenseapi.repository.TaxInvestmentDocRepository;
import com.srg.smartexpenseapi.repository.UserRepository;
import com.srg.smartexpenseapi.security.services.UserDetailsImpl;
import com.srg.smartexpenseapi.service.DocumentStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/tax/documents")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaxDocumentController {

    @Autowired
    private DocumentStorageService storageService;

    @Autowired
    private TaxInvestmentDocRepository docRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam("amount") Double amount,
            @RequestParam("fiscalYear") Integer fiscalYear) throws IOException {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        String storagePath = storageService.storeFile(file, userDetails.getId());

        TaxInvestmentDoc doc = TaxInvestmentDoc.builder()
                .user(userRepository.findById(userDetails.getId()).get())
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .storagePath(storagePath)
                .category(category)
                .amount(amount)
                .fiscalYear(fiscalYear)
                .build();

        docRepository.save(doc);

        return ResponseEntity.ok("Document uploaded successfully: " + file.getOriginalFilename());
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<TaxInvestmentDoc> getUserDocuments(@RequestParam(required = false) Integer year) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (year != null) {
            return docRepository.findByUserIdAndFiscalYear(userDetails.getId(), year);
        }
        return docRepository.findByUserId(userDetails.getId());
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) throws IOException {
        TaxInvestmentDoc doc = docRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        // Security check: Ensure document belongs to user
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!doc.getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).build();
        }

        Path file = storageService.loadFile(doc.getStoragePath());
        Resource resource = new UrlResource(file.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) throws IOException {
        TaxInvestmentDoc doc = docRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!doc.getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).build();
        }

        storageService.deleteFile(doc.getStoragePath());
        docRepository.delete(doc);
        
        return ResponseEntity.ok("Document deleted successfully");
    }
}
