package com.traveleasy.config;

import com.traveleasy.entity.Trip;
import com.traveleasy.entity.User;
import com.traveleasy.repository.TripRepository;
import com.traveleasy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final TripRepository trips;
    private final PasswordEncoder encoder;
    @Value("${app.admin.email}") private String adminEmail;
    @Value("${app.admin.password}") private String adminPassword;

    @Override
    public void run(String... args) {
        if (users.count() == 0) {
            User a = new User();
            a.setName("Admin");
            a.setEmail(adminEmail);
            a.setPasswordHash(encoder.encode(adminPassword));
            a.setRole(User.Role.ADMIN);
            users.save(a);
        }
        if (trips.count() == 0) {
            for (int day = 1; day <= 3; day++) {
                add(Trip.Type.BUS, "Blue Line Travels", "Bangalore", "Chennai", day, 21, 0, 420, 850, 40);
                add(Trip.Type.BUS, "Sunrise Express", "Bangalore", "Chennai", day, 23, 0, 420, 700, 35);
                add(Trip.Type.TRAIN, "Shatabdi Express", "Bangalore", "Chennai", day, 6, 0, 300, 1200, 80);
                add(Trip.Type.TRAIN, "Rajdhani Express", "Delhi", "Mumbai", day, 16, 30, 960, 2500, 100);
                add(Trip.Type.BUS, "Royal Cruiser", "Delhi", "Jaipur", day, 7, 0, 330, 600, 45);
            }
        }
    }

    private void add(Trip.Type type, String op, String src, String dst, int dayOffset, int h, int m,
                     int durationMin, int price, int seats) {
        LocalDateTime dep = LocalDate.now().plusDays(dayOffset).atTime(h, m);
        Trip t = new Trip();
        t.setType(type); t.setOperator(op); t.setSource(src); t.setDestination(dst);
        t.setDeparture(dep); t.setArrival(dep.plusMinutes(durationMin));
        t.setPrice(BigDecimal.valueOf(price)); t.setTotalSeats(seats); t.setAvailableSeats(seats);
        trips.save(t);
    }
}
