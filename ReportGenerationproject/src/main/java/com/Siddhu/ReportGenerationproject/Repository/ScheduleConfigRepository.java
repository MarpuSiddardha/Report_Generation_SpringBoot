package com.Siddhu.ReportGenerationproject.Repository;

import com.Siddhu.ReportGenerationproject.Entity.ScheduleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ScheduleConfigRepository extends JpaRepository<ScheduleConfig, Long> {

    List<ScheduleConfig> findByReportTypeAndScheduleInterval(String reportType, String scheduleInterval);

    List<ScheduleConfig> findByScheduleInterval(String scheduleInterval);
}

