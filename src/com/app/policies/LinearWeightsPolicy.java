package com.app.policies;

import com.app.inputs.KpiRecord;
import java.util.List;

public class LinearWeightsPolicy implements WeightingPolicy {
    @Override
    public double computeScore(List<KpiRecord> kpis) {
        return kpis.stream()
                .mapToDouble(k -> k.getValue() * k.getWeight())
                .sum();
    }
}
