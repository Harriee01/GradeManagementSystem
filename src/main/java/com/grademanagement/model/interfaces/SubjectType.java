package com.grademanagement.model.interfaces;

public interface SubjectType {
    // Get the name of the subject
    String getName();

    // Get the subject code
    String getCode();

    // Set or update the subject code
    void setCode(String code);

    String getSubjectType();   // e.g., "Core", "Elective"
    boolean isMandatory();     // true if subject is mandatory
}
