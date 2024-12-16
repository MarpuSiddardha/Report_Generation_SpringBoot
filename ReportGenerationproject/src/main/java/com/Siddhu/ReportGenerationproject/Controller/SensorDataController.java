package com.Siddhu.ReportGenerationproject.Controller;
import com.Siddhu.ReportGenerationproject.Entity.SensorData;
import com.Siddhu.ReportGenerationproject.Service.SensorDataService;
import com.Siddhu.ReportGenerationproject.Utils.ChartGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;




@RestController
@RequestMapping("/sensor-data")
public class SensorDataController {


    @Autowired
    private SensorDataService sensorDataService;

    @Autowired
    private final ChartGenerator chartGenerator;

    public SensorDataController(SensorDataService sensorDataService, ChartGenerator chartGenerator) {
        this.sensorDataService = sensorDataService;
        this.chartGenerator = chartGenerator;
    }

    @PostMapping("/insert")
    public SensorData insertSensorData(@RequestParam String sensorName,
                                                                         @RequestParam Double value) {
        return sensorDataService.insertSensorData(sensorName,value);
    }

//    @PostMapping
//    public ResponseEntity<String> saveSensorData(@RequestBody SensorData sensorData) {
//        sensorDataService.saveSensorData(sensorData);
//        return ResponseEntity.ok("Sensor data saved successfully!");
//    }

    @GetMapping("/view-all")
    public List<SensorData>  viewSensorData() {
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


    @GetMapping("/line-chart")
    public ResponseEntity<byte[]> getLineChart(@RequestParam String sensorName) throws IOException {
        List<SensorData> sensorDataList = fetchSensorData(sensorName);


        BufferedImage chartImage = chartGenerator.generateLineChart(sensorDataList, sensorName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(chartImage, "PNG", baos);
        byte[] chartData = baos.toByteArray();


        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(chartData);
    }

    @GetMapping(value = "/bar-chart", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getBarChart(@RequestParam String sensorName) throws IOException {
        List<SensorData> sensorDataList = sensorDataService.viewBySensorName(sensorName);


        double avg = sensorDataList.stream().mapToDouble(SensorData::getValue).average().orElse(0.0);
        double min = sensorDataList.stream().mapToDouble(SensorData::getValue).min().orElse(0.0);
        double max = sensorDataList.stream().mapToDouble(SensorData::getValue).max().orElse(0.0);

        File chartFile = chartGenerator.generateBarChart(sensorName, min, max, avg, "bar-chart.png");
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(java.nio.file.Files.readAllBytes(chartFile.toPath()));
    }
}
