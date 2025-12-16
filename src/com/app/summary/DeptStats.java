package com.app.summary;

public class DeptStats {
    private final double mean;
    private final double std;
    private final double min;
    private final double max;

    public DeptStats(double mean, double std, double min, double max) {
        this.mean = mean;
        this.std = std;
        this.min = min;
        this.max = max;
    }

    public double getMean() { return mean; }
    public double getStd() { return std; }
    public double getMin() { return min; }
    public double getMax() { return max; }
}
