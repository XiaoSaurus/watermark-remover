package com.watermark.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @Entity @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "avatar")
public class Avatar {
    @Id
    private Long id;

    @Column(length = 512, nullable = false)
    private String url;

    @Column(length = 50)
    private String category;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
