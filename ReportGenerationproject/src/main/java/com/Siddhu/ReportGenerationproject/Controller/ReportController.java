package com.Siddhu.ReportGenerationproject.Controller;

import com.Siddhu.ReportGenerationproject.Entity.ReportMetadata;
import com.Siddhu.ReportGenerationproject.Service.ReportService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

//    @GetMapping("/paginated")
//    public Page<ReportMetadata> getPaginatedReports(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        return reportService.getPaginatedReports(PageRequest.of(page, size));
//    }

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



////    @DeleteMapping("/delete/{report-type}")
////    public ResponseEntity <String> deleteReport(@PathVariable String ReportType) {
////        try {
////            reportService.deleteReportsByType(ReportType);
////            return ResponseEntity.ok("Reports of type " + ReportType + " have been deleted successfully.");
////        } catch (Exception e) {
////            return ResponseEntity.status(400).body("Error: " + e.getMessage());
////        }
//
//
//    }

//    @PostMapping("/generate")
//    public ResponseEntity<String> generateReport(@RequestParam String sensorName,
//                                                 @RequestParam String interval,
//                                                 @RequestParam String filePath) {
//        try {
//            ReportMetadata report = reportService.createReport(sensorName, interval, filePath);
//            return ResponseEntity.ok("Report generated successfully: " + report.getFilePath());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
//        }
//    }

    @GetMapping("/details")
    public Map<String, Object> getReportDetails(@RequestParam String filePath) {
        Map<String, Object> response = new HashMap<>();
        File pdfFile = new File(filePath);

        if (!pdfFile.exists()) {
            response.put("error", "File not found: " + filePath);
            return response;
        }

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDDocumentInformation info = document.getDocumentInformation();
            response.put("Title", info.getTitle());
            response.put("Author", info.getAuthor());
            response.put("CreationDate", info.getCreationDate());
            response.put("ModificationDate", info.getModificationDate());
            response.put("NumberOfPages", document.getNumberOfPages());

            PDFTextStripper textStripper = new PDFTextStripper();
            String content = textStripper.getText(document);
            response.put("Content", content.substring(0, Math.min(content.length(), 500))); // Limit text size
        } catch (IOException e) {
            response.put("error", "Unable to read PDF: " + e.getMessage());
        }

        return response;
    }
}
