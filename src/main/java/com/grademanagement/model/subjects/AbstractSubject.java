package com.grademanagement.model.subjects;
import com.grademanagement.model.interfaces.SubjectType;

// Abstract class for Subject - cannot be instantiated directly
public abstract class AbstractSubject implements SubjectType {
    protected String name;  // Protected: accessible to child classes
    protected String code;  // Subject code like "MATH101"

    public AbstractSubject(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public AbstractSubject(String name) {
        this(name, generateCodeFromName(name));
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    // Generate subject code from name (e.g., "Mathematics" -> "MATH")
    private static String generateCodeFromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "UNKN";
        }

        String[] words = name.split("\\s+");
        StringBuilder code = new StringBuilder();

        if (words.length == 1) {
            // Take first 4 letters of single word
            code.append(name.substring(0, Math.min(4, name.length())).toUpperCase());
        } else {
            // Take first letter of each word
            for (String word : words) {
                if (!word.isEmpty()) {
                    code.append(word.charAt(0));
                }
            }
            // Pad if necessary
            while (code.length() < 4) {
                code.append('X');
            }
            code.setLength(Math.min(4, code.length()));
        }

        return code.toString();
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, code);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AbstractSubject that = (AbstractSubject) obj;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
