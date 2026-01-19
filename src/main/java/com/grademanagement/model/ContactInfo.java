package com.grademanagement.model;
import java.util.regex.Pattern;


public class ContactInfo {
    private String email;
    private String phone;
    private String address;

    public ContactInfo(String email, String phone, String address) {
        setEmail(email);      // Validated setter
        setPhone(phone);      // Validated setter
        this.address = address;
    }



    public void setEmail(String email) {
        // Comprehensive email regex pattern
        Pattern emailPattern = Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        );
        if (!emailPattern.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        this.email = email;
    }

    public void setPhone(String phone) {
        // Supports multiple phone formats: (123) 456-7890, 123-456-7890, 1234567890
        Pattern phonePattern = Pattern.compile(
                "^\\+?\\d{1,3}?[-.\\s]?\\(?\\d{1,4}\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}$"
        );
        if (!phonePattern.matcher(phone).matches()) {
            throw new IllegalArgumentException("Invalid phone format: " + phone);
        }
        this.phone = phone;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
}
