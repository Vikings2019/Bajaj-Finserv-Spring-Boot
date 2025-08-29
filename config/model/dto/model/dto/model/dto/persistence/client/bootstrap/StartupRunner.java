package com.bfs.qualifier.bootstrap;

import com.bfs.qualifier.client.HiringClient;
import com.bfs.qualifier.config.CandidateProperties;
import com.bfs.qualifier.model.dto.FinalQueryPayload;
import com.bfs.qualifier.model.dto.GenerateWebhookRequest;
import com.bfs.qualifier.model.dto.GenerateWebhookResponse;
import com.bfs.qualifier.persistence.AttemptRecord;
import com.bfs.qualifier.persistence.AttemptRepository;
import com.bfs.qualifier.service.SqlSolverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupRunner {

    private final CandidateProperties candidate;
    private final SqlSolverService sqlSolver;
    private final HiringClient client;
    private final AttemptRepository repo;

    @Value("${app.endpoints.generateWebhook}")
    private String generateWebhookEndpoint;

    @Value("${app.endpoints.fallbackSubmit}")
    private String fallbackSubmitEndpoint;

    @Bean
    public ApplicationRunner runOnStartup() {
        return args -> {
            log.info("=== BFS Health Qualifier | Bootstrapping ===");
            log.info("Candidate: name={}, regNo={}, email={}", candidate.name(), candidate.regNo(), candidate.email());

            // 1) Generate webhook + JWT token
            var req = new GenerateWebhookRequest(candidate.name(), candidate.regNo(), candidate.email());
            GenerateWebhookResponse resp = client.generateWebhook(generateWebhookEndpoint, req);
            if (resp == null) {
                throw new IllegalStateException("generateWebhook returned null response");
            }
            String webhook = Optional.ofNullable(resp.webhook()).orElse(fallbackSubmitEndpoint);
            String accessToken = Optional.ofNullable(resp.accessToken())
                    .orElseThrow(() -> new IllegalStateException("No accessToken in response"));

            log.info("Received webhook: {}", webhook);
            log.info("Received accessToken: <redacted>");

            // 2) Decide question by regNo parity and load SQL from resources
            String finalSql = sqlSolver.resolveSqlByRegNo(candidate.regNo());
            log.info("Loaded final SQL ({} chars).", finalSql.length());

            // 3) Persist the attempt BEFORE submission
            AttemptRecord record = AttemptRecord.builder()
                    .regNo(candidate.regNo())
                    .webhookUrl(webhook)
                    .accessToken(accessToken)
                    .finalQuery(finalSql)
                    .submitStatus("PENDING")
                    .createdAt(OffsetDateTime.now())
                    .build();
            record = repo.save(record);

            // 4) Submit finalQuery with JWT
            String responseBody = client.submitFinalQuery(webhook, accessToken, new FinalQueryPayload(finalSql));
            log.info("Submission response: {}", responseBody);

            // 5) Update attempt status
            record.setSubmitStatus("SUBMITTED_OK");
            repo.save(record);

            log.info("=== Completed submission for regNo={} ===", candidate.regNo());
        };
    }
}
