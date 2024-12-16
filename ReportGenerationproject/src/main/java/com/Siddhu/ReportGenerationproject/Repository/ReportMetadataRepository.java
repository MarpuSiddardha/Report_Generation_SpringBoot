package com.Siddhu.ReportGenerationproject.Repository;

import com.Siddhu.ReportGenerationproject.Entity.ReportMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportMetadataRepository extends JpaRepository<ReportMetadata, Long> {
    List<ReportMetadata> findByReportType(String reportType);


    @Query("SELECT r FROM ReportMetadata r WHERE r.generatedTime BETWEEN :fromDate AND :toDate")
    List<ReportMetadata> findReportsByDateRange(@Param("fromDate") LocalDateTime fromDate,
                                                @Param("toDate") LocalDateTime toDate);

    @Query("SELECT r FROM ReportMetadata r WHERE r.generatedTime BETWEEN :fromDate AND :toDate AND r.reportType = :reportType")
    List<ReportMetadata> findReportsByDateRangeAndType(@Param("fromDate") LocalDateTime fromDate,
                                                       @Param("toDate") LocalDateTime toDate,
                                                       @Param("reportType") String reportType);

    Page<ReportMetadata> findAll(Pageable pageable);
}
