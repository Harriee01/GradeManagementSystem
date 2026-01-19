package com.grademanagement.service;

import com.grademanagement.model.Student;
import java.util.List;

public class NotificationService {

    public void sendLowGradeNotification(Student student, double threshold) {
        double avg = student.calculateAverageGrade();
        if (avg < threshold) {
            System.out.printf("Notification: Student %s has low average grade: %.2f\n",
                    student.getName(), avg);
        }
    }

    public void sendHonorsNotification(Student student) {
        if (student.isEligibleForHonors(student.calculateAverageGrade())) {
            System.out.printf("Notification: Student %s is eligible for honors!\n",
                    student.getName());
        }
    }

    public void batchSendNotifications(List<Student> students) {
        students.forEach(student -> {
            sendLowGradeNotification(student, 60.0);
            sendHonorsNotification(student);
        });
    }
}