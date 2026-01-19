package com.grademanagement.model;

// HonorsStudent IS-A Student
public class HonorsStudent extends Student {
    // Constants for honors student
    private static final double PASSING_GRADE = 60.0;
    private static final double HONORS_THRESHOLD = 85.0;
    private String honorsProgram;  // Specific honors program

    public HonorsStudent(String id, String name, ContactInfo contactInfo) {
        super(id, name, contactInfo);
        this.honorsProgram = "General Honors";
    }

    public HonorsStudent(String id, String name, ContactInfo contactInfo, String honorsProgram) {
        super(id, name, contactInfo);
        this.honorsProgram = honorsProgram;
    }

    public String getHonorsProgram() {
        return honorsProgram;
    }

    public void setHonorsProgram(String honorsProgram) {
        this.honorsProgram = honorsProgram;
    }

    // POLYMORPHISM: Override abstract method
    @Override
    public String getStudentType() {
        return "Honors Student";
    }

    // POLYMORPHISM: Implementation of interface method
    @Override
    public double getPassingGrade() {
        return PASSING_GRADE;  // Honors students need 60% to pass
    }

    // POLYMORPHISM: Implementation of interface method
    @Override
    public boolean isEligibleForHonors(double averageGrade) {
        // Honors students need 85% average for honors recognition
        return averageGrade >= HONORS_THRESHOLD;
    }

    // Check if eligible for graduation with honors
//    public boolean isEligibleForGraduationHonors() {
//        double gpa = calculateWeightedGPA();
//        return gpa >= 3.5 && getPassRate() >= 90.0;
//    }

    // POLYMORPHISM: Override toString to add honors info
    @Override
    public String toString() {
        String baseInfo = super.toString();
        double avg = calculateAverageGrade();
        return baseInfo + String.format(", Honors Program: %s, Honors Eligible: %s",
                honorsProgram, isEligibleForHonors(avg) ? "YES" : "NO");
    }
}
