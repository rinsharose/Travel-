package com.traveleasy.service;

import com.traveleasy.config.ApiException;
import com.traveleasy.dto.Dtos.TripRequest;
import com.traveleasy.entity.Trip;
import com.traveleasy.repository.BookingRepository;
import com.traveleasy.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {
    private final TripRepository trips;
    private final BookingRepository bookings;

    public List<Trip> search(String from, String to, LocalDate date, Trip.Type type) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = date.atStartOfDay();
        return trips.search(from.trim(), to.trim(), start, start.plusDays(1)).stream()
                .filter(t -> t.getDeparture().isAfter(now))
                .filter(t -> type == null || t.getType() == type)
                .toList();
    }

    public Trip create(TripRequest r) {
        if (!r.arrival().isAfter(r.departure()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Arrival time must be after departure time");
        Trip t = new Trip();
        t.setType(r.type());
        t.setOperator(r.operator().trim());
        t.setSource(r.source().trim());
        t.setDestination(r.destination().trim());
        t.setDeparture(r.departure());
        t.setArrival(r.arrival());
        t.setPrice(r.price());
        t.setTotalSeats(r.totalSeats());
        t.setAvailableSeats(r.totalSeats());
        return trips.save(t);
    }

    public void delete(Long id) {
        if (!trips.existsById(id)) throw new ApiException(HttpStatus.NOT_FOUND, "Trip not found");
        if (bookings.existsByTripId(id))
            throw new ApiException(HttpStatus.CONFLICT, "Operation not allowed: this trip has bookings");
        trips.deleteById(id);
    }
}
