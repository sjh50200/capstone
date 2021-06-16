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
public class Member {

    @Id
    @Column(name = "member_num", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long number;

    @Column(nullable = false, length = 50)
    private String id;

    @Column(nullable = false, length = 50)
    private String pw;

    @Column
    private LocalDateTime entryTime;

    @Column(nullable = false, length = 50)
    private String carNum;

    @Column
    private Long seatNum;

    @Column
    private Boolean isParked = false;
}