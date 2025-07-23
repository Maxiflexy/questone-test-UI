package com.maxiflexy.keyvault_demo.service;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KeyVaultService {

    private final SecretClient secretClient;

    public KeyVaultService(@Value("${spring.cloud.azure.keyvault.secret.endpoint}") String keyVaultUrl) {
        this.secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }

    public String getSecret(String secretName) {
        try {
            return secretClient.getSecret(secretName).getValue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve secret: " + secretName, e);
        }
    }

    public Map<String, String> getAllConfiguredSecrets() {
        Map<String, String> secrets = new HashMap<>();

        try {
            // Get database password
            String dbPassword = getSecret("database-password");
            secrets.put("database-password", dbPassword);

            // Get JWT secret
            String jwtSecret = getSecret("jwtSecret");
            secrets.put("jwt-secret", jwtSecret);

            System.out.println("Successfully retrieved secrets from Azure Key Vault:");
            secrets.forEach((key, value) ->
                    System.out.println("Secret: " + key + " = " + value)
            );

        } catch (Exception e) {
            System.err.println("Error retrieving secrets: " + e.getMessage());
            throw e;
        }

        return secrets;
    }
}