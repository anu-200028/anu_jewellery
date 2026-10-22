package com.example.anu_jewellery.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class MetalPriceService {

    public Map<String, BigDecimal> getCurrentRatesPerGram() {
        Map<String, BigDecimal> rates = new HashMap<>();
        
        try {
            // Live API URL (Replace with your valid key if using an external provider like MetalpriceAPI)
            String apiUrl = "https://api.metalpriceapi.com/v1/latest?api_key=YOUR_API_KEY&base=INR&currencies=XAU,XAG,XPT";
            
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
            
            if (response != null && response.containsKey("rates")) {
                Map<String, Double> apiRates = (Map<String, Double>) response.get("rates");
                
                // Conversion factor: 1 Troy Ounce = 31.1035 Grams
                double ouncesToGrams = 31.1035;
                
                double goldPerOz = apiRates.getOrDefault("INRXAU", 1.0 / apiRates.getOrDefault("XAU", 1.0));
                double silverPerOz = apiRates.getOrDefault("INRXAG", 1.0 / apiRates.getOrDefault("XAG", 1.0));
                double platinumPerOz = apiRates.getOrDefault("INRXPT", 1.0 / apiRates.getOrDefault("XPT", 1.0));

                // Directly calculating per gram values
                double gold24kPerGram = goldPerOz / ouncesToGrams;
                double gold22kPerGram = gold24kPerGram * (22.0 / 24.0); // 22K ratio
                double silverPerGram = silverPerOz / ouncesToGrams;
                double platinumPerGram = platinumPerOz / ouncesToGrams;

                rates.put("GOLD_24K", BigDecimal.valueOf(gold24kPerGram).setScale(2, RoundingMode.HALF_UP));
                rates.put("GOLD_22K", BigDecimal.valueOf(gold22kPerGram).setScale(2, RoundingMode.HALF_UP));
                rates.put("SILVER", BigDecimal.valueOf(silverPerGram).setScale(2, RoundingMode.HALF_UP));
                rates.put("PLATINUM", BigDecimal.valueOf(platinumPerGram).setScale(2, RoundingMode.HALF_UP));
                
                return rates;
            }
        } catch (Exception e) {
            System.err.println("API fetch failed, falling back to standard market estimates per gram: " + e.getMessage());
        }

        // Fallback current baseline rates per gram (INR) if API is unavailable
        rates.put("GOLD_24K", new BigDecimal("14957.00")); //
        rates.put("GOLD_22K", new BigDecimal("13710.00")); //
        rates.put("SILVER", new BigDecimal("221.00"));     //
        rates.put("PLATINUM", new BigDecimal("4200.00"));
        
        return rates;
    }
}