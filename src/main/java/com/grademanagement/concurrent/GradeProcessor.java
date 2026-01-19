package com.grademanagement.concurrent;

import com.grademanagement.model.Grade;
import java.util.List;
import java.util.concurrent.Callable;

public class GradeProcessor implements Callable<Double> {
    private final List<Grade> grades;

    public GradeProcessor(List<Grade> grades) {
        this.grades = grades;
    }

    @Override
    public Double call() throws Exception {
        return grades.stream()
                .mapToDouble(Grade::getScore)
                .average()
                .orElse(0.0);
    }
}