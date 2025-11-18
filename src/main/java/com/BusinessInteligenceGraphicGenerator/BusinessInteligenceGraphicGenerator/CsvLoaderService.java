package com.BusinessInteligenceGraphicGenerator.BusinessInteligenceGraphicGenerator;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CsvLoaderService {

    @Value("${bi.csv.path:classpath:NasdaqDataSource.csv}")
    private Resource csvResource;

    private final List<OHLCRecord> records = new ArrayList<>();

    private final DateTimeFormatter[] dateFormats = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy"), // Added this pattern
            DateTimeFormatter.ISO_LOCAL_DATE
    };

    private static final Logger logger = LoggerFactory.getLogger(CsvLoaderService.class);

    @PostConstruct
    public void init() throws Exception {
        // Load lines from csvResource; supports classpath: or file: prefixes via Resource
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvResource.getInputStream()))) {
            String header = br.readLine();
            if (header == null) return;
            String line;
            while ((line = br.readLine()) != null) {
                // Simple CSV split by comma; if your CSV has quoted commas, replace with a robust CSV parser
                String[] cols = line.split(",", -1);
                // Assume common columns: Date,Open,High,Low,Close,Volume  (order may vary)
                // We'll try to interpret by header positions
                // For simplicity here we'll assume standard order: Date,Open,High,Low,Close,Adj Close,Volume
                // But to be robust we map headers:
                // If header contains names, parse columns positions
                // For now assume Date,Open,High,Low,Close,Volume OR Date,Open,High,Low,Close,Adj Close,Volume
                try {
                    // Trim values
                    for (int i = 0; i < cols.length; i++) cols[i] = cols[i].trim().replaceAll("^\"|\"$", "");
                    // Try common patterns:
                    LocalDate date = parseDate(cols[0]);
                    double open = Double.parseDouble(cols[1]);
                    double high = Double.parseDouble(cols[2]);
                    double low = Double.parseDouble(cols[3]);
                    double close = Double.parseDouble(cols[4]);
                    long volume;
                    if (cols.length >= 7) {
                        // Many datasets: Date,Open,High,Low,Close,Adj Close,Volume
                        volume = parseLongSafe(cols[6]);
                    } else if (cols.length == 6) {
                        volume = parseLongSafe(cols[5]);
                    } else {
                        volume = 0L;
                    }
                    records.add(new OHLCRecord(date, open, high, low, close, volume));
                } catch (Exception ex) {
                    logger.error("Failed to parse line: {}. Error: {}", line, ex.getMessage());
                }
            }
        }
        // Ensure records are sorted ascending by date
        records.sort(Comparator.comparing(OHLCRecord::getDate));
    }

    private long parseLongSafe(String s) {
        if (s == null || s.isBlank()) return 0L;
        String cleaned = s.replaceAll("[^0-9]", "");
        if (cleaned.isBlank()) return 0L;
        return Long.parseLong(cleaned);
    }

    private LocalDate parseDate(String s) {
        for (DateTimeFormatter fmt : dateFormats) {
            try {
                return LocalDate.parse(s, fmt);
            } catch (Exception ignored) {}
        }
        // fallback: try ISO
        return LocalDate.parse(s);
    }

    public List<OHLCRecord> getRecords() {
        return List.copyOf(records);
    }
}