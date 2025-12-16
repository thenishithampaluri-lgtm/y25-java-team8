package com.app.evaluator;

import com.app.inputs.*;
import com.app.policies.WeightingPolicy;
import java.util.List;

public abstract class Evaluator {

    protected final WeightingPolicy weightingPolicy;

    protected Evaluator(WeightingPolicy weightingPolicy) {
        this.weightingPolicy = weightingPolicy;
    }

    public final EvaluationResult evaluate(Employee employee, List<KpiRecord> kpis) {
        double raw = weightingPolicy.computeScore(kpis);
        double norm = normalize(employee, raw);
        String rating = assignRating(norm);
        boolean bonus = isBonusEligible(norm, rating);
        boolean promo = isPromotionCandidate(employee, norm, rating);

        return new EvaluationResult(employee, raw, norm, rating, bonus, promo);
    }

    protected abstract double normalize(Employee employee, double rawScore);

    protected String assignRating(double score) {
        if (score >= 1.5) return "Outstanding";
        if (score >= 0.75) return "Exceeds Expectations";
        if (score >= 0) return "Meets Expectations";
        if (score >= -0.75) return "Below Expectations";
        return "Unsatisfactory";
    }

    protected boolean isBonusEligible(double score, String rating) {
        return score >= 0.25 && !rating.equals("Below Expectations") && !rating.equals("Unsatisfactory");
    }

    protected boolean isPromotionCandidate(Employee e, double score, String rating) {
        boolean strong = rating.equals("Outstanding") || rating.equals("Exceeds Expectations");
        boolean notSenior = !e.getLevel().equalsIgnoreCase("Senior") &&
                            !e.getLevel().equalsIgnoreCase("Principal");
        return score >= 1.0 && strong && notSenior;
    }
}
 