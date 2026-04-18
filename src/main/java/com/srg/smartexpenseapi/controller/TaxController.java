package com.srg.smartexpenseapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.srg.smartexpenseapi.security.services.UserDetailsImpl;
import com.srg.smartexpenseapi.service.TaxCalculationService;
import com.srg.smartexpenseapi.service.TaxRecommendationService;
import com.srg.smartexpenseapi.service.TaxReportPdfService;
import com.srg.smartexpenseapi.payload.response.TaxReportResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

@RestController
@RequestMapping("/api/tax")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaxController {

    @Autowired
    private TaxCalculationService taxService;

    @Autowired
    private TaxRecommendationService recommendationService;

    @Autowired
    private TaxReportPdfService pdfService;

    @GetMapping("/recommendation")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getRecommendation(@RequestParam(defaultValue = "2026") Integer year) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String recommendation = recommendationService.recommendItrForm(userDetails.getId(), year);
        
        Map<String, String> response = new HashMap<>();
        response.put("year", String.valueOf(year));
        response.put("recommendedItr", recommendation);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TaxReportResponse> getTaxReport(
            @RequestParam(defaultValue = "2026") Integer year,
            @RequestParam(defaultValue = "ITR-1") String itrType) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(taxService.calculateTaxForYear(userDetails.getId(), year, itrType));
    }
    @GetMapping("/report/download")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadTaxReport(
            @RequestParam(defaultValue = "2026") Integer year,
            @RequestParam(defaultValue = "ITR-1") String itrType) throws IOException {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TaxReportResponse report = taxService.calculateTaxForYear(userDetails.getId(), year, itrType);
        
        byte[] pdfContent = pdfService.generateTaxReportPdf(report);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=SmartExpense_Tax_Report_" + year + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }
}
