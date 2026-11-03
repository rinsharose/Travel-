package com.traveleasy.controller;

import com.traveleasy.dto.Dtos.*;
import com.traveleasy.entity.Booking;
import com.traveleasy.repository.*;
import com.traveleasy.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final TripService tripService;
    private final TripRepository trips;
    private final BookingRepository bookings;
    private final UserRepository users;

    @GetMapping("/stats")
    public StatsResponse stats() {
        long confirmed = bookings.countByStatus(Booking.Status.CONFIRMED);
        long cancelled = bookings.countByStatus(Booking.Status.CANCELLED);
        return new StatsResponse(users.count(), trips.count(), confirmed + cancelled, confirmed, cancelled,
                bookings.sumAmountByStatus(Booking.Status.CONFIRMED));
    }

    @GetMapping("/trips")
    public List<TripResponse> allTrips() {
        return trips.findAllByOrderByDepartureDesc().stream().map(TripResponse::from).toList();
    }

    @PostMapping("/trips")
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse addTrip(@Valid @RequestBody TripRequest r) { return TripResponse.from(tripService.create(r)); }

    @DeleteMapping("/trips/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrip(@PathVariable Long id) { tripService.delete(id); }

    @GetMapping("/bookings")
    public List<BookingResponse> allBookings() {
        return bookings.findAllByOrderByCreatedAtDesc().stream().map(BookingResponse::from).toList();
    }
}
