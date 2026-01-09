package com.example.pcos.health.tracker.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class PeriodRequest {

    private LocalDate startDate;
    private LocalDate endDate;

}
