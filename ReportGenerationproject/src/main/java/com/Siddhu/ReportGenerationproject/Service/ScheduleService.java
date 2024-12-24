package com.Siddhu.ReportGenerationproject.Service;

import com.Siddhu.ReportGenerationproject.Entity.ReportMetadata;
import com.Siddhu.ReportGenerationproject.Entity.ScheduleConfig;
import com.Siddhu.ReportGenerationproject.Entity.SensorData;
import com.Siddhu.ReportGenerationproject.Repository.ReportMetadataRepository;
import com.Siddhu.ReportGenerationproject.Repository.ScheduleConfigRepository;
import com.Siddhu.ReportGenerationproject.Repository.SensorDataRepository;
import com.Siddhu.ReportGenerationproject.Utils.ReportPdfGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ScheduleService {

    private final ReportService reportService;
    private final ReportPdfGenerator reportPdfGenerator;
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
    private final ScheduleConfigRepository scheduleConfigRepository;
    private final SensorDataRepository sensorDataRepository;
    private final Lock reportLock = new ReentrantLock();
    private static final AtomicBoolean reportGenerationEnabled = new AtomicBoolean(true);
    private final ReportMetadataRepository reportMetadataRepository;

    public ScheduleService(ReportService reportService, ScheduleConfigRepository scheduleConfigRepository,
                           ReportPdfGenerator reportPdfGenerator, SensorDataRepository sensorDataRepository, ReportMetadataRepository reportMetadataRepository) {
        this.reportService = reportService;
        this.scheduleConfigRepository = scheduleConfigRepository;
        this.reportPdfGenerator = reportPdfGenerator;
        this.sensorDataRepository = sensorDataRepository;
        this.reportMetadataRepository=reportMetadataRepository;
    }

    public void enableReportGeneration() {
        reportGenerationEnabled.set(true);
        logger.info("Report generation has been enabled.");

        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        List<ScheduleConfig> schedules = scheduleConfigRepository.findAll();
        for (ScheduleConfig schedule : schedules) {
            if ("HOURLY".equalsIgnoreCase(schedule.getScheduleInterval())) {
                // Set next run to the immediate next hour after enabling
                LocalDateTime nextRun = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
                schedule.setNextRun(nextRun);
                scheduleConfigRepository.save(schedule);
                logger.info("Next HOURLY report run time updated to: {}", nextRun);
            }
        }

        List<ScheduleConfig> hourlySchedules = scheduleConfigRepository.findByScheduleInterval("HOURLY");
        for (ScheduleConfig hourlySchedule : hourlySchedules) {
            logger.info("Hourly schedule next run after enabling: {}", hourlySchedule.getNextRun());
        }

    }

    public void disableReportGeneration() {
        reportGenerationEnabled.set(false);
        logger.info("Report generation has been disabled.");
    }

    private void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean isCreated = directory.mkdirs();
            if (isCreated) {
                logger.info("Directory created: {}", directoryPath);
            } else {
                logger.error("Failed to create directory: {}", directoryPath);
            }
        }
    }

    public ScheduleConfig configureSchedule(String reportType, String scheduleInterval, LocalDateTime nextRun) {
        ScheduleConfig scheduleConfig = new ScheduleConfig();
        scheduleConfig.setReportType(reportType);
        scheduleConfig.setScheduleInterval(scheduleInterval);
        scheduleConfig.setNextRun(nextRun);
        return scheduleConfigRepository.save(scheduleConfig);
    }

    private LocalDateTime calculateNextRunTime(ScheduleConfig scheduleConfig, LocalDateTime now) {
        switch (scheduleConfig.getScheduleInterval().toUpperCase()) {
            case "HOURLY":
                return now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
            case "DAILY":
                return now.plusDays(1).withHour(10).withMinute(30).withSecond(0).withNano(0);
            default:
                throw new IllegalArgumentException("Unknown schedule interval: " + scheduleConfig.getScheduleInterval());
        }
    }

    @Scheduled(cron = "${report.schedule.hourly.cron}")
    public void generateHourlyReports() {
        if (!reportGenerationEnabled.get()) {
            logger.info("Hourly report generation is disabled. Skipping this execution.");
            return;
        }
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0); // Normalize time to the top of the minute
        logger.info("Hourly report task triggered at: {}", now);

        if (reportLock.tryLock()) {
            try {
                String hourlyFolderPath = "C:\\Reports\\HourlyReports\\";
                createDirectoryIfNotExists(hourlyFolderPath);

                List<ScheduleConfig> schedules = scheduleConfigRepository.findAll();
                for (ScheduleConfig schedule : schedules) {
                    if ("HOURLY".equalsIgnoreCase(schedule.getScheduleInterval())) {
                        LocalDateTime nextRun = schedule.getNextRun().withSecond(0).withNano(0);
                        logger.debug("Normalized current time: {}, Normalized next scheduled run time: {}", now, nextRun);

                        if (now.isBefore(nextRun)) {
                            logger.info("Trigger occurred before the scheduled time. Adjusting to scheduled time: {}", nextRun);
                            now = nextRun;
                        }

                        now = now.withMinute(0).withSecond(0).withNano(0);


                        if (now.equals(nextRun)) {
                            logger.info("Generating report for scheduled time: {}", now);
                        }


                        int currentHour = now.getHour();
                        if (currentHour < 10 || currentHour >= 22) {

                            LocalDateTime nextValidRun = now.plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
                            schedule.setNextRun(nextValidRun);
                            scheduleConfigRepository.save(schedule);
                            logger.info("Outside allowed hours. Next run scheduled for: {}", nextValidRun);
                            continue;
                        }

                        String reportType = schedule.getReportType();
                        logger.debug("Fetching sensor data for report type: {}", reportType);
                        List<SensorData> sensorDataList = reportService.fetchSensorData(reportType);

                        if (sensorDataList.isEmpty()) {
                            logger.warn("No sensor data found for report type: {}", reportType);
                            continue;
                        }

                        String filePath = hourlyFolderPath + reportType + "_HOURLY_Report_" +
                                now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

                        ReportMetadata reportMetadata = new ReportMetadata();
                        reportMetadata.setReportName(reportType + "_Report");
                        reportMetadata.setReportType(reportType);
                        reportMetadata.setGeneratedTime(now);
                        reportMetadata.setFilePath(filePath);

                        reportPdfGenerator.generateReport(reportMetadata, sensorDataList, reportType);
                        logger.info("Hourly report generated: {}", filePath);

                        reportMetadataRepository.save(reportMetadata);

                        LocalDateTime nextReportRunTime = nextRun.plusHours(1).withMinute(0).withSecond(0).withNano(0);

                        schedule.setNextRun(nextReportRunTime);
                        scheduleConfigRepository.save(schedule);
                        logger.debug("Next hourly report run time updated to: {}", nextReportRunTime);
                    }
                }
            } catch (Exception e) {
                logger.error("Error during hourly report generation: {}", e.getMessage());
            } finally {
                reportLock.unlock();
            }
        } else {
            logger.info("Hourly report task is already running, skipping this execution.");
        }
    }







    @Scheduled(cron = "${report.schedule.daily.cron}")
    public void generateDailyReports() {
        if (!reportGenerationEnabled.get()) {
            logger.info("Daily report generation is disabled. Skipping this execution.");
            return;
        }
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        logger.info("Daily report task triggered at: {}", now);

        if (reportLock.tryLock()) {
            try {
                String dailyFolderPath = "C:\\Reports\\DailyReports\\";
                createDirectoryIfNotExists(dailyFolderPath);

                List<ScheduleConfig> schedules = scheduleConfigRepository.findAll();
                for (ScheduleConfig schedule : schedules) {
                    if ("DAILY".equalsIgnoreCase(schedule.getScheduleInterval())) {
                        LocalDateTime nextRun = schedule.getNextRun();

                        if (!now.isBefore(nextRun)) {
                            String reportType = schedule.getReportType();
                            logger.debug("Fetching sensor data for report type: {}", reportType);
                            List<SensorData> sensorDataList = reportService.fetchSensorData(reportType);

                            if (sensorDataList.isEmpty()) {
                                logger.warn("No sensor data found for report type: {}", reportType);
                                continue;
                            }

                            String filePath = dailyFolderPath + reportType + "_DAILY_Report_" +
                                    now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

                            ReportMetadata reportMetadata = new ReportMetadata();
                            reportMetadata.setReportName(reportType + "_Report");
                            reportMetadata.setReportType(reportType);
                            reportMetadata.setGeneratedTime(now);
                            reportMetadata.setFilePath(filePath);

                            reportPdfGenerator.generateReport(reportMetadata, sensorDataList, reportType);
                            logger.info("Daily report generated: {}", filePath);

                            reportMetadataRepository.save(reportMetadata);

                            LocalDateTime nextReportRunTime = calculateNextRunTime(schedule, now);
                            schedule.setNextRun(nextReportRunTime);
                            scheduleConfigRepository.save(schedule);
                            logger.debug("Next daily report run time updated to: {}", nextReportRunTime);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error during daily report generation: {}", e.getMessage());
            } finally {
                reportLock.unlock();
            }
        } else {
            logger.info("Daily report task is already running, skipping this execution.");
        }
    }

    @Transactional
    public ScheduleConfig deleteSchedule(String reportType, String scheduleInterval) {
        List<ScheduleConfig> schedules = scheduleConfigRepository.findByReportTypeAndScheduleInterval(reportType, scheduleInterval);

        if (schedules.isEmpty()) {
            throw new IllegalArgumentException("Schedule not found for report type: " + reportType + " and schedule interval: " + scheduleInterval);
        }

        ScheduleConfig scheduleConfig = schedules.get(0);
        scheduleConfigRepository.delete(scheduleConfig);
        logger.info("Schedule for report type: {} and schedule interval: {} has been deleted.", reportType, scheduleInterval);
        return scheduleConfig;
    }
}
