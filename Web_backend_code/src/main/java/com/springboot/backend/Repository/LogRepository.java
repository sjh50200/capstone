package com.springboot.backend.Repository;

import com.springboot.backend.Entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    public Log getLogByCarNum(String carNum);
}
