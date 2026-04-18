package com.srg.smartexpenseapi.controller;

import com.srg.smartexpenseapi.payload.response.DiscoveryResponse;
import com.srg.smartexpenseapi.service.SmartDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    @Autowired
    private SmartDiscoveryService discoveryService;

    @GetMapping("/suggest")
    public ResponseEntity<DiscoveryResponse> suggest(@RequestParam String description) {
        return ResponseEntity.ok(discoveryService.discover(description));
    }
}
