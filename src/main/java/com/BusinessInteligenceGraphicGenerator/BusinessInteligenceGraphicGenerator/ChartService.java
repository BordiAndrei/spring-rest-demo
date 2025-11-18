package com.BusinessInteligenceGraphicGenerator.BusinessInteligenceGraphicGenerator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataItem;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class ChartService {

    public BufferedImage createCloseLineChart(List<OHLCRecord> records, int width, int height) {
        TimeSeries ts = new TimeSeries("Close");
        for (OHLCRecord r : records) {
            Day d = new Day(java.util.Date.from(r.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            ts.addOrUpdate(d, r.getClose());
        }
        TimeSeriesCollection dataset = new TimeSeriesCollection(ts);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Close Price",
                "Date",
                "Price",
                dataset,
                false,
                false,
                false
        );
        styleTimeChart(chart);
        return chart.createBufferedImage(width, height);
    }

    public BufferedImage createMovingAverageChart(List<OHLCRecord> records, int width, int height, int window) {
        TimeSeries closeTs = new TimeSeries("Close");
        for (OHLCRecord r : records) {
            Day d = new Day(Date.from(r.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            closeTs.addOrUpdate(d, r.getClose());
        }
        TimeSeries ma = movingAverage(closeTs, window);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(closeTs);
        dataset.addSeries(ma);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Close and " + window + "-day Moving Average",
                "Date",
                "Price",
                dataset,
                true,
                false,
                false
        );
        styleTimeChart(chart);
        // color adjustments
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.ORANGE);
        plot.setRenderer(renderer);
        return chart.createBufferedImage(width, height);
    }

    private TimeSeries movingAverage(TimeSeries ts, int window) {
        TimeSeries ma = new TimeSeries(ts.getKey() + "[" + window + "]");
        for (int i = 0; i < ts.getItemCount(); i++) {
            int start = Math.max(0, i - window + 1);
            double sum = 0;
            for (int j = start; j <= i; j++) sum += ts.getValue(j).doubleValue();
            double avg = sum / (i - start + 1);
            ma.add(ts.getTimePeriod(i), avg);
        }
        ma.setKey("MA");
        return ma;
    }

    public BufferedImage createReturnsHistogram(List<OHLCRecord> records, int width, int height, int bins) {
        double[] returns = new double[Math.max(0, records.size() - 1)];
        for (int i = 1; i < records.size(); i++) {
            double prev = records.get(i - 1).getClose();
            double cur = records.get(i).getClose();
            returns[i - 1] = Math.log(cur / prev);
        }
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Log Returns", returns, bins);
        JFreeChart chart = ChartFactory.createHistogram("Log Returns Histogram", "Log Return", "Frequency", dataset);
        return chart.createBufferedImage(width, height);
    }

    public BufferedImage createVolumeBarChart(List<OHLCRecord> records, int width, int height, int numBars) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int start = Math.max(0, records.size() - numBars);
        for (int i = start; i < records.size(); i++) {
            OHLCRecord r = records.get(i);
            String label = r.getDate().toString();
            dataset.addValue(r.getVolume(), "Volume", label);
        }
        JFreeChart chart = ChartFactory.createBarChart("Volume", "Date", "Volume", dataset);
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(60, 120, 180));
        // reduce domain axis label clutter
        plot.getDomainAxis().setMaximumCategoryLabelWidthRatio(0.6f);
        return chart.createBufferedImage(width, height);
    }

    public BufferedImage createCandlestickChart(List<OHLCRecord> records, int width, int height) {
        int n = records.size();
        Date[] dates = new Date[n];
        double[] highs = new double[n];
        double[] lows = new double[n];
        double[] opens = new double[n];
        double[] closes = new double[n];
        double[] volumes = new double[n];

        for (int i = 0; i < n; i++) {
            OHLCRecord r = records.get(i);
            dates[i] = Date.from(r.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            highs[i] = r.getHigh();
            lows[i] = r.getLow();
            opens[i] = r.getOpen();
            closes[i] = r.getClose();
            volumes[i] = r.getVolume();
        }

        DefaultHighLowDataset dataset = new DefaultHighLowDataset("OHLC", dates, highs, lows, opens, closes, volumes);
        JFreeChart chart = ChartFactory.createHighLowChart("Candlestick", "Date", "Price", dataset, false);
        XYPlot plot = chart.getXYPlot();
        CandlestickRenderer renderer = new CandlestickRenderer();
        plot.setRenderer(renderer);
        styleTimeChart(chart);
        return chart.createBufferedImage(width, height);
    }

    private void styleTimeChart(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.lightGray);
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new java.text.SimpleDateFormat("yyyy-MM-dd"));
    }
}