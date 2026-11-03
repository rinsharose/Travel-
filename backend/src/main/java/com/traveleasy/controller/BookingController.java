package com.traveleasy.controller;

import com.traveleasy.dto.Dtos.*;
import com.traveleasy.repository.BookingRepository;
import com.traveleasy.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService service;
    private final BookingRepository bookings;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(Authentication auth, @Valid @RequestBody BookingRequest r) {
        return BookingResponse.from(service.book(auth.getName(), r));
    }

    @GetMapping("/me")
    public List<BookingResponse> mine(Authentication auth) {
        return bookings.findByUserEmailOrderByCreatedAtDesc(auth.getName()).stream().map(BookingResponse::from).toList();
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(Authentication auth, @PathVariable Long id) {
        return BookingResponse.from(service.cancel(auth.getName(), id));
    }
}
