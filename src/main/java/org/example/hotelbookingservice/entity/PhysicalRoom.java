package org.example.hotelbookingservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.hotelbookingservice.enums.RoomCondition;

@Getter
@Setter
@Entity
@Table(name = "PhysicalRoom")
public class PhysicalRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "roomNumber", nullable = false)
    private Integer roomNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "roomCondition", nullable = false)
    private RoomCondition roomCondition;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

}
