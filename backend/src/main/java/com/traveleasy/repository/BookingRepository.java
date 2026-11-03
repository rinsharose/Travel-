package com.traveleasy.repository;

import com.traveleasy.entity.Booking;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserEmailOrderByCreatedAtDesc(String email);
    List<Booking> findAllByOrderByCreatedAtDesc();
    boolean existsByTripId(Long tripId);
    long countByStatus(Booking.Status status);

    @Query("select coalesce(sum(b.totalAmount), 0) from Booking b where b.status = :s")
    BigDecimal sumAmountByStatus(@Param("s") Booking.Status s);
}
