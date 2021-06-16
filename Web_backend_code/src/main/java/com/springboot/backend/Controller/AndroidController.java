package com.springboot.backend.Controller;

import com.springboot.backend.Entity.Member;
import com.springboot.backend.Entity.Seat;
import com.springboot.backend.Exception.NotFoundException;
import com.springboot.backend.Service.MemberService;
import com.springboot.backend.Service.SeatService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/android")
public class AndroidController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private SeatService seatService;

    public int calcFee(LocalDateTime time){
        LocalDateTime now = LocalDateTime.now();

        int dayDiffer = time.getDayOfMonth() - now.getDayOfMonth();
        int hourDiffer, minuteDiffer, secondDiffer;
        if(now.getHour() - time.getHour() < 0)
            hourDiffer = (now.getHour() - time.getHour()) + 24;
        else
            hourDiffer = (now.getHour() - time.getHour());

        if(now.getMinute() - time.getMinute() < 0)
            minuteDiffer = (now.getMinute() - time.getMinute()) + 60;
        else
            minuteDiffer = (now.getMinute() - time.getMinute());

        if(now.getSecond() - time.getSecond() < 0)
            secondDiffer = (now.getSecond() - time.getSecond()) + 60;
        else
            secondDiffer = (now.getSecond() - time.getSecond());

        return ((dayDiffer * 86400) + (hourDiffer * 3600) + (minuteDiffer * 60) + secondDiffer) / 10 * 500;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String helloWorld() {
        System.out.println("hello world");
        return "hello world";
    }

    @RequestMapping(path = "/fee/{carNum}", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveFee(@PathVariable String carNum) {
        LocalDateTime time = memberService.getMemberByCarNum(carNum).getEntryTime();

        return ResponseEntity.ok(calcFee(time));
    }

    @RequestMapping(path = "/member/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveCategory(@PathVariable String id) {

        Member member = memberService.getMemberById(id);
        if (member == null)
            throw new NotFoundException(id);

        return ResponseEntity.ok(member);
    }

    @RequestMapping(path = "/member", method = RequestMethod.POST)
    public ResponseEntity<?> checkInfo(@RequestBody MemberDto request) {

        Member member = memberService.getMemberById(request.getId());
        if (member == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        else {
            CarDto response = new CarDto();
            if (member.getSeatNum() == null) {
                response.setCarNum(member.getCarNum());
                return ResponseEntity.status(HttpStatus.FOUND).body(response);
            }
            else {
                response.setSeatNum(member.getSeatNum());
                response.setCarNum(member.getCarNum());
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        }
    }


    @RequestMapping(path = "/seats", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveSeats() {
        SeatsDto seats = new SeatsDto();
        seats.setParked(seatService.findAllInavailableSeats());
        List<Long> temp = new ArrayList<>();
        temp.add(Long.valueOf(3));
        temp.add(Long.valueOf(5));
        seats.setReserved(temp);

        return ResponseEntity.ok(seats);
    }

    @RequestMapping(path = "/seat", method = RequestMethod.POST)
    public ResponseEntity<?> retrieveSeat(@RequestBody MemberDto request) {
        Member member = memberService.getMemberById(request.getId());

        Seat seat = seatService.getSeatByCarNum(member.getCarNum());

        return ResponseEntity.status(HttpStatus.FOUND).body(seat);
    }

    @RequestMapping(path = "/seat/{carNum}", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveSeat(@PathVariable String carNum) {

        Seat seat = seatService.getSeatByCarNum(carNum);
        if (seat == null)
            throw new NotFoundException(carNum);

        return ResponseEntity.ok(seat);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    class CarDto {
        private Long seatNum = null;
        private String carNum;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    class SeatsDto {
        private List<Long> parked;
        private List<Long> reserved;
    }

    @Getter
    @Setter
    static class MemberDto {
        private String id;
        private String pw;
    }
}