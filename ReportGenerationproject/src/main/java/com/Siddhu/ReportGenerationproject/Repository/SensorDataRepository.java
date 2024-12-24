package com.Siddhu.ReportGenerationproject.Repository;

import com.Siddhu.ReportGenerationproject.Entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    List<SensorData> findBySensorName(String sensorName);

    @Query("SELECT s FROM SensorData s WHERE s.sensorName = :sensorName AND s.timestamp BETWEEN :fromDate AND :toDate")
    List<SensorData> findBySensorNameAndDateRange(
            @Param("sensorName") String sensorName,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

}

