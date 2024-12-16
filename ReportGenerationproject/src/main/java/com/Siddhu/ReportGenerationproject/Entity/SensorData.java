package com.Siddhu.ReportGenerationproject.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
public class SensorData {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Use AUTO or SEQUENCE if required
    @Column(name = "sensor_id", nullable = false)
    private Long sensorId;

    @Column(name = "sensor_name")
    private String sensorName;

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getSensorName() {
        return sensorName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "value")
    private Double value;


    // Getters and setters
}
