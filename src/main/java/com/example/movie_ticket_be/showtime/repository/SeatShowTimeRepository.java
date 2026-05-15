package com.example.movie_ticket_be.showtime.repository;

import com.example.movie_ticket_be.showtime.entity.SeatShowTime;
import com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface SeatShowTimeRepository extends JpaRepository<SeatShowTime, String> {

    // --- API VẼ SƠ ĐỒ GHẾ ---
    @Modifying
    @Transactional
    @Query(value = """
    INSERT INTO seat_show_time (seat_id, show_time_id, seat_show_time_status)
    SELECT s.seat_id, :showTimeId, 'AVAILABLE'
    FROM seat s
    WHERE s.room_id = :roomId
    AND NOT EXISTS (
        SELECT 1 FROM seat_show_time ss
        WHERE ss.seat_id = s.seat_id
        AND ss.show_time_id = :showTimeId
    )
    """, nativeQuery = true)
    void bulkInsertSeatsForShowTime(@Param("showTimeId") Long showTimeId, @Param("roomId") Long roomId);

    List<SeatShowTime> findByShowTimes_ShowTimeId(Long showTimeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<SeatShowTime> findAllBySeatShowTimeIdIn(Collection<Long> seatShowTimeIds);


    @Query("SELECT COUNT(ss) > 0 FROM SeatShowTime ss " +
            "WHERE ss.seatShowTimeId IN :ids " +
            "AND ss.seatShowTimeStatus IN ('SOLD', 'RESERVED', 'BLOCKED')")
    boolean existsAnyNotAvailable(@Param("ids") List<Long> ids);


    List<SeatShowTime> findBySeatShowTimeStatusAndLockedUntilBefore(SeatShowTimeStatus status, LocalDateTime now);

    @Modifying
    @Query("UPDATE SeatShowTime ss SET ss.seatShowTimeStatus = 'AVAILABLE', ss.users = null, ss.lockedUntil = null " +
            "WHERE ss.seatShowTimeStatus = 'RESERVED' AND ss.lockedUntil < :now")
    void releaseExpiredSeats(@Param("now") LocalDateTime now);

    boolean existsBySeatShowTimeId(Long seatShowTimeId);

    boolean existsByShowTimes_ShowTimeIdAndSeatShowTimeStatusIn(Long showTimeId, Collection<SeatShowTimeStatus> statuses);

    long countByShowTimes_ShowTimeIdAndSeatShowTimeStatus(Long showTimeId, SeatShowTimeStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM SeatShowTime ss WHERE ss.showTimes.showTimeId = :showTimeId AND ss.seatShowTimeStatus IN ('AVAILABLE', 'RESERVED', 'BLOCKED')")
    void deleteNonSoldSeatsByShowTime(@Param("showTimeId") Long showTimeId);
}
