package io.github.lampajr.model;

public class Metric {
    public String value;
    public String unit;

    public Metric() {
    }

    public Metric(String value, String unit) {
        this.value = value;
        this.unit = unit;
    }
}
