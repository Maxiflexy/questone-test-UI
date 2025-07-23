package com.fundquest.auth.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class WebClientConfig {


    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

//    /**
//     * Creates a configured WebClient bean for external API calls
//     *
//     * The codec configuration sets the maximum in-memory size for buffering HTTP message content.
//     * Default is 256KB, but we increase it to 1MB (1024 * 1024 bytes) to handle larger responses
//     * from Microsoft OAuth endpoints which might return substantial token responses or user data.
//     */
//    @Bean
//    public WebClient webClient() {
//        // Configure HTTP client with timeouts and connection settings
//        HttpClient httpClient = HttpClient.create()
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 10 seconds connection timeout
//                .responseTimeout(Duration.ofSeconds(30)) // 30 seconds response timeout
//                .doOnConnected(conn ->
//                        conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
//                                .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))
//                );
//
//        // Configure exchange strategies with increased buffer size
//        ExchangeStrategies strategies = ExchangeStrategies.builder()
//                .codecs(configurer -> {
//                    // Set max in-memory size to 1MB for handling large OAuth responses
//                    // This prevents DataBufferLimitException when Microsoft returns large token responses
//                    configurer.defaultCodecs().maxInMemorySize(1024 * 1024); // 1MB
//
//                    // Enable logging for debugging (can be removed in production)
//                    configurer.defaultCodecs().enableLoggingRequestDetails(true);
//                })
//                .build();
//
//        WebClient webClient = WebClient.builder()
//                .clientConnector(new ReactorClientHttpConnector(httpClient))
//                .exchangeStrategies(strategies)
//                .defaultHeader("User-Agent", "FundQuest-Auth-Service/1.0")
//                .build();
//
//        log.info("WebClient configured with 1MB buffer size and 30s timeouts");
//        return webClient;
//    }

//    /**
//     * Alternative WebClient bean for internal API calls with smaller buffer
//     * This can be used for lighter internal service communications
//     */
//    @Bean("internalWebClient")
//    public WebClient internalWebClient() {
//        ExchangeStrategies strategies = ExchangeStrategies.builder()
//                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(512 * 1024)) // 512KB
//                .build();
//
//        return WebClient.builder()
//                .exchangeStrategies(strategies)
//                .defaultHeader("User-Agent", "FundQuest-Auth-Internal/1.0")
//                .build();
//    }
}