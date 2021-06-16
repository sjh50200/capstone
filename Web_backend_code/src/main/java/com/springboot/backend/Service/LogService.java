package com.springboot.backend.Service;


import com.springboot.backend.Entity.Log;

public interface LogService {
    public Log isCarExists(String carNum);
    public Log updateLog(Log log);

    Log getLogByCarNum(String carNum);
}
