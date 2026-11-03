package com.traveleasy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "trips", indexes = @Index(name = "idx_trip_search", columnList = "source,destination,departure"))
@Getter @Setter @NoArgsConstructor
public class Trip {
    public enum Type { BUS, TRAIN }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Type type;
    @Column(name = "operator_name", nullable = false)
    private String operator;
    @Column(nullable = false)
    private String source;
    @Column(nullable = false)
    private String destination;
    @Column(nullable = false)
    private LocalDateTime departure;
    @Column(nullable = false)
    private LocalDateTime arrival;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @Column(nullable = false)
    private int totalSeats;
    @Column(nullable = false)
    private int availableSeats;
}
