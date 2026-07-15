package org.example.hotelbookingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "news")
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_account_id")
    private User author;

    @Column(name = "author_account_id", insertable = false, updatable = false)
    private UUID authorAccountId;

    @Size(max = 180)
    @NotNull
    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Size(max = 220)
    @NotNull
    @Column(name = "slug", nullable = false, length = 220)
    private String slug;

    @Size(max = 500)
    @Column(name = "summary", length = 500)
    private String summary;

    @NotNull
    @Column(name = "content", nullable = false)
    private String content;

    @Size(max = 32)
    @NotNull
    @Column(name = "status", nullable = false, length = 32)
    private String status = "DRAFT";

    @Column(name = "published_at")
    private Instant publishedAt;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
