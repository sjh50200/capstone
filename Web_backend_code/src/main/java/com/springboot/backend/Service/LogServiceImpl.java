package com.springboot.backend.Service;

import com.springboot.backend.Entity.Log;
import com.springboot.backend.Repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogServiceImpl implements LogService {
    @Autowired
    private LogRepository logRepository;

    public Log isCarExists(String carNum){
        List<Log> logs = logRepository.findAll();

        for(int i=0; i<logs.size(); i++){
            if(carNum.equals(logs.get(i).getCarNum()))
                return logs.get(i);
        }
        return null;
    }

    public Log updateLog(Log log){
        return logRepository.save(log);
    }

    @Override
    public Log getLogByCarNum(String carNum) {
        return logRepository.getLogByCarNum(carNum);
    }
}
