package com.traveleasy.controller;

import com.traveleasy.dto.Dtos.TripResponse;
import com.traveleasy.entity.Trip;
import com.traveleasy.repository.TripRepository;
import com.traveleasy.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {
    private final TripService service;
    private final TripRepository trips;

    /** GET /api/trips/search?from=Bangalore&to=Chennai&date=2026-11-04&type=BUS (type optional) */
    @GetMapping("/search")
    public List<TripResponse> search(@RequestParam String from, @RequestParam String to,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                     @RequestParam(required = false) Trip.Type type) {
        return service.search(from, to, date, type).stream().map(TripResponse::from).toList();
    }

    @GetMapping("/count")
    public long upcomingCount() { return trips.countByDepartureAfter(LocalDateTime.now()); }
}
