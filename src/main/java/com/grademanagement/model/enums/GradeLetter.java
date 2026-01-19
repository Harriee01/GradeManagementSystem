package com.grademanagement.model.enums;

// Enum for grade letters with corresponding GPA values
public enum GradeLetter {
    A_PLUS("A+", 4.0),
    A("A", 4.0),
    A_MINUS("A-", 3.7),
    B_PLUS("B+", 3.3),
    B("B", 3.0),
    B_MINUS("B-", 2.7),
    C_PLUS("C+", 2.3),
    C("C", 2.0),
    C_MINUS("C-", 1.7),
    D_PLUS("D+", 1.3),
    D("D", 1.0),
    F("F", 0.0);

    private final String letter;
    private final double gpaValue;

    GradeLetter(String letter, double gpaValue) {
        this.letter = letter;
        this.gpaValue = gpaValue;
    }

    public String getLetter() {
        return letter;
    }

    public double getGpaValue() {
        return gpaValue;
    }

    // Convert percentage to grade letter
    public static GradeLetter fromPercentage(double percentage) {
        if (percentage >= 97) return A_PLUS;
        else if (percentage >= 93) return A;
        else if (percentage >= 90) return A_MINUS;
        else if (percentage >= 87) return B_PLUS;
        else if (percentage >= 83) return B;
        else if (percentage >= 80) return B_MINUS;
        else if (percentage >= 77) return C_PLUS;
        else if (percentage >= 73) return C;
        else if (percentage >= 70) return C_MINUS;
        else if (percentage >= 67) return D_PLUS;
        else if (percentage >= 65) return D;
        else return F;
    }
}
