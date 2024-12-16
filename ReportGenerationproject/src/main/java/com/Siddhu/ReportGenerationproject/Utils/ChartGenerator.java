package com.Siddhu.ReportGenerationproject.Utils;

import com.Siddhu.ReportGenerationproject.Entity.SensorData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class ChartGenerator {

    private Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    // Line chart
    public BufferedImage generateLineChart(List<SensorData> sensorDataList, String sensorName) {
        // Create dataset for line chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (SensorData data : sensorDataList) {
            dataset.addValue(data.getValue(), sensorName, data.getTimestamp().toString());
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Sensor Data Over Time",  // chart title
                "Time",                   // x axis label
                "Sensor Value",           // y axis label
                dataset                   // dataset
        );

        lineChart.setBackgroundPaint(Color.white);
        CategoryPlot plot = lineChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.black);

        BufferedImage chartImage = lineChart.createBufferedImage(800, 500);
        return chartImage;
    }

    //Bar Chart
    public File generateBarChart(String sensorName, double min, double max, double avg, String filePath) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(min, "Min", "Minimum");
        dataset.addValue(max, "Max", "Maximum");
        dataset.addValue(avg, "Avg", "Average");

        JFreeChart chart = ChartFactory.createBarChart(
                "Aggregated Sensor Data - " + sensorName, // Chart title
                "Metrics",                               // X-axis label
                "Value",                                 // Y-axis label
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        BarRenderer renderer = new BarRenderer();
        renderer.setSeriesPaint(0, new Color(79, 129, 189)); // Example custom color
        chart.getCategoryPlot().setRenderer(renderer);

        File chartFile = new File(filePath);
        ChartUtils.saveChartAsPNG(chartFile, chart, 800, 500);
        return chartFile;
    }
}
