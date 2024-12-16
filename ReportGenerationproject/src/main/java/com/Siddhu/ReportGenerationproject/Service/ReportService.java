package com.Siddhu.ReportGenerationproject.Service;

import com.Siddhu.ReportGenerationproject.Entity.ReportMetadata;
import com.Siddhu.ReportGenerationproject.Entity.SensorData;
import com.Siddhu.ReportGenerationproject.Repository.ReportMetadataRepository;
import com.Siddhu.ReportGenerationproject.Repository.SensorDataRepository;
import com.Siddhu.ReportGenerationproject.Utils.ReportPdfGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final SensorDataRepository sensorDataRepository;
    private final ReportMetadataRepository reportMetadataRepository;
    private final ReportPdfGenerator reportPdfGenerator;

    public ReportService(SensorDataRepository sensorDataRepository,
                         ReportMetadataRepository reportMetadataRepository,
                         ReportPdfGenerator reportPdfGenerator) {
        this.sensorDataRepository = sensorDataRepository;
        this.reportMetadataRepository = reportMetadataRepository;
        this.reportPdfGenerator = reportPdfGenerator;
    }

    public ReportMetadata createReport(String reportName, String reportType, String filePath, String sensorType) {

        File directory = new File(filePath).getParentFile();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }


        ReportMetadata reportMetadata = new ReportMetadata();
        reportMetadata.setReportName(reportName);
        reportMetadata.setReportType(reportType);
        reportMetadata.setGeneratedTime(LocalDateTime.now());
        reportMetadata.setFilePath(filePath);


        List<SensorData> sensorDataList = fetchSensorData(reportType);


        reportPdfGenerator.generateReport(reportMetadata, sensorDataList, sensorType);



       return  reportMetadataRepository.save(reportMetadata);
    }


    public ReportMetadata saveReportMetadata(ReportMetadata reportMetadata) {
        return reportMetadataRepository.save(reportMetadata); // Save report metadata in DB
    }

    List<SensorData> fetchSensorData(String reportType) {
        return sensorDataRepository.findBySensorName(reportType);
    }

    // View all reports
    public List<ReportMetadata> viewReport() {
        return reportMetadataRepository.findAll();
    }

    public Page<ReportMetadata> getPaginatedReports(Pageable pageable) {
        return reportMetadataRepository.findAll(pageable);
    }

    // View reports by type
    public List<ReportMetadata> viewReportByType(String reportType) {
        return reportMetadataRepository.findByReportType(reportType)
                .stream()
                .filter(report -> report.getReportType().equalsIgnoreCase(reportType))
                .collect(Collectors.toList());
    }

    // Check report status
    public boolean isReportGenerated(String reportType) {
        return !viewReportByType(reportType).isEmpty();
    }

    // see reports between from and to date
    public List<ReportMetadata> getReportsByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        return reportMetadataRepository.findReportsByDateRange(fromDate, toDate);
    }

    // see the reports from and to date and by report type
    public List<ReportMetadata> getReportsByDateRangeAndType(LocalDateTime fromDate, LocalDateTime toDate, String reportType) {
        return reportMetadataRepository.findReportsByDateRangeAndType(fromDate, toDate, reportType);
    }


//    // Delete reports by report type
//    public void deleteReportsByType(String reportType) {
//        List<ReportMetadata> reports = reportMetadataRepository.findByReportType(reportType);
//        if (reports.isEmpty()) {
//            throw new IllegalArgumentException("No reports found for the specified report type.");
//        }
//        reportMetadataRepository.deleteAll(reports);
//    }
}



