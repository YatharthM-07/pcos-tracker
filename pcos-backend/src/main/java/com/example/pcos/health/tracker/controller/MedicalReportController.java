package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.entity.MedicalReport;
import com.example.pcos.health.tracker.entity.User;
import com.example.pcos.health.tracker.repository.MedicalReportRepository;
import com.example.pcos.health.tracker.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/reports")
public class MedicalReportController {

    @Autowired
    private MedicalReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    /* =========================
       UPLOAD REPORT
    ========================== */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadReport(
            @RequestParam("file") MultipartFile file,
            Principal principal
    ) throws Exception {

        User user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        MedicalReport report = new MedicalReport();
        report.setUser(user);
        report.setFileName(file.getOriginalFilename());
        report.setFileType(file.getContentType());
        report.setUploadDate(LocalDateTime.now());
        report.setFileData(file.getBytes());

        reportRepository.save(report);

        return ResponseEntity.ok(Map.of("message", "Report uploaded"));
    }

    /* =========================
       GET ALL REPORTS
    ========================== */
    @GetMapping("/all")
    public List<Map<String, Object>> getAllReports(Principal principal) {

        User user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        List<MedicalReport> reports = reportRepository.findByUserId(user.getId());
        List<Map<String, Object>> response = new ArrayList<>();

        for (MedicalReport r : reports) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("fileName", r.getFileName());
            map.put("uploadDate", r.getUploadDate().toString());
            response.add(map);
        }

        return response;
    }

    /* =========================
       DOWNLOAD REPORT
    ========================== */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadReport(
            @PathVariable Long id,
            Principal principal
    ) {

        User user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        MedicalReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        if (!report.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + report.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(report.getFileType()))
                .body(report.getFileData());
    }

    /* =========================
       DELETE REPORT
    ========================== */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteReport(
            @PathVariable Long id,
            Principal principal
    ) {

        User user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        MedicalReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        if (!report.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        reportRepository.delete(report);
        return ResponseEntity.ok(Map.of("message", "Report deleted"));
    }
}
