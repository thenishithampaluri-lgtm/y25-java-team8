package com.app.policies;

import java.util.List;
import com.app.inputs.KpiRecord;

public class ZScorePolicy implements WeightingPolicy {

    @Override
    public double computeScore(List<KpiRecord> kpis) {
        double mean = kpis.stream()
                .mapToDouble(KpiRecord::getValue)
                .average()
                .orElse(0);

        double stdDev = calculateStdDev(kpis, mean);

        if (stdDev == 0) {
            return 0;
        }

        return kpis.stream()
                .mapToDouble(k -> ((k.getValue() - mean) / stdDev) * k.getWeight())
                .sum();
    }

    private double calculateStdDev(List<KpiRecord> kpis, double mean) {
        double variance = kpis.stream()
                .mapToDouble(k -> Math.pow(k.getValue() - mean, 2))
                .average()
                .orElse(0);

        return Math.sqrt(variance);
    }
}
