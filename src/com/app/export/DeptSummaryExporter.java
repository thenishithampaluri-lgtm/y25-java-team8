package com.app.export;

import com.app.summary.DeptSummary;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;

public class DeptSummaryExporter implements ReportExporter {

    private final Map<String, DeptSummary> deptSummaries;

    public DeptSummaryExporter(Map<String, DeptSummary> deptSummaries) {
        this.deptSummaries = deptSummaries;
    }

    @Override
    public void export() throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter("dept_summary.csv"))) {
            out.println("dept,avgRawScore,avgNormalizedScore,employeeCount,bonusCount,promoCount,topPerformerId");

            for (DeptSummary s : deptSummaries.values()) {
                out.printf(Locale.ROOT,
                        "%s,%.4f,%.4f,%d,%d,%d,%s%n",
                        s.getDept(),
                        s.getAvgRawScore(),
                        s.getAvgNormScore(),
                        s.getEmployeeCount(),
                        s.getBonusCount(),
                        s.getPromoCount(),
                        s.getTopPerformerId());
            }
        }
    }
}
