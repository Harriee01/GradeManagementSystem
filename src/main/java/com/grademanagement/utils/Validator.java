package com.grademanagement.utils;

import java.util.regex.Pattern;

public class Validator {

    public static boolean validateEmail(String email) {
        return email != null && RegexPatterns.EMAIL.matcher(email).matches();
    }

    public static boolean validatePhone(String phone) {
        return phone != null && RegexPatterns.PHONE.matcher(phone).matches();
    }

    public static boolean validateStudentId(String id) {
        return id != null && RegexPatterns.STUDENT_ID.matcher(id).matches();
    }

    public static boolean validateGrade(String grade) {
        return grade != null && RegexPatterns.GRADE.matcher(grade).matches();
    }
}