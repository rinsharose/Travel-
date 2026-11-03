package com.traveleasy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "bookings")
@Getter @Setter @NoArgsConstructor
public class Booking {
    public enum Status { CONFIRMED, CANCELLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(optional = false) @JoinColumn(name = "trip_id")
    private Trip trip;
    @Column(nullable = false)
    private String passengerName;
    @Column(nullable = false)
    private int seats;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status = Status.CONFIRMED;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
