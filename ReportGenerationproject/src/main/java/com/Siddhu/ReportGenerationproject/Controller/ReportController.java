package com.Siddhu.ReportGenerationproject.Controller;

import com.Siddhu.ReportGenerationproject.Entity.ReportMetadata;
import com.Siddhu.ReportGenerationproject.Service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Manual (Ad-hoc) report creation
    @PostMapping("/create")
    public ResponseEntity<ReportMetadata> createReport(@RequestParam @Valid String reportName,
                                                       @RequestParam @Valid String reportType,
                                                       @RequestParam @Valid String filePath,
                                                       @RequestParam String sensorType) {
        ReportMetadata reportMetadata = reportService.createReport(reportName, reportType, filePath, sensorType);
        return new ResponseEntity<>(reportMetadata, HttpStatus.CREATED); // HTTP 201 Created
    }

    // View all reports in reports-list screen
    @GetMapping("/view-all")
    public List<ReportMetadata> viewReport() {
        return reportService.viewReport();
    }


    // View reports by type
    @GetMapping("/reportType")
    public List<ReportMetadata> viewReportByType(@RequestParam String reportType) {
        return reportService.viewReportByType(reportType);
    }

    // Check report status
    @GetMapping("/status")
    public ResponseEntity<String> checkReportStatus(@RequestParam String reportType) {
        List<ReportMetadata> reports = reportService.viewReportByType(reportType);
        if (reports.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No report generated for the specified type.");
        } else {
            ReportMetadata lastReport = reports.get(reports.size() - 1);
            return ResponseEntity.ok("Last report generated on " + lastReport.getGeneratedTime() + ". File path: " + lastReport.getFilePath());
        }
    }

    @GetMapping("/filter-by-date")
    public ResponseEntity<List<ReportMetadata>> getReportsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        List<ReportMetadata> reports = reportService.getReportsByDateRange(fromDate, toDate);
        return ResponseEntity.ok(reports);
    }

    //Api for historical reports screen
    @GetMapping("/filter-by-date-reportType")
    public ResponseEntity<List<ReportMetadata>> getReportsByDateRangeAndType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam String reportType) {
        List<ReportMetadata> reports = reportService.getReportsByDateRangeAndType(fromDate, toDate, reportType);
        if (reports.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(reports);
        }
        return ResponseEntity.ok(reports);
    }
}

