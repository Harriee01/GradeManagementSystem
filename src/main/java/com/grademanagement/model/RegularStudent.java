package com.grademanagement.model;

// RegularStudent IS-A Student
public class RegularStudent extends Student {
    // Constant for passing grade
    private static final double PASSING_GRADE = 50.0;

    public RegularStudent(String id, String name, ContactInfo contactInfo) {
        super(id, name, contactInfo);
    }

    // POLYMORPHISM: Override abstract method
    @Override
    public String getStudentType() {
        return "Regular Student";
    }

    // POLYMORPHISM: Implementation of interface method
    @Override
    public double getPassingGrade() {
        return PASSING_GRADE;  // Regular students need 50% to pass
    }

    // POLYMORPHISM: Implementation of interface method
    @Override
    public boolean isEligibleForHonors(double averageGrade) {
        // Regular students are not eligible for honors
        return false;
    }

    // Override to add specific regular student info
    @Override
    public String toString() {
        return super.toString() + " [Regular]";
    }
}
