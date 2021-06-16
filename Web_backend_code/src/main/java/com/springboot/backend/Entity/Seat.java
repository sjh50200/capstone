package com.springboot.backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Seat {

    @Id
    @Column(nullable = false)
    private long seatNum;

    @Column(columnDefinition = "varchar(255) default NULL")
    private String carNum;

    @Column(columnDefinition = "varchar(255) default NULL")
    private String Status;
}