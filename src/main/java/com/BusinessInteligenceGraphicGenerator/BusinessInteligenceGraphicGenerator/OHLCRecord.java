package com.BusinessInteligenceGraphicGenerator.BusinessInteligenceGraphicGenerator;

import lombok.Getter;

import java.time.LocalDate;

public class OHLCRecord {

    private LocalDate date;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;

    public OHLCRecord(LocalDate date, double open, double high, double low, double close, long volume) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    // No-arg constructor optional (useful pentru frameworks)
    public OHLCRecord() { }

    public LocalDate getDate() {
        return date;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public long getVolume() {
        return volume;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }
}