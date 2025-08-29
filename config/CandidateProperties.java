package com.bfs.qualifier.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.candidate")
public record CandidateProperties(
        @NotBlank String name,
        @NotBlank String regNo,
        @Email @NotBlank String email
) { }
