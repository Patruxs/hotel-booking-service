package org.example.hotelbookingservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "policy")
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "limitedAge", nullable = false)
    private Integer limitedAge;

    @NotNull
    @Column(name = "minAdultAge", nullable = false)
    private Integer minAdultAge;

    @NotNull
    @Column(name = "childrenAllowed", nullable = false)
    private Boolean childrenAllowed = false;

    @NotNull
    @Column(name = "animalAllowed", nullable = false)
    private Boolean animalAllowed = false;

    @NotNull
    @Column(name = "freeCancellation", nullable = false)
    private LocalDate freeCancellation;

    @NotNull
    @Column(name = "starCheckinTime", nullable = false)
    private LocalDate starCheckinTime;

    @NotNull
    @Column(name = "endCheckinTime", nullable = false)
    private LocalDate endCheckinTime;

    @NotNull
    @Column(name = "starCheckoutTime", nullable = false)
    private LocalDate starCheckoutTime;

    @NotNull
    @Column(name = "endCheckoutTime", nullable = false)
    private LocalDate endCheckoutTime;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @NotNull
    @Column(name = "noAdvancePaid", nullable = false)
    private Boolean noAdvancePaid = false;

}