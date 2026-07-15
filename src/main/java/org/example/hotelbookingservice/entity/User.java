package org.example.hotelbookingservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "accounts")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 50)
    @NotNull
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Size(max = 50)
    @NotNull
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Size(max = 255)
    @Column(name = "password_hash")
    private String passwordHash;

    @Size(max = 320)
    @NotNull
    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Size(max = 32)
    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @NotNull
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Size(max = 32)
    @NotNull
    @Column(name = "auth_provider", nullable = false, length = 32)
    @Builder.Default
    private String provider = "LOCAL";

    @Column(name = "avatar_url")
    private String avatarUrl;

    @NotNull
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private Set<Booking> bookings = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private Set<Hotel> hotels = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private Set<Review> reviews = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new LinkedHashSet<>();

    @Transient
    private String fullName;

    public UUID getUuid() {
        return id;
    }

    public String getFullName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        String normalized = fullName == null ? "" : fullName.trim();
        int splitAt = normalized.indexOf(' ');
        if (splitAt < 0) {
            this.firstName = normalized;
            this.lastName = normalized;
            return;
        }
        this.firstName = normalized.substring(0, splitAt).trim();
        this.lastName = normalized.substring(splitAt + 1).trim();
    }

    public String getPassword() {
        return passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = password;
    }

    public LocalDate getDob() {
        return dateOfBirth;
    }

    public void setDob(LocalDate dob) {
        this.dateOfBirth = dob;
    }

    public Boolean getActivate() {
        return emailVerified;
    }

    public void setActivate(Boolean activate) {
        this.emailVerified = activate;
    }
}
