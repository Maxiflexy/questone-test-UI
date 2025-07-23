package com.fundquest.auth.service.impl;

import com.fundquest.auth.constants.AppConstants;
import com.fundquest.auth.exception.MicrosoftOAuthException;
import com.fundquest.auth.service.MicrosoftOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MicrosoftOAuthServiceImpl implements MicrosoftOAuthService {

    private final WebClient webClient;

    @Value("${azure.client-id}")
    private String clientId;

    @Value("${azure.client-secret}")
    private String clientSecret;

    @Value("${azure.tenant-id}")
    private String tenantId;

    @Value("${azure.redirect-uri}")
    private String redirectUri;

    @Override
    public JSONObject exchangeCodeForToken(String authCode) {

        String tokenUrl = AppConstants.MICROSOFT_TOKEN_URL.replace("{tenantId}", tenantId);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", AppConstants.GRANT_TYPE_AUTH_CODE);
        params.add("code", authCode);
        params.add("redirect_uri", redirectUri);
        params.add("client_id", clientId);

        if (clientSecret != null && !clientSecret.trim().isEmpty()) {
            params.add("client_secret", clientSecret);
        }

        try {
            log.debug("Exchanging authorization code for Microsoft tokens");

            String response = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(params)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.trim().isEmpty()) {
                throw new MicrosoftOAuthException("Empty response from Microsoft token endpoint");
            }

            JSONObject jsonResponse = new JSONObject(response);
            log.debug("Successfully exchanged authorization code for tokens");

            return jsonResponse;

        } catch (WebClientResponseException e) {
            log.error("Microsoft token exchange failed. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new MicrosoftOAuthException("Invalid authorization code", AppConstants.INVALID_AUTH_CODE);
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new MicrosoftOAuthException("Authorization code expired", AppConstants.AUTH_CODE_EXPIRED);
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new MicrosoftOAuthException("Invalid tenant", AppConstants.INVALID_TENANT);
            }

            throw new MicrosoftOAuthException("Microsoft OAuth service error: " + e.getStatusCode());

        } catch (Exception e) {
            log.error("Error during Microsoft token exchange", e);
            throw new MicrosoftOAuthException("Unexpected error during token exchange: " + e.getMessage());
        }
    }
}