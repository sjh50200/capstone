package com.springboot.backend.Service;

import com.springboot.backend.Entity.Seat;
import com.springboot.backend.Repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Override
    public Seat getSeatByCarNum(String carNum) {
        return seatRepository.findByCarNum(carNum);
    }

    @Override
    public Seat getSeatBySeatNum(Long seatNum) {
        return seatRepository.findBySeatNum(seatNum);
    }

    @Override
    public Seat updateSeat(Seat seat, String carNum, String status) {
        seat.setCarNum(carNum);
        seat.setStatus(status);
        seatRepository.save(seat);
        return seat;
    }

    @Override
    public List<Long> findAllInavailableSeats() {
        List<Seat> seats = seatRepository.findAll();
        List<Long> result = new ArrayList<>();
        final String parkIn = "parkIn";
        Iterator<Seat> it = seats.iterator();

        while(it.hasNext()){
            Seat seat = it.next();
            if(parkIn.equals(seat.getStatus()))
                result.add(seat.getSeatNum());
        }

        if(result.size() == 0)
            return null;

        return result;
    }

    @Override
    public Seat deleteSeat(Seat seat) {
        seat.setStatus(null);
        seat.setCarNum(null);
        return seatRepository.save(seat);
    }
}