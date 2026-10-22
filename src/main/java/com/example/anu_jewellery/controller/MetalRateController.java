package com.example.anu_jewellery.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metals")
public class MetalRateController {

    private final RestTemplate restTemplate;

    public MetalRateController() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/rates")
    public Map<String, Object> getLiveMetalRates() {
        Map<String, Object> rates = new HashMap<>();

        try {
            String url = "https://data-asg.goldprice.org/dbXRates/INR";

            // Add standard browser headers to prevent 403 Forbidden blocks
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "application/json, text/plain, */*");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
                if (!items.isEmpty()) {
                    Map<String, Object> data = items.get(0);

                    // Conversion: 1 Troy Ounce = 31.1035 Grams
                    double ounce24k = Double.parseDouble(data.get("xauPrice").toString());
                    double gram24k = ounce24k / 31.1035;

                    rates.put("GOLD_24K", Math.round(gram24k));
                    rates.put("GOLD_22K", Math.round(gram24k * 0.916)); // 22K Purity Ratio
                    rates.put("GOLD_18K", Math.round(gram24k * 0.750)); // 18K Purity Ratio

                    double ounceSilver = Double.parseDouble(data.get("xagPrice").toString());
                    rates.put("SILVER", Math.round(ounceSilver / 31.1035));

                    rates.put("PLATINUM", 5163); // Fallback estimate for platinum
                    
                    return rates; // Return successfully fetched live rates
                }
            }
        } catch (Exception e) {
            System.err.println("Live fetch failed, falling back to static rates: " + e.getMessage());
        }

        // Fallback rates if network drops or request gets throttled
        rates.put("GOLD_24K", 14790);
        rates.put("GOLD_22K", 13546);
        rates.put("GOLD_18K", 11090);
        rates.put("SILVER", 235);
        rates.put("PLATINUM", 5163);

        return rates;
    }
}