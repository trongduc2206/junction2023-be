package com.ducvt.diabeater.account.models;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
@Data
@Entity
@Table(name = "stats")
public class Stats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Float glucoseLevel;

    private Float a1cLevel;

    private Float eag;

    private Float gmi;

    private Float cv;

    private Float weight;

    private Float height;

    private Float bmi;

    private Date createTime;
}
