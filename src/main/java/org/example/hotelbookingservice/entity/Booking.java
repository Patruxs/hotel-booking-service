package org.example.hotelbookingservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.hotelbookingservice.enums.BookingStatus;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "`bookingReference`", nullable = false)
    private String bookingReference;

    @NotNull
    @Column(name = "checkinDate", nullable = false)
    private LocalDate checkinDate;

    @NotNull
    @Column(name = "checkoutDate", nullable = false)
    private LocalDate checkoutDate;

    @NotNull
    @Column(name = "adultAmount", nullable = false)
    private Integer adultAmount;

    @Size(max = 255)
    @NotNull
    @Column(name = "customerName", nullable = false)
    private String customerName;

    @Size(max = 10)
    @Column(name = "room_number")
    private String roomNumber;

    @Size(max = 255)
    @Column(name = "cancelReason")
    private String cancelReason;

    @NotNull
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Size(max = 255)
    @Column(name = "specialRequire")
    private String specialRequire;

    @NotNull
    @Column(name = "createAt", nullable = false)
    private LocalDate createAt;

    @NotNull
    @Column(name = "childrenAmount", nullable = false)
    private Integer childrenAmount;

    @NotNull
    @Column(name = "totalPrice", nullable = false)
    private Float totalPrice;

    @Column(name = "refund")
    private Float refund;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Bookingroom> bookingrooms = new LinkedHashSet<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Payment> payments = new LinkedHashSet<>();


}