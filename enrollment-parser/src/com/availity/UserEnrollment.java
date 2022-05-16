package com.availity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UserEnrollment implements Comparable<UserEnrollment> {
    private String userId;
    private String name;
    private Integer version;
    private String insuranceCompany;
    private String error = "";

    public UserEnrollment() {}

    public UserEnrollment(String userId, String name, Integer version, String insuranceCompany) {
        this.userId = userId;
        this.name = name;
        this.version = version;
        this.insuranceCompany = insuranceCompany;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getInsuranceCompany() {
        return insuranceCompany;
    }

    public void setInsuranceCompany(String insuranceCompany) {
        this.insuranceCompany = insuranceCompany;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    // Overriding the default compareTo method to compare the last name. If the last name is the same, then compare the first name.
    public int compareTo(UserEnrollment userEnrollment) {
        return getLastName(this.getName()).compareTo(getLastName(userEnrollment.getName())) == 0 ? compareFirstNameTo(userEnrollment) : getLastName(this.getName()).compareTo(getLastName(userEnrollment.getName()));
    }

    public int compareFirstNameTo(UserEnrollment userEnrollment) {
        return getFirstName(this.getName()).compareTo(getFirstName(userEnrollment.getName()));
    }

    private String getFirstName(String fullName) {
        List<String> parts = Arrays.stream(fullName.split(" ")).collect(Collectors.toList());
        return parts.stream().findFirst().orElse("");
    }

    private String getLastName(String fullName) {
        List<String> parts = Arrays.stream(fullName.split(" ")).collect(Collectors.toList());
        String firstName = parts.stream().findFirst().orElse("");
        parts.remove(firstName);
        return String.join(" ", parts);
    }
}
