package com.springboot.backend.Service;

import com.springboot.backend.Entity.Accident;
import com.springboot.backend.Repository.AccidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccidentServiceImpl implements AccidentService{

    @Autowired
    private AccidentRepository accidentRepository;

    @Override
    public Accident updateAccident(Accident accident) {
        Accident acc;
        acc = accidentRepository.save(accident);
        return acc;
    }
}
