package com.app.export;

import com.app.evaluator.EvaluationResult;
import com.app.inputs.Employee;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;

public class RatingsCsvExporter implements ReportExporter {

    private final Map<String, EvaluationResult> evaluations;

    public RatingsCsvExporter(Map<String, EvaluationResult> evaluations) {
        this.evaluations = evaluations;
    }

    @Override
    public void export() throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter("ratings.csv"))) {
            out.println("id,dept,level,rawScore,normalizedScore,rating");

            for (EvaluationResult r : evaluations.values()) {
                Employee e = r.getEmployee();
                out.printf(Locale.ROOT,
                        "%s,%s,%s,%.4f,%.4f,%s%n",
                        e.getId(), e.getDept(), e.getLevel(),
                        r.getRawScore(), r.getNormalizedScore(), r.getRating());
            }
        }
    }
}
