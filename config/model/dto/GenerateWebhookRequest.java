package com.bfs.qualifier.model.dto;

public record GenerateWebhookRequest(
        String name,
        String regNo,
        String email
) { }
