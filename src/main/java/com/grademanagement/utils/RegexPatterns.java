package com.grademanagement.utils;

import java.util.regex.Pattern;

public class RegexPatterns {
    public static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static final Pattern PHONE = Pattern.compile(
            "^\\+?\\d{1,3}?[-.\\s]?\\(?\\d{1,4}\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}$");

    public static final Pattern STUDENT_ID = Pattern.compile("^[A-Z][A-Z0-9]{2,}$");

    public static final Pattern GRADE = Pattern.compile("^([0-9]{1,2}|100)(\\.[0-9]{1,2})?$");
}