package com.bfs.qualifier.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GenerateWebhookResponse(
        String webhook,
        String accessToken
) { }
