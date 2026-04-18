package com.srg.smartexpenseapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.srg.smartexpenseapi.security.services.UserDetailsImpl;
import com.srg.smartexpenseapi.service.AnalyticsService;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSummary() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(analyticsService.getUserFinancialSummary(userDetails.getId()));
    }

    @PostMapping("/snapshot")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> saveSnapshot(@RequestParam Integer month, @RequestParam Integer year) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(analyticsService.saveMonthlySnapshot(userDetails.getId(), month, year));
    }
}
