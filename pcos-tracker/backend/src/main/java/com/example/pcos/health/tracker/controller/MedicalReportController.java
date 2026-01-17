package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.entity.MedicalReport;
import com.example.pcos.health.tracker.entity.User;
import com.example.pcos.health.tracker.repository.MedicalReportRepository;
import com.example.pcos.health.tracker.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    /* =====================================================
       HELPER → GET LOGGED-IN USER FROM JWT
    ====================================================== */
    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // extracted from JWT

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return user;
    }


    /* =====================================================
       1) UPLOAD REPORT
    ====================================================== */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadReport(
            @RequestParam("file") MultipartFile file) {

        try {
            User user = getLoggedInUser();

            MedicalReport report = new MedicalReport();
            report.setUser(user);
            report.setFileName(file.getOriginalFilename());
            report.setFileType(file.getContentType());
            report.setUploadDate(LocalDateTime.now());
            report.setFileData(file.getBytes());

            reportRepository.save(report);

            return ResponseEntity.ok(
                    Map.of("message", "Report uploaded", "id", report.getId())
            );

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error uploading report: " + e.getMessage());
        }
    }

    /* =====================================================
       2) GET ALL REPORTS (LOGGED-IN USER)
    ====================================================== */
    @GetMapping("/all")
    public List<Map<String, Object>> getAllReports() {

        User user = getLoggedInUser();
        List<MedicalReport> reports = reportRepository.findByUserId(user.getId());

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

    /* =====================================================
       3) DOWNLOAD REPORT (OWNERSHIP CHECK)
    ====================================================== */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long id,
            Principal principal) {

        MedicalReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        // 🔐 Security check (VERY IMPORTANT)
        if (!report.getUser().getEmail().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + report.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(report.getFileType()))
                .body(report.getFileData());
    }


    /* =====================================================
       4) DELETE REPORT (OWNERSHIP CHECK)
    ====================================================== */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {

        User user = getLoggedInUser();

        MedicalReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        if (!report.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        reportRepository.delete(report);
        return ResponseEntity.ok(Map.of("message", "Report deleted"));
    }
}