package com.app.export;

import com.app.evaluator.EvaluationResult;
import com.app.inputs.Employee;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Map;

public class PromotionCandidatesExporter implements ReportExporter {

    private final Map<String, EvaluationResult> evaluations;

    public PromotionCandidatesExporter(Map<String, EvaluationResult> evaluations) {
        this.evaluations = evaluations;
    }

    @Override
    public void export() throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter("promotion_candidates.txt"))) {
            out.println("Promotion Candidates");
            out.println("====================");

            evaluations.values().stream()
                    .filter(EvaluationResult::isPromotionCandidate)
                    .sorted(Comparator.comparingDouble(EvaluationResult::getNormalizedScore).reversed())
                    .forEach(r -> {
                        Employee e = r.getEmployee();
                        out.printf(
                                "ID=%s, Dept=%s, Level=%s, NormScore=%.4f, Rating=%s%n",
                                e.getId(), e.getDept(), e.getLevel(),
                                r.getNormalizedScore(), r.getRating());
                    });
        }
    }
}
