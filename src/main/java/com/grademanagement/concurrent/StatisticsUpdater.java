package com.grademanagement.concurrent;

import com.grademanagement.model.Student;
import java.util.List;
import java.util.concurrent.Callable;

public class StatisticsUpdater implements Callable<Double> {
    private final List<Student> students;

    public StatisticsUpdater(List<Student> students) {
        this.students = students;
    }

    @Override
    public Double call() throws Exception {
        double sum = 0;
        int count = 0;
        for (Student student : students) {
            sum += student.calculateAverageGrade();
            count++;
        }
        return count > 0 ? sum / count : 0.0;
    }
}