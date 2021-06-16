package com.springboot.backend.Controller;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.springboot.backend.Entity.Log;
import com.springboot.backend.Entity.Member;
import com.springboot.backend.Service.LogService;
import com.springboot.backend.Service.MemberService;
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
@RequestMapping(value = "/raspberry")
public class RaspberryPiController {
    @Autowired
    LogService logService;
    @Autowired
    MemberService memberService;

    @RequestMapping(path = "/entry", method = RequestMethod.POST)
    public ResponseEntity<?> updateEntry(@RequestBody CarDto request) {
        Member member = memberService.getMemberByCarNum(request.carNum);
        member.setEntryTime(request.time);
        memberService.updateMember(member);

        Log log = new Log();
        log.setCarNum(request.getCarNum());
        log.setEntryTime(request.getTime());

        return ResponseEntity.status(HttpStatus.OK).body(logService.updateLog(log));
    }

    @RequestMapping(path = "/exit", method = RequestMethod.POST)
    public ResponseEntity<?> updateExit(@RequestBody CarDto request) {
        Log log = logService.getLogByCarNum(request.getCarNum());
        log.setExitTime(request.getTime());

        return ResponseEntity.status(HttpStatus.OK).body(logService.updateLog(log));
    }

    @Getter
    @Setter
    static class CarDto {
        @NotNull(message = "car number is required")
        @Size(message = "car number must be under 10", min = 1, max = 10)
        private String carNum;

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime time;
    }
}
