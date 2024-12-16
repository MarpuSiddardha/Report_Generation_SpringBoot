package com.Siddhu.ReportGenerationproject.Repository;

import com.Siddhu.ReportGenerationproject.Entity.ScheduleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduleConfigRepository extends JpaRepository<ScheduleConfig, Long> {

    Optional<ScheduleConfig> findByReportType(String reportType);
}

