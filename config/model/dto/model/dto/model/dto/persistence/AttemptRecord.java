package com.bfs.qualifier.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "attempts")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttemptRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String regNo;
    private String webhookUrl;

    @Column(length = 4000)
    private String accessToken;

    @Column(length = 8000)
    private String finalQuery;

    private String submitStatus;
    private OffsetDateTime createdAt;
}
