package com.Siddhu.ReportGenerationproject.Utils;

import com.Siddhu.ReportGenerationproject.Entity.ReportMetadata;
import com.Siddhu.ReportGenerationproject.Entity.SensorData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ReportPdfGenerator {

    public void generateReport(ReportMetadata report, List<SensorData> sensorDataList, String sensorType) {

        double averageValue = getAverageValue(sensorDataList);
        double minValue = getMinValue(sensorDataList);
        double maxValue = getMaxValue(sensorDataList);


        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.setLeading(14.5f);


            contentStream.newLineAtOffset(25, 750);  // Set position
            contentStream.showText("Report: " + report.getReportName());
            contentStream.newLine();
            contentStream.showText("Generated at: " + report.getGeneratedTime());
            contentStream.newLine();
            contentStream.showText("Report Type: " + report.getReportType());
            contentStream.newLine();
            contentStream.newLine();


            contentStream.showText("The Aggregations for the " +report.getReportType()+ " are : ");
            contentStream.newLine();
            contentStream.newLine();
            contentStream.showText(sensorType + " Average: " + averageValue);
            contentStream.newLine();
            contentStream.showText(sensorType + " Minimum: " + minValue);
            contentStream.newLine();
            contentStream.showText(sensorType + " Maximum: " + maxValue);

            contentStream.endText();
            contentStream.close();

            document.save(report.getFilePath());
        } catch (IOException e) {
            // Handle exceptions
            System.err.println("Error generating the report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public double getAverageValue(List<SensorData> sensorDataList) {
        return sensorDataList.stream()
                .mapToDouble(SensorData::getValue)
                .average()
                .orElse(0.0);
    }

    private double getMinValue(List<SensorData> sensorDataList) {
        return sensorDataList.stream()
                .mapToDouble(SensorData::getValue)
                .min()
                .orElse(0.0);
    }

    private double getMaxValue(List<SensorData> sensorDataList) {
        return sensorDataList.stream()
                .mapToDouble(SensorData::getValue)
                .max()
                .orElse(0.0);
    }
}
