package com.app.inputs;

public class KpiRecord {
    private final String id;
    private final String metric;
    private final double value;
    private final double weight;

    public KpiRecord(String id, String metric, double value, double weight) {
        this.id = id;
        this.metric = metric;
        this.value = value;
        this.weight = weight;
    }

    public String getid() { return id; }
    public String getMetric() { return metric; }
    public double getValue() { return value; }
    public double getWeight() { return weight; }
}
