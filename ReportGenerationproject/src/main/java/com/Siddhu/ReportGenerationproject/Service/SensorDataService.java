package com.Siddhu.ReportGenerationproject.Service;

import com.Siddhu.ReportGenerationproject.Entity.SensorData;
import com.Siddhu.ReportGenerationproject.Repository.SensorDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SensorDataService {

    private static final Logger logger = LoggerFactory.getLogger(SensorDataService.class);

    private final SensorDataRepository sensorDataRepository;

    public SensorDataService(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    public SensorData insertSensorData(String sensorName, Double value) {
        if (sensorName == null || sensorName.trim().isEmpty()) {
            logger.error("Sensor name is empty or null");
            throw new IllegalArgumentException("Sensor name cannot be null or empty");
        }
        if (value == null) {
            logger.error("Invalid sensor value: {}", value);
            throw new IllegalArgumentException("Sensor value must be valid");
        }

        SensorData sensor = new SensorData();
        sensor.setSensorName(sensorName);
        sensor.setTimestamp(LocalDateTime.now());
        sensor.setValue(value);

        logger.info("Inserting new sensor data: SensorName={}, Value={}", sensorName, value);
        return sensorDataRepository.save(sensor);
    }

    public SensorData saveSensorData(SensorData sensorData) {
        if (sensorData.getSensorName() == null || sensorData.getSensorName().isEmpty()) {
            logger.error("Sensor name is required");
            throw new IllegalArgumentException("Sensor name cannot be null or empty");
        }

        logger.info("Saving sensor data: {}", sensorData);
        return sensorDataRepository.save(sensorData);
    }

    // View all sensor data
    public List<SensorData> viewSensorData() {
        logger.info("Fetching all sensor data");
        return sensorDataRepository.findAll();
    }

    // View sensor data by sensor name
    public List<SensorData> viewBySensorName(String sensorName) {
        if (sensorName == null || sensorName.trim().isEmpty()) {
            logger.error("Sensor name is empty or null");
            throw new IllegalArgumentException("Sensor name cannot be null or empty");
        }

        logger.info("Fetching sensor data for sensor: {}", sensorName);
        return sensorDataRepository.findBySensorName(sensorName);
    }

    public List<SensorData> viewBySensorNameAndDateRange(String sensorName, LocalDateTime fromDate, LocalDateTime toDate) {
        if (sensorName == null || sensorName.trim().isEmpty()) {
            throw new IllegalArgumentException("Sensor name cannot be null or empty");
        }
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("Date range cannot be null");
        }

        return sensorDataRepository.findBySensorNameAndDateRange(sensorName, fromDate, toDate);
    }
}
