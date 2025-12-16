package com.app.policies;

import com.app.inputs.KpiRecord;
import java.util.List;

public interface WeightingPolicy {
    double computeScore(List<KpiRecord> kpis);
}
