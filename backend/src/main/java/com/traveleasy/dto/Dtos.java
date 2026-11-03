package com.traveleasy.dto;

import com.traveleasy.entity.Booking;
import com.traveleasy.entity.Trip;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Dtos {
    private Dtos() {}

    public record RegisterRequest(@NotBlank String name, @NotBlank @Email String email,
                                  @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password) {}
    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record AuthResponse(String token, String name, String email, String role) {}

    public record TripRequest(@NotNull Trip.Type type, @NotBlank String operator, @NotBlank String source,
                              @NotBlank String destination, @NotNull LocalDateTime departure,
                              @NotNull LocalDateTime arrival, @NotNull @DecimalMin("0") BigDecimal price,
                              @Min(1) int totalSeats) {}

    public record TripResponse(Long id, Trip.Type type, String operator, String source, String destination,
                               LocalDateTime departure, LocalDateTime arrival, BigDecimal price,
                               int totalSeats, int availableSeats) {
        public static TripResponse from(Trip t) {
            return new TripResponse(t.getId(), t.getType(), t.getOperator(), t.getSource(), t.getDestination(),
                    t.getDeparture(), t.getArrival(), t.getPrice(), t.getTotalSeats(), t.getAvailableSeats());
        }
    }

    public record BookingRequest(@NotNull Long tripId, @NotBlank String passengerName,
                                 @Min(1) @Max(6) int seats) {}

    public record BookingResponse(Long id, String userEmail, String passengerName, int seats, BigDecimal totalAmount,
                                  Booking.Status status, LocalDateTime createdAt, TripResponse trip) {
        public static BookingResponse from(Booking b) {
            return new BookingResponse(b.getId(), b.getUser().getEmail(), b.getPassengerName(), b.getSeats(),
                    b.getTotalAmount(), b.getStatus(), b.getCreatedAt(), TripResponse.from(b.getTrip()));
        }
    }

    public record StatsResponse(long users, long trips, long bookings, long confirmed, long cancelled, BigDecimal revenue) {}
}
