package com.springboot.backend.Repository;

import com.springboot.backend.Entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    public Seat findByCarNum(String carNum);
    public Seat findBySeatNum(Long seatNum);
}
