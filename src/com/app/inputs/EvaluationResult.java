package com.app.inputs;

public class EvaluationResult {
    private final Employee employee;
    private final double rawScore;
    private final double normalizedScore;
    private final String rating;
    private final boolean bonusEligible;
    private final boolean promotionCandidate;

    public EvaluationResult(Employee employee,
                            double rawScore,
                            double normalizedScore,
                            String rating,
                            boolean bonusEligible,
                            boolean promotionCandidate) {
        this.employee = employee;
        this.rawScore = rawScore;
        this.normalizedScore = normalizedScore;
        this.rating = rating;
        this.bonusEligible = bonusEligible;
        this.promotionCandidate = promotionCandidate;
    }

    public Employee getEmployee() { return employee; }
    public double getRawScore() { return rawScore; }
    public double getNormalizedScore() { return normalizedScore; }
    public String getRating() { return rating; }
    public boolean isBonusEligible() { return bonusEligible; }
    public boolean isPromotionCandidate() { return promotionCandidate; }
}
