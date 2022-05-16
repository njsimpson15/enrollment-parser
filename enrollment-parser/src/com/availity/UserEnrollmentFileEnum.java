package com.availity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum UserEnrollmentFileEnum {

    USER_ID(1, "UserId"),
    NAME(2, "Name"),
    VERSION(3, "Version"),
    INSURANCE_COMPANY(4, "InsuranceCompany"),
    UNKNOWN(0, "");

    private final Integer columnId;
    private final String headerName;

    UserEnrollmentFileEnum(Integer columnId, String headerName) {
        this.columnId = columnId;
        this.headerName = headerName;
    }

    public String getHeaderName() { return this.headerName; }

    public Integer getColumnId() { return this.columnId; }

    public static UserEnrollmentFileEnum getColumn(String text) {
        return Arrays.stream(UserEnrollmentFileEnum.values())
                .filter(e -> e.getHeaderName().equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static Map<Integer, UserEnrollmentFileEnum> getDefaultColumnMap() {
        return Arrays.stream(UserEnrollmentFileEnum.values())
                .filter(e -> e.columnId > 0)
                .collect(Collectors.toMap(UserEnrollmentFileEnum::getColumnId, columnName -> columnName));
    }

    public static List<String> getDefaultColumns() {
        return getDefaultColumnMap()
                .values()
                .stream()
                .map(UserEnrollmentFileEnum::getHeaderName)
                .collect(Collectors.toList());
    }
}
