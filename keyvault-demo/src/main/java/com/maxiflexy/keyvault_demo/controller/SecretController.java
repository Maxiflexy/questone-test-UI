package com.maxiflexy.keyvault_demo.controller;

import com.maxiflexy.keyvault_demo.service.KeyVaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SecretController {

    @Autowired
    private KeyVaultService keyVaultService;

    @GetMapping("/secrets")
    public ResponseEntity<Map<String, Object>> getAllSecrets() {
        try {
            Map<String, String> secrets = keyVaultService.getAllConfiguredSecrets();

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Secrets retrieved successfully from Azure Key Vault",
                    "secrets", secrets,
                    "count", secrets.size()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", "Failed to retrieve secrets: " + e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/secrets/{secretName}")
    public ResponseEntity<Map<String, Object>> getSecret(@PathVariable String secretName) {
        try {
            String secretValue = keyVaultService.getSecret(secretName);

            System.out.println("Retrieved secret '" + secretName + "' from Azure Key Vault: " + secretValue);

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Secret '" + secretName + "' retrieved successfully",
                    "secretName", secretName,
                    "secretValue", secretValue
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", "Failed to retrieve secret '" + secretName + "': " + e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}