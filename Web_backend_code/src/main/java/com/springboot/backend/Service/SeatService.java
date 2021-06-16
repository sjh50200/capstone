package com.springboot.backend.Service;

import com.springboot.backend.Entity.Seat;

import java.util.List;

public interface SeatService {
    public Seat getSeatByCarNum(String carNum);
    public Seat getSeatBySeatNum(Long seatNum);

    public Seat updateSeat(Seat seat, String carNum, String status);

    public List<Long> findAllInavailableSeats();

    public Seat deleteSeat(Seat seat);
}