package com.srg.smartexpenseapi.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Service to fetch real-time market data (Gold, Silver, Stocks).
 * Currently simulated for demonstration purposes.
 * Can be connected to GoldAPI.io or Alpha Vantage.
 */
@Service
public class MarketDataService {

    private final Random random = new Random();
    
    // Simplistic cache to avoid wildly fluctuating prices in a single session
    private final Map<String, Double> priceCache = new HashMap<>();

    public Double getLivePrice(String assetType) {
        String key = assetType.toUpperCase();
        
        // Return cached price if fetched within specific timeframe (simulated)
        if (priceCache.containsKey(key) && random.nextDouble() > 0.1) {
            return priceCache.get(key);
        }

        double price;
        switch (key) {
            case "GOLD":
                price = 62000.0 + (random.nextDouble() * 5000.0); // Price per 10g
                break;
            case "SILVER":
                price = 72000.0 + (random.nextDouble() * 3000.0); // Price per kg
                break;
            case "STOCKS":
                price = 500.0 + (random.nextDouble() * 1000.0); // Generic index factor
                break;
            default:
                price = 100.0;
        }

        priceCache.put(key, price);
        return price;
    }

    /**
     * Estimates current value of an asset based on purchase price and time (simulated ROI).
     */
    public Double estimateCurrentValue(String assetType, Double purchasePrice) {
        Double liveFactor = getLivePrice(assetType);
        
        // This is a simplified simulation of "Market Value"
        // In a real app, this would fetch the actual ticker price.
        if (assetType.equalsIgnoreCase("GOLD")) {
            return purchasePrice * (liveFactor / 62000.0);
        } else if (assetType.equalsIgnoreCase("STOCKS")) {
            return purchasePrice * (liveFactor / 500.0);
        }
        
        return purchasePrice * 1.05; // Default 5% annual growth simulation
    }
}
