package com.bfs.qualifier.client;

import com.bfs.qualifier.model.dto.FinalQueryPayload;
import com.bfs.qualifier.model.dto.GenerateWebhookRequest;
import com.bfs.qualifier.model.dto.GenerateWebhookResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class HiringClient {

    private final WebClient webClient = WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    private static final Retry RETRY_POLICY = Retry.backoff(3, Duration.ofSeconds(1))
            .maxBackoff(Duration.ofSeconds(5))
            .transientErrors(true);

    public GenerateWebhookResponse generateWebhook(String endpoint, GenerateWebhookRequest body) {
        log.info("Calling generateWebhook endpoint: {}", endpoint);
        return webClient.post()
                .uri(endpoint)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GenerateWebhookResponse.class)
                .retryWhen(RETRY_POLICY)
                .block();
    }

    public String submitFinalQuery(String webhookUrlOrFallback,
                                   String accessToken,
                                   FinalQueryPayload payload) {
        log.info("Submitting finalQuery to webhook: {}", webhookUrlOrFallback);

        return webClient.post()
                .uri(webhookUrlOrFallback)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(RETRY_POLICY)
                .block();
    }
}
