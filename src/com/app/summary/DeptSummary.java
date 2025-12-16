package com.app.summary;

public class DeptSummary {
    private final String dept;
    private final double avgRawScore;
    private final double avgNormScore;
    private final int employeeCount;
    private final int bonusCount;
    private final int promoCount;
    private final String topPerformerId;

    public DeptSummary(String dept, double avgRawScore, double avgNormScore,
                       int employeeCount, int bonusCount, int promoCount, String topPerformerId) {
        this.dept = dept;
        this.avgRawScore = avgRawScore;
        this.avgNormScore = avgNormScore;
        this.employeeCount = employeeCount;
        this.bonusCount = bonusCount;
        this.promoCount = promoCount;
        this.topPerformerId = topPerformerId;
    }

    public String getDept() { return dept; }
    public double getAvgRawScore() { return avgRawScore; }
    public double getAvgNormScore() { return avgNormScore; }
    public int getEmployeeCount() { return employeeCount; }
    public int getBonusCount() { return bonusCount; }
    public int getPromoCount() { return promoCount; }
    public String getTopPerformerId() { return topPerformerId; }
}
