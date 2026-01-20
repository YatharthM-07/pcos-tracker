package com.example.pcos.health.tracker.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CycleRequest {

    private LocalDate startDate;
    private LocalDate endDate;

}
