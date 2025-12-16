package com.app.apps;

import com.app.inputs.*;
import com.app.exception.*;
import com.app.evaluator.*;
import com.app.policies.*;
import com.app.summary.*;
import com.app.export.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    private final Map<String, Employee> employees = new HashMap<>();
    private final Map<String, List<KpiRecord>> kpiByEmployee = new HashMap<>();
    private final Map<String, EvaluationResult> evaluations = new HashMap<>();
    private final Map<String, DeptSummary> deptSummaries = new HashMap<>();

    public static void main(String[] args) {
        new Main().runMenu();
    }

    private void runMenu() {
        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            printMenu();
            while (!sc.hasNextInt()) {
                System.out.print("Enter a number: ");
                sc.next();
            }
            choice = sc.nextInt();
            sc.nextLine();

            try {
                switch (choice) {
                    case 1 -> loadFiles(sc);
                    case 2 -> evaluateAll();
                    case 3 -> topN(sc);
                    case 4 -> exportReports();
                    case 5 -> showPoorPerformers();   // ✅ NEW
                    case 6 -> System.out.println("Exiting...");
                    default -> System.out.println("Invalid choice.");
                }
            } catch (IOException | RuntimeException ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }

        } while (choice != 6);
    }

    private void printMenu() {
        System.out.println("----EMPLOYEE PERFORMANCE ENGINE----");
        System.out.println("1. Load Employee & KPI Files");
        System.out.println("2. Evaluate Performance");
        System.out.println("3. Show Top N Employees");
        System.out.println("4. Export Reports");
        System.out.println("5. Show Poor Performance Employees"); // ✅ NEW
        System.out.println("6. Exit");
        System.out.print("Enter choice: ");
    }

    private void loadFiles(Scanner sc) throws IOException {
        System.out.print("Enter employees file path: ");
        String empFile = sc.nextLine().trim();
        System.out.print("Enter KPI file path: ");
        String kpiFile = sc.nextLine().trim();

        loadEmployees(empFile);
        loadKpis(kpiFile);

        System.out.println("Files loaded successfully!");
    }

    private void loadEmployees(String filePath) throws IOException {
        employees.clear();
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        int lineno = 0;

        for (String rawLine : lines) {
            lineno++;
            if (rawLine == null) continue;

            String line = rawLine.replace("\uFEFF", "").trim();
            if (line.isBlank() || line.startsWith("#")) continue;

            String[] parts = line.contains(",")
                    ? line.split("\\s*,\\s*")
                    : line.split("\\s+");

            String first = parts[0].toLowerCase();
            if (lineno == 1 &&
                    (first.equals("id") || first.equals("empid") || first.equals("employee"))) {
                continue;
            }

            if (parts.length < 3) continue;

            String id = parts[0].trim();
            String dept = parts[1].trim();
            String level = parts[2].trim();

            if (!id.isEmpty()) {
                employees.put(id, new Employee(id, dept, level));
            }
        }

        System.out.printf("Loaded %d employees from %s%n", employees.size(), filePath);
    }

    private void loadKpis(String filePath) throws IOException {
        kpiByEmployee.clear();
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        for (String rawLine : lines) {
            if (rawLine == null) continue;

            String line = rawLine.replace("\uFEFF", "").trim();
            if (line.isBlank() || line.startsWith("#")) continue;

            String[] parts = line.contains(",")
                    ? line.split("\\s*,\\s*")
                    : line.split("\\s+");

            if (parts.length < 4) continue;

            try {
                String id = parts[0].trim();
                String metric = parts[1].trim();
                double value = Double.parseDouble(parts[2].trim());
                double weight = Double.parseDouble(parts[3].trim());

                if (weight <= 0) continue;

                kpiByEmployee.computeIfAbsent(id, k -> new ArrayList<>())
                             .add(new KpiRecord(id, metric, value, weight));

            } catch (NumberFormatException ignored) {}
        }

        System.out.printf("Loaded KPIs for %d employees%n", kpiByEmployee.size());
    }

    private void evaluateAll() {
        if (employees.isEmpty()) throw new IllegalStateException("No employees loaded.");

        evaluations.clear();
        deptSummaries.clear();

        WeightingPolicy policy = new LinearWeightsPolicy();
        Map<String, Double> rawScores = new HashMap<>();

        for (Employee e : employees.values()) {
            List<KpiRecord> kpis = kpiByEmployee.get(e.getId());
            if (kpis == null || kpis.isEmpty())
                throw new MissingKpiException("Missing KPI records for " + e.getId());

            rawScores.put(e.getId(), policy.computeScore(kpis));
        }

        Map<String, DeptStats> statsByDept = computeDeptStats(rawScores);
        Evaluator evaluator = new DepartmentEvaluator(policy, statsByDept);
        DeptSummaryBuilder builder = new DeptSummaryBuilder();

        for (Employee e : employees.values()) {
            EvaluationResult result = evaluator.evaluate(e, kpiByEmployee.get(e.getId()));
            evaluations.put(e.getId(), result);
            builder.add(result);
        }

        deptSummaries.putAll(builder.build());
        System.out.println("Evaluation completed.");
    }

    private Map<String, DeptStats> computeDeptStats(Map<String, Double> rawScores) {
        Map<String, List<Double>> byDept = new HashMap<>();

        for (Employee e : employees.values()) {
            byDept.computeIfAbsent(e.getDept(), d -> new ArrayList<>())
                  .add(rawScores.getOrDefault(e.getId(), 0.0));
        }

        Map<String, DeptStats> stats = new HashMap<>();
        for (var entry : byDept.entrySet()) {
            List<Double> v = entry.getValue();
            double mean = v.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double std = Math.sqrt(v.stream().mapToDouble(x -> (x - mean) * (x - mean)).average().orElse(0));
            double min = v.stream().mapToDouble(Double::doubleValue).min().orElse(mean);
            double max = v.stream().mapToDouble(Double::doubleValue).max().orElse(mean);

            stats.put(entry.getKey(), new DeptStats(mean, std, min, max));
        }
        return stats;
    }

    private void topN(Scanner sc) {
        System.out.println("Top N : ");
		evaluations.values().stream()
                .sorted(Comparator.comparingDouble(EvaluationResult::getNormalizedScore).reversed())
                .limit(sc.nextInt())
                .forEach(r -> {
                    Employee e = r.getEmployee();
                    System.out.printf("ID=%s Dept=%s Score=%.4f Rating=%s%n",
                            e.getId(), e.getDept(), r.getNormalizedScore(), r.getRating());
                });
    }


    private void showPoorPerformers() {
        System.out.println("---- POOR PERFORMANCE EMPLOYEES ----");

        evaluations.values().stream()
                .filter(r -> "POOR".equalsIgnoreCase(r.getRating()))
                .sorted(Comparator.comparingDouble(EvaluationResult::getNormalizedScore))
                .forEach(r -> {
                    Employee e = r.getEmployee();
                    System.out.printf(
                            "ID=%s Dept=%s Level=%s Score=%.4f Rating=%s%n",
                            e.getId(), e.getDept(), e.getLevel(),
                            r.getNormalizedScore(), r.getRating()
                    );
                });
    }

    private void exportReports() throws IOException {
        List<ReportExporter> exporters = List.of(
                new RatingsCsvExporter(evaluations),
                new BonusListExporter(evaluations),
                new PromotionCandidatesExporter(evaluations),
                new DeptSummaryExporter(deptSummaries),
                new PoorPerformanceCsvExporter(evaluations)
        );

        for (ReportExporter exporter : exporters) {
            exporter.export();
        }

        System.out.println("All reports exported successfully.");
    }
}
