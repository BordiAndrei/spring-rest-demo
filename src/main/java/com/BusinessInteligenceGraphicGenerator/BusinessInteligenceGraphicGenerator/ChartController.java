package com.BusinessInteligenceGraphicGenerator.BusinessInteligenceGraphicGenerator;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.imageio.ImageIO;

@RestController
@RequestMapping("/chart")
public class ChartController {

    private final CsvLoaderService csvLoaderService;
    private final ChartService chartService;

    public ChartController(CsvLoaderService csvLoaderService, ChartService chartService) {
        this.csvLoaderService = csvLoaderService;
        this.chartService = chartService;
    }

    @GetMapping(value = "/close", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> closeChart(
            @RequestParam(defaultValue = "1200") int width,
            @RequestParam(defaultValue = "600") int height
    ) throws Exception {
        List<OHLCRecord> records = csvLoaderService.getRecords();
        BufferedImage img = chartService.createCloseLineChart(records, width, height);
        return bufferedImageAsPng(img);
    }

    @GetMapping(value = "/moving-average", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> movingAverage(
            @RequestParam(defaultValue = "20") int window,
            @RequestParam(defaultValue = "1200") int width,
            @RequestParam(defaultValue = "600") int height
    ) throws Exception {
        List<OHLCRecord> records = csvLoaderService.getRecords();
        BufferedImage img = chartService.createMovingAverageChart(records, width, height, window);
        return bufferedImageAsPng(img);
    }

    @GetMapping(value = "/returns-hist", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> returnsHist(
            @RequestParam(defaultValue = "50") int bins,
            @RequestParam(defaultValue = "800") int width,
            @RequestParam(defaultValue = "600") int height
    ) throws Exception {
        List<OHLCRecord> records = csvLoaderService.getRecords();
        BufferedImage img = chartService.createReturnsHistogram(records, width, height, bins);
        return bufferedImageAsPng(img);
    }

    @GetMapping(value = "/volume", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> volumeChart(@RequestParam(defaultValue = "60") int numBars,
                                              @RequestParam(defaultValue = "1200") int width,
                                              @RequestParam(defaultValue = "400") int height) throws Exception {
        List<OHLCRecord> records = csvLoaderService.getRecords();
        BufferedImage img = chartService.createVolumeBarChart(records, width, height, numBars);
        return bufferedImageAsPng(img);
    }

    @GetMapping(value = "/candlestick", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> candlestick(@RequestParam(defaultValue = "1200") int width,
                                              @RequestParam(defaultValue = "600") int height) throws Exception {
        List<OHLCRecord> records = csvLoaderService.getRecords();
        BufferedImage img = chartService.createCandlestickChart(records, width, height);
        return bufferedImageAsPng(img);
    }

    private ResponseEntity<byte[]> bufferedImageAsPng(BufferedImage img) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] bytes = baos.toByteArray();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(bytes);
    }
}