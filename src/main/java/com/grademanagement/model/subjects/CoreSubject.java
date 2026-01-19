package com.grademanagement.model.subjects;

// CoreSubject IS-A AbstractSubject and implements SubjectType
public class CoreSubject extends AbstractSubject {
    private int creditHours = 4;  // Core subjects typically have more credits

    public CoreSubject(String name, String code) {
        super(name, code);
    }

    public CoreSubject(String name) {
        super(name);
    }

    public CoreSubject(String name, String code, int creditHours) {
        super(name, code);
        this.creditHours = creditHours;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        if (creditHours < 1 || creditHours > 6) {
            throw new IllegalArgumentException("Credit hours must be between 1 and 6");
        }
        this.creditHours = creditHours;
    }

    @Override
    public String getSubjectType() {
        return "Core";
    }

    @Override
    public boolean isMandatory() {
        return true;  // Core subjects are always mandatory
    }

    @Override
    public String toString() {
        return String.format("%s [Core, %d credits]", super.toString(), creditHours);
    }
}
