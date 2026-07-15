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
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_account_id", nullable = false)
    private User recipient;

    @Column(name = "recipient_account_id", insertable = false, updatable = false)
    private UUID recipientAccountId;

    @Size(max = 64)
    @NotNull
    @Column(name = "type", nullable = false, length = 64)
    private String type;

    @Size(max = 180)
    @NotNull
    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "body")
    private String body;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "read_at")
    private Instant readAt;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
