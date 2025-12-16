package com.app.export;

import com.app.evaluator.EvaluationResult;
import com.app.inputs.Employee;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

public class PoorPerformanceCsvExporter implements ReportExporter {

    private final Map<String, EvaluationResult> evaluations;

    public PoorPerformanceCsvExporter(Map<String, EvaluationResult> evaluations) {
        this.evaluations = evaluations;
    }

    @Override
    public void export() throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter("poor_performers.csv"))) {

            out.println("id,dept,level,normalizedScore,rating");

            evaluations.values().stream()
                    .filter(r -> "POOR".equalsIgnoreCase(r.getRating()))
                    .sorted(Comparator.comparingDouble(EvaluationResult::getNormalizedScore))
                    .forEach(r -> {
                        Employee e = r.getEmployee();
                        out.printf(
                                Locale.ROOT,
                                "%s,%s,%s,%.4f,%s%n",
                                e.getId(),
                                e.getDept(),
                                e.getLevel(),
                                r.getNormalizedScore(),
                                r.getRating()
                        );
                    });
        }
    }
}
