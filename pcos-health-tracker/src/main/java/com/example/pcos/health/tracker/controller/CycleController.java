package com.example.pcos.health.tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.pcos.health.tracker.entity.Cycle;
import com.example.pcos.health.tracker.repository.CycleRepository;

import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/cycles")
public class CycleController {

    @Autowired
    private CycleRepository cycleRepository;

    @PostMapping("/add")
    public String addCycle(@RequestBody Cycle cycle) {
        // Calculate cycle duration automatically
        int duration = (int) ChronoUnit.DAYS.between(cycle.getStartDate(), cycle.getEndDate());
        cycle.setDuration(duration);

        cycleRepository.save(cycle);
        return "Cycle saved successfully with duration: " + duration + " days.";
    }

    @GetMapping("/all")
    public List<Cycle> getAllCycles() {
        return cycleRepository.findAll();
    }
}
