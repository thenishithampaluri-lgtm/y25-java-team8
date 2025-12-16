package com.app.evaluator;

import com.app.inputs.Employee;
import com.app.policies.WeightingPolicy;
import com.app.summary.DeptStats;

import java.util.Map;

public class DepartmentEvaluator extends Evaluator {

    private final Map<String, DeptStats> statsByDept;

    public DepartmentEvaluator(WeightingPolicy weightingPolicy,
                               Map<String, DeptStats> statsByDept) {
        super(weightingPolicy);
        this.statsByDept = statsByDept;
    }

    @Override
    protected double normalize(Employee employee, double rawScore) {
        DeptStats stats = statsByDept.get(employee.getDept());
        if (stats == null) return 0.0;

        if (stats.getStd() == 0) {
            double range = stats.getMax() - stats.getMin();
            if (range == 0) return 0.0;
            return (rawScore - stats.getMin()) / range;
        }

        return (rawScore - stats.getMean()) / stats.getStd();
    }
}
