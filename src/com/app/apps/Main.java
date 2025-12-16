package com.app.apps;
import com.app.inputs.*;
import com.app.exception.*;
import com.app.evaluator.*;
import com.app.policies.*;
import com.app.summary.*;
import java.io.*;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main{
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
                    case 5 -> System.out.println("Exiting...");
                    default -> System.out.println("Invalid choice.");
                }
            } catch (IOException | RuntimeException ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        } while (choice != 5);
    }

    private void printMenu() {
        System.out.println("----EMPLOYEE PERFORMANCE ENGINE----");
        System.out.println("1. Load Employee & KPI Files");
        System.out.println("2. Evaluate Performance");
        System.out.println("3. Show Top N Employees");
        System.out.println("4. Export Reports");
        System.out.println("5. Exit");

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
            String line = rawLine.replace("\uFEFF", "").trim(); // remove BOM and trim
            if (line.isBlank()) continue;
            if (line.startsWith("#")) continue; // allow comments

         
            String[] parts;
            if (line.contains(",")) {
                parts = line.split("\\s*,\\s*");
            } else {
                parts = line.split("\\s+");
            }

            
            String first = parts[0].toLowerCase();
            if (lineno == 1 && (first.equals("id") || first.equals("empid") || first.equals("employee"))) {
               
                continue;
            }

            if (parts.length < 3) {
                
                continue;
            }

            String id = parts[0].trim();
            String dept = parts[1].trim();
            String level = parts[2].trim();
            if (id.isEmpty()) {
                System.err.printf("SKIP employees file %s:%d -> empty id: '%s'%n", filePath, lineno, line);
                continue;
            }
            employees.put(id, new Employee(id, dept, level));
        }
        System.out.printf("Loaded %d employees from %s%n", employees.size(), filePath);
    }

    

    private void loadKpis(String filePath) throws IOException {
        kpiByEmployee.clear();
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        int lineno = 0;
        for (String rawLine : lines) {
            lineno++;
            if (rawLine == null) continue;
            String line = rawLine.replace("\uFEFF", "").trim();
            if (line.isBlank()) continue;
            if (line.startsWith("#")) continue;

            String[] parts;
            if (line.contains(",")) {
                parts = line.split("\\s*,\\s*");
            } else {
                parts = line.split("\\s+");
            }

            
            String first = parts[0].toLowerCase();
            if (lineno == 1 && (first.equals("id") || first.equals("employee") || first.equals("empid") || first.equals("metric"))) {
                
                continue;
            }

            if (parts.length < 4) {
               
                continue;
            }

            String id = parts[0].trim();
            String metric = parts[1].trim();

            double value;
            double weight;
            try {
                value = Double.parseDouble(parts[2].trim());
            } catch (NumberFormatException nfe) {
                System.err.printf("SKIP KPI file %s:%d -> invalid value: '%s'%n", filePath, lineno, parts[2]);
                continue;
            }
            try {
                weight = Double.parseDouble(parts[3].trim());
            } catch (NumberFormatException nfe) {
                System.err.printf("SKIP KPI file %s:%d -> invalid weight: '%s'%n", filePath, lineno, parts[3]);
                continue;
            }
            if (weight <= 0) {
                System.err.printf("SKIP KPI file %s:%d -> non-positive weight: %s%n", filePath, lineno, weight);
                continue;
            }

            KpiRecord rec = new KpiRecord(id, metric, value, weight);
            kpiByEmployee.computeIfAbsent(id, k -> new ArrayList<>()).add(rec);
        }
        System.out.printf("Loaded KPIs for %d employees from %s%n", kpiByEmployee.size(), filePath);
    }


    private void evaluateAll() {
        if (employees.isEmpty()) {
            throw new IllegalStateException("No employees loaded. Use option 1.");
        }
        evaluations.clear();
        deptSummaries.clear();

        WeightingPolicy policy = new LinearWeightsPolicy();
        Map<String, Double> rawScores = new HashMap<>();

        for (Employee e : employees.values()) {
            List<KpiRecord> kpis = kpiByEmployee.get(e.getId());
            if (kpis == null || kpis.isEmpty()) throw new MissingKpiException("Missing KPI records for " + e.getId());
            double raw = policy.computeScore(kpis);
            rawScores.put(e.getId(), raw);
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
        System.out.println("Evaluation completed for " + evaluations.size() + " employees.");
    }

    private Map<String, DeptStats> computeDeptStats(Map<String, Double> rawScores) {
        Map<String, List<Double>> byDept = new HashMap<>();
        for (Employee e : employees.values()) {
            double s = rawScores.getOrDefault(e.getId(), 0.0);
            byDept.computeIfAbsent(e.getDept(), d -> new ArrayList<>()).add(s);
        }

        Map<String, DeptStats> stats = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : byDept.entrySet()) {
            List<Double> vals = entry.getValue();
            double mean = vals.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double variance = vals.stream().mapToDouble(v -> (v - mean) * (v - mean)).average().orElse(0.0);
            double std = Math.sqrt(variance);
            double min = vals.stream().mapToDouble(Double::doubleValue).min().orElse(mean);
            double max = vals.stream().mapToDouble(Double::doubleValue).max().orElse(mean);
            stats.put(entry.getKey(), new DeptStats(mean, std, min, max));
        }
        return stats;
    }

    private void topN(Scanner sc) {
        if (evaluations.isEmpty()) {
            System.out.println("No evaluations. Run option 2 first.");
            return;
        }
        System.out.print("Enter N: ");
        while (!sc.hasNextInt()) {
            System.out.print("Enter a number: ");
            sc.next();
        }
        int n = sc.nextInt();
        sc.nextLine();

        evaluations.values().stream()
                .sorted(Comparator.comparingDouble(EvaluationResult::getNormalizedScore).reversed())
                .limit(n)
                .forEach(r -> {
                    Employee e = r.getEmployee();
                    System.out.printf("ID=%s Dept=%s Level=%s Norm=%.4f Rating=%s Bonus=%s Promo=%s%n",
                            e.getId(), e.getDept(), e.getLevel(),
                            r.getNormalizedScore(), r.getRating(),
                            r.isBonusEligible(), r.isPromotionCandidate());
                });
    }

    private void exportReports() throws IOException {
        if (evaluations.isEmpty()) throw new IllegalStateException("No evaluations to export. Run option 2 first.");

        // ratings.csv (whitespace-safe CSV: still comma-free — values separated by commas for readability)
        try (PrintWriter out = new PrintWriter(new FileWriter("ratings.csv"))) {
            out.println("id,dept,level,rawScore,normalizedScore,rating");
            for (EvaluationResult r : evaluations.values()) {
                Employee e = r.getEmployee();
                out.printf(Locale.ROOT, "%s,%s,%s,%.4f,%.4f,%s%n",
                        e.getId(), e.getDept(), e.getLevel(),
                        r.getRawScore(), r.getNormalizedScore(), r.getRating());
            }
        }

        // bonus_list.csv
        try (PrintWriter out = new PrintWriter(new FileWriter("bonus_list.csv"))) {
            out.println("id,dept,level,normalizedScore,rating");
            evaluations.values().stream()
                    .filter(EvaluationResult::isBonusEligible)
                    .sorted(Comparator.comparingDouble(EvaluationResult::getNormalizedScore).reversed())
                    .forEach(r -> {
                        Employee e = r.getEmployee();
                        out.printf(Locale.ROOT, "%s,%s,%s,%.4f,%s%n",
                                e.getId(), e.getDept(), e.getLevel(),
                                r.getNormalizedScore(), r.getRating());
                    });
        }

        // promotion_candidates.txt
        try (PrintWriter out = new PrintWriter(new FileWriter("promotion_candidates.txt"))) {
            out.println("Promotion Candidates");
            out.println("====================");
            evaluations.values().stream()
                    .filter(EvaluationResult::isPromotionCandidate)
                    .sorted(Comparator.comparingDouble(EvaluationResult::getNormalizedScore).reversed())
                    .forEach(r -> {
                        Employee e = r.getEmployee();
                        out.printf("ID=%s, Dept=%s, Level=%s, NormScore=%.4f, Rating=%s%n",
                                e.getId(), e.getDept(), e.getLevel(),
                                r.getNormalizedScore(), r.getRating());
                    });
        }

        // dept_summary.csv
        try (PrintWriter out = new PrintWriter(new FileWriter("dept_summary.csv"))) {
            out.println("dept,avgRawScore,avgNormalizedScore,employeeCount,bonusCount,promoCount,topPerformerId");
            for (DeptSummary s : deptSummaries.values()) {
                out.printf(Locale.ROOT, "%s,%.4f,%.4f,%d,%d,%d,%s%n",
                        s.getDept(), s.getAvgRawScore(), s.getAvgNormScore(),
                        s.getEmployeeCount(), s.getBonusCount(), s.getPromoCount(),
                        s.getTopPerformerId());
            }
        }

        System.out.println("Exported ratings.csv, bonus_list.csv, promotion_candidates.txt, dept_summary.csv");
    }
}