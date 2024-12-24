package com.Siddhu.ReportGenerationproject.Controller;

import com.Siddhu.ReportGenerationproject.Entity.SensorData;
import com.Siddhu.ReportGenerationproject.Service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;




@RestController
@RequestMapping("/sensor-data")
public class SensorDataController {


    @Autowired
    private SensorDataService sensorDataService;



    @PostMapping("/insert")
    public SensorData insertSensorData(@RequestParam String sensorName,
                                       @RequestParam Double value) {
        return sensorDataService.insertSensorData(sensorName, value);
    }

    @GetMapping("/view-all")
    public List<SensorData> viewSensorData() {
        return sensorDataService.viewSensorData();
    }

    @GetMapping("/sensor-Name")
    public List<SensorData> viewBySensorName(String sensorName) {
        return sensorDataService.viewBySensorName(sensorName);
    }

    private List<SensorData> fetchSensorData(String sensorName) {
        return sensorDataService.viewBySensorName(sensorName);
    }

    @GetMapping("/filter-by-date-sensorName")
    public List<SensorData> getSensorDataByDateRangeAndName(
            @RequestParam String sensorName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return sensorDataService.viewBySensorNameAndDateRange(sensorName, fromDate, toDate);
    }
}



