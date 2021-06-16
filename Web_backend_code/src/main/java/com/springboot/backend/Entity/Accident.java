package com.springboot.backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Accident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long accNum;

    @Column
    private String carNum1;

    @Column
    private String carNum2;

    @Column
    private LocalDateTime accTime;

    public Accident(String carNum1, String carNum2, LocalDateTime accTime) {
        this.carNum1 = carNum1;
        this.carNum2 = carNum2;
        this.accTime = accTime;
    }
}
