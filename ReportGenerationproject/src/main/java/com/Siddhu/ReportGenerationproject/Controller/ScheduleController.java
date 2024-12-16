package com.Siddhu.ReportGenerationproject.Controller;

import com.Siddhu.ReportGenerationproject.Entity.ScheduleConfig;
import com.Siddhu.ReportGenerationproject.Service.ScheduleService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    // Configure a new schedule for report generation
    @PostMapping("/configure")
    public ScheduleConfig configureSchedule(@RequestParam String reportType,
                                            @RequestParam String scheduleInterval,
                                            @RequestParam String nextRun) {
        LocalDateTime nextRunTime = LocalDateTime.parse(nextRun);
        return scheduleService.configureSchedule(reportType, scheduleInterval, nextRunTime);
    }

//    @DeleteMapping("/delete-schedule")
//    public ScheduleConfig deleteSchedule(@RequestParam String reportType) {
//        return scheduleService.deleteSchedule(reportType);
//    }

}
