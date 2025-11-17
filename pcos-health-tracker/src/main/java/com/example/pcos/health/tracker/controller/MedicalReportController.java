package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.entity.MedicalReport;
import com.example.pcos.health.tracker.entity.User;
import com.example.pcos.health.tracker.repository.MedicalReportRepository;
import com.example.pcos.health.tracker.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/reports")
public class MedicalReportController {

    @Autowired
    private MedicalReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    // -------------------------------
    // 1) UPLOAD FILE
    // -------------------------------
    @PostMapping("/upload")
    public ResponseEntity<?> uploadReport(
            @RequestParam Long userId,
            @RequestParam("file") MultipartFile file) {

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

            MedicalReport report = new MedicalReport();
            report.setUser(userOpt.get());
            report.setFileName(file.getOriginalFilename());
            report.setFileType(file.getContentType());
            report.setUploadDate(LocalDateTime.now());
            report.setFileData(file.getBytes());

            reportRepository.save(report);

            return ResponseEntity.ok(Map.of("message", "Report uploaded", "id", report.getId()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading file: " + e.getMessage());
        }
    }

    // -------------------------------
    // 2) GET ALL REPORTS FOR USER
    // -------------------------------
    @GetMapping("/all")
    public List<Map<String, Object>> getAllReports(@RequestParam Long userId) {

        List<MedicalReport> reports = reportRepository.findByUserId(userId);
        List<Map<String, Object>> response = new ArrayList<>();

        for (MedicalReport r : reports) {
            response.add(Map.of(
                    "id", r.getId(),
                    "fileName", r.getFileName(),
                    "fileType", r.getFileType(),
                    "uploadDate", r.getUploadDate().toString()
            ));
        }
        return response;
    }

    // -------------------------------
    // 3) DOWNLOAD FILE
    // -------------------------------
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {

        Optional<MedicalReport> opt = reportRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        MedicalReport report = opt.get();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + report.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(report.getFileType()))
                .body(report.getFileData());
    }

    // -------------------------------
    // 4) DELETE REPORT
    // -------------------------------
    @DeleteMapping("/delete/{id}")
    public Map<String, String> deleteReport(@PathVariable Long id) {
        reportRepository.deleteById(id);
        return Map.of("message", "Report deleted");
    }
}
