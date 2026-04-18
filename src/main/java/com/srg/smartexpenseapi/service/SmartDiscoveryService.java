package com.srg.smartexpenseapi.service;

import com.srg.smartexpenseapi.payload.response.DiscoveryResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmartDiscoveryService {

    private static final Map<String, String> CATEGORY_MAP = new HashMap<>();
    private static final Map<String, Boolean> TAX_MAP = new HashMap<>();

    static {
        // Food & Dining
        CATEGORY_MAP.put("zomato", "FOOD_DINING");
        CATEGORY_MAP.put("swiggy", "FOOD_DINING");
        CATEGORY_MAP.put("starbucks", "FOOD_DINING");
        CATEGORY_MAP.put("restaurant", "FOOD_DINING");
        CATEGORY_MAP.put("lunch", "FOOD_DINING");
        CATEGORY_MAP.put("dinner", "FOOD_DINING");

        // Transportation
        CATEGORY_MAP.put("uber", "TRANSPORTATION");
        CATEGORY_MAP.put("ola", "TRANSPORTATION");
        CATEGORY_MAP.put("petrol", "TRANSPORTATION");
        CATEGORY_MAP.put("diesel", "TRANSPORTATION");
        CATEGORY_MAP.put("fuel", "TRANSPORTATION");

        // Shopping
        CATEGORY_MAP.put("amazon", "SHOPPING");
        CATEGORY_MAP.put("flipkart", "SHOPPING");
        CATEGORY_MAP.put("myntra", "SHOPPING");
        CATEGORY_MAP.put("ajio", "SHOPPING");

        // Utilities
        CATEGORY_MAP.put("electricity", "UTILITIES");
        CATEGORY_MAP.put("water bill", "UTILITIES");
        CATEGORY_MAP.put("recharge", "UTILITIES");
        CATEGORY_MAP.put("jio", "UTILITIES");
        CATEGORY_MAP.put("airtel", "UTILITIES");

        // Tax Savings (80C / 80D / 80G)
        TAX_MAP.put("lic", true);
        TAX_MAP.put("insurance", true);
        TAX_MAP.put("policy", true);
        TAX_MAP.put("elss", true);
        TAX_MAP.put("mutual fund", true);
        TAX_MAP.put("tuition fee", true);
        TAX_MAP.put("donation", true);
        TAX_MAP.put("charity", true);
        TAX_MAP.put("hospital", true);
        TAX_MAP.put("medical", true);
        TAX_MAP.put("health", true);
        TAX_MAP.put("rent", true); // Section 80GG or HRA
    }

    public DiscoveryResponse discover(String description) {
        if (description == null || description.trim().isEmpty()) {
            return DiscoveryResponse.builder()
                    .category("MISCELLANEOUS")
                    .isTaxDeductible(false)
                    .confidence("LOW")
                    .message("No matching category found.")
                    .build();
        }

        String input = description.trim().toLowerCase();
        String discoveredCategory = "MISCELLANEOUS";
        boolean taxPossible = false;
        String confidence = "LOW";

        // Try to find category
        for (Map.Entry<String, String> entry : CATEGORY_MAP.entrySet()) {
            if (input.contains(entry.getKey())) {
                discoveredCategory = entry.getValue();
                confidence = "HIGH";
                break;
            }
        }

        // Try to find tax benefit
        for (Map.Entry<String, Boolean> entry : TAX_MAP.entrySet()) {
            if (input.contains(entry.getKey())) {
                taxPossible = true;
                confidence = "HIGH";
                break;
            }
        }

        // Specific category-based tax logic
        if (discoveredCategory.equals("HEALTH_FITNESS") || discoveredCategory.equals("EDUCATION")) {
            taxPossible = true;
            confidence = "HIGH";
        }

        return DiscoveryResponse.builder()
                .category(discoveredCategory)
                .isTaxDeductible(taxPossible)
                .confidence(confidence)
                .message("Discovered via Smart AI")
                .build();
    }
}
