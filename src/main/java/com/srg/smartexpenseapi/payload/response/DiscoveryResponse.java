package com.srg.smartexpenseapi.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscoveryResponse {
    private String category;
    private Boolean isTaxDeductible;
    private String confidence; // "HIGH", "MEDIUM", "LOW"
    private String message;
}
