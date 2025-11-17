package com.example.pcos.health.tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.pcos.health.tracker.entity.Symptom;
import com.example.pcos.health.tracker.repository.SymptomRepository;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/symptoms")
public class SymptomController {

    @Autowired
    private SymptomRepository symptomRepository;

    @PostMapping("/add")
    public String addSymptom(@RequestBody Symptom symptom) {
        symptomRepository.save(symptom);
        return "Symptom added successfully!";
    }

    @GetMapping("/all")
    public List<Symptom> getAllSymptoms() {
        return symptomRepository.findAll();
    }
}
