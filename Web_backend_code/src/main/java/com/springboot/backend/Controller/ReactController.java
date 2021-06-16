/*
package com.springboot.backend.Controller;


import com.springboot.backend.Entity.Accident;
import com.springboot.backend.Entity.Log;
import com.springboot.backend.Entity.Member;
import com.springboot.backend.Entity.Seat;
import com.springboot.backend.Repository.AccidentRepository;
import com.springboot.backend.Repository.LogRepository;
import com.springboot.backend.Repository.MemberRepository;
import com.springboot.backend.Repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(value = "/react")
public class ReactController {
    @Autowired
    AccidentRepository accidentRepository;
    @Autowired
    LogRepository logRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    SeatRepository seatRepository;

    @RequestMapping(value = "/accidents", method = RequestMethod.GET)
    public List<Accident> getAccidents() {
        return accidentRepository.findAll();
    }

    @RequestMapping(value = "/accidents", method = RequestMethod.GET)
    public List<Log> getLogs() {
        return logRepository.findAll();
    }

    @RequestMapping(value = "/accidents", method = RequestMethod.GET)
    public List<Member> getMembers() {
        return memberRepository.findAll();
    }

    @RequestMapping(value = "/accidents", method = RequestMethod.GET)
    public List<Seat> getSeats() {
        return seatRepository.findAll();
    }

}
*/
