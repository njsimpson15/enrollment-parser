package com.availity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnrollmentFileResults {
    private Map<String, List<UserEnrollment>> insuranceToUserListMap;
    private List<UserEnrollment> linesWithError = new ArrayList<>();

    public Map<String, List<UserEnrollment>> getInsuranceToUserListMap() {
        return insuranceToUserListMap;
    }

    public void setInsuranceToUserListMap(Map<String, List<UserEnrollment>> insuranceToUserListMap) {
        this.insuranceToUserListMap = insuranceToUserListMap;
    }

    public List<UserEnrollment> getLinesWithError() {
        return linesWithError;
    }

    public void setLinesWithError(List<UserEnrollment> linesWithError) {
        this.linesWithError = linesWithError;
    }
}
