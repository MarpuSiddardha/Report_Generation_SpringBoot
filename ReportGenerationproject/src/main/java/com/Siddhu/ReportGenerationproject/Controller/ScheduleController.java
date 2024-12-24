package com.Siddhu.ReportGenerationproject.Controller;

import com.Siddhu.ReportGenerationproject.Entity.ScheduleConfig;
import com.Siddhu.ReportGenerationproject.Repository.ScheduleConfigRepository;
import com.Siddhu.ReportGenerationproject.Service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleConfigRepository scheduleConfigRepository;

    public ScheduleController(ScheduleService scheduleService, ScheduleConfigRepository  scheduleConfigRepository) {
        this.scheduleService = scheduleService;
        this.scheduleConfigRepository=scheduleConfigRepository;
    }

    @PostMapping("/configure")
    public ScheduleConfig configureSchedule(@RequestParam String reportType,
                                            @RequestParam String scheduleInterval,
                                            @RequestParam String nextRun) {
        LocalDateTime nextRunTime = LocalDateTime.parse(nextRun);
        return scheduleService.configureSchedule(reportType, scheduleInterval, nextRunTime);
    }


    @PostMapping("/enable")
    public ResponseEntity<String> enableReportGeneration() {
        scheduleService.enableReportGeneration();
        return ResponseEntity.ok("Report generation enabled.");
    }

    @PostMapping("/disable")
    public ResponseEntity<String> disableReportGeneration() {
        scheduleService.disableReportGeneration();
        return ResponseEntity.ok("Report generation disabled.");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteSchedule(@RequestParam String reportType, @RequestParam String scheduleInterval) {
        try {
            ScheduleConfig deletedSchedule = scheduleService.deleteSchedule(reportType, scheduleInterval);
            return ResponseEntity.ok("Schedule for report type '" + reportType + "' and interval '" + scheduleInterval + "' has been deleted.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error occurred while deleting the schedule.");
        }
    }


}
