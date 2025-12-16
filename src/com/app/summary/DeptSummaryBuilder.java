package com.app.summary;

import com.app.inputs.EvaluationResult;

import java.util.*;

public class DeptSummaryBuilder {

    private final Map<String, List<EvaluationResult>> byDept = new HashMap<>();

    public void add(EvaluationResult r) {
        String dept = r.getEmployee().getDept();
        byDept.computeIfAbsent(dept, d -> new ArrayList<>()).add(r);
    }

    public Map<String, DeptSummary> build() {
        Map<String, DeptSummary> result = new HashMap<>();

        for (var entry : byDept.entrySet()) {
            String dept = entry.getKey();
            List<EvaluationResult> list = entry.getValue();

            double avgRaw = list.stream().mapToDouble(EvaluationResult::getRawScore).average().orElse(0.0);
            double avgNorm = list.stream().mapToDouble(EvaluationResult::getNormalizedScore).average().orElse(0.0);
            int count = list.size();
            int bonus = (int) list.stream().filter(EvaluationResult::isBonusEligible).count();
            int promo = (int) list.stream().filter(EvaluationResult::isPromotionCandidate).count();

            String top = list.stream()
                    .max(Comparator.comparingDouble(EvaluationResult::getNormalizedScore))
                    .map(r -> r.getEmployee().getId())
                    .orElse("");

            result.put(dept, new DeptSummary(dept, avgRaw, avgNorm, count, bonus, promo, top));
        }
        return result;
    }
}
