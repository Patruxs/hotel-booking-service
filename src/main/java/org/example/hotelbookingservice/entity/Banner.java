package org.example.hotelbookingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "banners")
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 180)
    @NotNull
    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Size(max = 300)
    @Column(name = "subtitle", length = 300)
    private String subtitle;

    @NotNull
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "link_url")
    private String linkUrl;

    @Size(max = 32)
    @NotNull
    @Column(name = "link_type", nullable = false, length = 32)
    private String linkType = "URL";

    @NotNull
    @Column(name = "position", nullable = false)
    private Integer position;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }
}
