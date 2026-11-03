package com.traveleasy.service;

import com.traveleasy.config.ApiException;
import com.traveleasy.dto.Dtos.BookingRequest;
import com.traveleasy.entity.*;
import com.traveleasy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookings;
    private final TripRepository trips;
    private final UserRepository users;

    @Transactional
    public Booking book(String email, BookingRequest r) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        Trip trip = trips.findById(r.tripId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!trip.getDeparture().isAfter(LocalDateTime.now()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "This trip has already departed");
        if (trips.reserve(trip.getId(), r.seats()) == 0)
            throw new ApiException(HttpStatus.CONFLICT, "Not enough seats left on this trip");

        Trip fresh = trips.findById(trip.getId()).orElseThrow();
        Booking b = new Booking();
        b.setUser(user);
        b.setTrip(fresh);
        b.setPassengerName(r.passengerName().trim());
        b.setSeats(r.seats());
        b.setTotalAmount(fresh.getPrice().multiply(BigDecimal.valueOf(r.seats())));
        return bookings.save(b);
    }

    @Transactional
    public Booking cancel(String email, Long id) {
        Booking b = bookings.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!b.getUser().getEmail().equals(email))
            throw new ApiException(HttpStatus.NOT_FOUND, "Booking not found"); // don't reveal others' bookings
        if (b.getStatus() != Booking.Status.CONFIRMED)
            throw new ApiException(HttpStatus.CONFLICT, "Booking is already cancelled");
        if (!b.getTrip().getDeparture().isAfter(LocalDateTime.now()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Departed trips cannot be cancelled");
        b.setStatus(Booking.Status.CANCELLED);
        bookings.saveAndFlush(b);
        trips.release(b.getTrip().getId(), b.getSeats());
        return b;
    }
}
