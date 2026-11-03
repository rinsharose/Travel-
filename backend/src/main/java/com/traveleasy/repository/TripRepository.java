package com.traveleasy.repository;

import com.traveleasy.entity.Trip;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query("""
        select t from Trip t
        where lower(t.source) = lower(:src) and lower(t.destination) = lower(:dst)
          and t.departure >= :from and t.departure < :to
          and t.availableSeats > 0
        order by t.departure
        """)
    List<Trip> search(@Param("src") String src, @Param("dst") String dst,
                      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<Trip> findAllByOrderByDepartureDesc();

    long countByDepartureAfter(LocalDateTime time);

    /** Atomic seat reservation: returns 0 if not enough seats (prevents overselling under concurrency). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Trip t set t.availableSeats = t.availableSeats - :n where t.id = :id and t.availableSeats >= :n")
    int reserve(@Param("id") Long id, @Param("n") int n);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Trip t set t.availableSeats = t.availableSeats + :n where t.id = :id")
    int release(@Param("id") Long id, @Param("n") int n);
}
