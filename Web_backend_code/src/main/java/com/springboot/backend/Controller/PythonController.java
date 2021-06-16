package com.springboot.backend.Controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.springboot.backend.Entity.Accident;
import com.springboot.backend.Entity.Member;
import com.springboot.backend.Entity.Seat;
import com.springboot.backend.Exception.EnumTypeException;
import com.springboot.backend.Exception.NotFoundException;
import com.springboot.backend.Service.AccidentService;
import com.springboot.backend.Service.LogService;
import com.springboot.backend.Service.MemberService;
import com.springboot.backend.Service.SeatService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@RestController
@RequestMapping(value = "/py")
public class PythonController {

    @Autowired
    private SeatService seatService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private AccidentService accidentService;
    @Autowired
    private LogService logService;

    @RequestMapping(method = RequestMethod.GET)
    public String helloWorld() {
        return "hello world";
    }

    @RequestMapping(path = "/parkin", method = RequestMethod.POST)
    public ResponseEntity<?> updateInfo(@RequestBody CarDto request) {
        /*Seat seat = seatService.getSeatByCarNum(request.getCarNum());
        if (seat == null)
            throw new NotFoundException(request.getCarNum());*/
        if (request.getStatus() != null &&
                request.getStatus() != CarDto.ParkInfo.parkIn && request.getStatus() != CarDto.ParkInfo.parkOut)
            throw new EnumTypeException(request.getStatus().toString());

        Seat seat = seatService.getSeatBySeatNum(request.getSeatNum());
        seat = seatService.updateSeat(seat, request.getCarNum(), request.getStatus().toString());

        System.out.println(request.getCarNum() + " " + request.getSeatNum() + " " + request.getStatus());

        Member member = memberService.getMemberByCarNum(request.getCarNum());
        memberService.updateSeatNum(member, request.getSeatNum());

        return ResponseEntity.status(HttpStatus.OK).body(seat);
    }

    @RequestMapping(path = "/parkout", method = RequestMethod.POST)
    public ResponseEntity<?> deleteInfo(@RequestBody CarDto request) {
        /*Seat seat = seatService.getSeatByCarNum(request.getCarNum());
        if (seat == null)
            throw new NotFoundException(request.getCarNum());*/
        if (request.getStatus() != null &&
                request.getStatus() != CarDto.ParkInfo.parkIn && request.getStatus() != CarDto.ParkInfo.parkOut)
            throw new EnumTypeException(request.getStatus().toString());

        Seat seat = seatService.getSeatBySeatNum(request.getSeatNum());
        if(seat == null)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(request);

        seat = seatService.deleteSeat(seat);

        Member member = memberService.getMemberByCarNum(request.getCarNum());
        memberService.deleteSeatNum(member);

        return ResponseEntity.status(HttpStatus.OK).body(seat);
    }

    @RequestMapping(path = "/accident", method = RequestMethod.POST)
    public ResponseEntity<?> changeInfo(@RequestBody AccidentDto request) {

        System.out.println(request.getCarNum1() + " " + request.getCarNum2());

        Accident accident = new Accident(request.getCarNum1(), request.getCarNum2(), request.getAccTime());
        if(accident == null)
            throw new NotFoundException(request.getCarNum1());
        System.out.println(accident.getCarNum1() + " " + accident.getCarNum2() + " " + accident.getAccTime());

        accidentService.updateAccident(accident);

        return ResponseEntity.status(HttpStatus.FOUND).body(request);
    }

    @Getter
    @Setter
    static class AccidentDto {
        @NotNull(message = "car number is required")
        @Size(message = "car number must be under 10", min = 1, max = 10)
        private String carNum1;
        @NotNull(message = "car number is required")
        @Size(message = "car number must be under 10", min = 1, max = 10)
        private String carNum2;

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime accTime;
    }

    @Getter
    @Setter
    static class CarDto {
        public enum ParkInfo {parkIn, parkOut}

        private Long seatNum;

        @NotNull(message = "car number is required")
        @Size(message = "car number must be under 10", min = 1, max = 10)
        private String carNum;

        private ParkInfo status;
    }
}