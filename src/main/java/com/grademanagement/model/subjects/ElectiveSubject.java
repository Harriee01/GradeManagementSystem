package com.grademanagement.model.subjects;

// ElectiveSubject IS-A AbstractSubject and implements SubjectType
// ElectiveSubject IS-A AbstractSubject and implements SubjectType
public class ElectiveSubject extends AbstractSubject {
    private int creditHours = 3;  // Electives typically have fewer credits
    private String department;    // Which department offers this elective

    public ElectiveSubject(String name, String code) {
        super(name, code);
    }

    public ElectiveSubject(String name) {
        super(name);
    }

    public ElectiveSubject(String name, String code, int creditHours, String department) {
        super(name, code);
        this.creditHours = creditHours;
        this.department = department;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        if (creditHours < 1 || creditHours > 4) {
            throw new IllegalArgumentException("Credit hours must be between 1 and 4");
        }
        this.creditHours = creditHours;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String getSubjectType() {
        return "Elective";
    }

    @Override
    public boolean isMandatory() {
        return false;  // Elective subjects are optional
    }

    @Override
    public String toString() {
        if (department != null) {
            return String.format("%s [Elective, %d credits, Dept: %s]",
                    super.toString(), creditHours, department);
        }
        return String.format("%s [Elective, %d credits]", super.toString(), creditHours);
    }
}
