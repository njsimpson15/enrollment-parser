package com.availity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

public class EnrollmentFileParser {

    private static final String COMMA_DELIMITER = ",";

    public List<UserEnrollment> parse(InputStream inputStream) throws ImportException {
        try{
            return parseImport(read(new InputStreamReader(inputStream)));
        } catch(ImportException ie) {
            throw ie;
        } catch(Exception e) {
            throw new ImportException(e.getMessage(), ImportException.USER_ENROLLMENT_IMPORT_ERRORS.UNKNOWN);
        }
    }

    private List<UserEnrollment> parseImport(List<List<String>> fileDataRows) throws ImportException {
        List<UserEnrollment> importList = new ArrayList<>();
        // Get the headers (if included in the file) and the order or the default headers and order
        Map<Integer, UserEnrollmentFileEnum> columnMap = getHeaderMap(fileDataRows);
        for(List<String> csvLine : fileDataRows) {
            if(csvLine.isEmpty() || (csvLine.size() == 1 && csvLine.get(0).isBlank())) continue; //Skip that line
            UserEnrollment userEnrollment = new UserEnrollment();
            try{
                int index = 0;
                // For each column in the row, find the current column and assign the data in the row to the associated field
                for(String columnData: csvLine){
                    UserEnrollmentFileEnum columnName = columnMap.getOrDefault(index, null);
                    if(columnName != null && columnName != UserEnrollmentFileEnum.UNKNOWN)
                        setColumnValue(userEnrollment, columnName, columnData.trim());
                    index++;
                }
                // Check if any of the fields are empty and if so, then assign an error to the row
                validateRowData(userEnrollment);
            } catch(Exception e) {
                userEnrollment.setError(ImportException.USER_ENROLLMENT_IMPORT_ERRORS.UNKNOWN.name());
            }
            importList.add(userEnrollment);
        }
        return importList;
    }

    private Map<Integer, UserEnrollmentFileEnum> getHeaderMap(List<List<String>> fileDataRows) throws ImportException {
        Map<Integer, UserEnrollmentFileEnum> columnMap;
        // If headers are present then get header map. Otherwise, use the default header order.
        if(fileDataRows.get(0).stream().anyMatch(r -> UserEnrollmentFileEnum.getColumn(r) != UserEnrollmentFileEnum.UNKNOWN)) {
            try {
                List<String> headerLine = fileDataRows.get(0);
                if (headerLine.size() == 1 && headerLine.get(0).isBlank())
                    throw new ImportException(ImportException.USER_ENROLLMENT_IMPORT_ERRORS.INVALID_HEADERS);
                columnMap = getHeadersFromFile(headerLine);
                verifyRequiredColumns(columnMap.values().stream().filter(c -> c != UserEnrollmentFileEnum.UNKNOWN).collect(Collectors.toSet()));
                fileDataRows.remove(0);
            } catch (Exception e) {
                throw new ImportException(ImportException.USER_ENROLLMENT_IMPORT_ERRORS.INVALID_HEADERS);
            }
        } else columnMap = UserEnrollmentFileEnum.getDefaultColumnMap();
        return columnMap;
    }

    private HashMap<Integer, UserEnrollmentFileEnum> getHeadersFromFile(List<String> firstRowData) {
        HashMap<Integer, UserEnrollmentFileEnum> headers = new HashMap<>();
        int headerIndex = 0;
        for(String columnData: firstRowData){
            // Remove any special or hidden characters and any white space in the column header
            columnData = deleteWhitespace(columnData.replaceAll("[^a-zA-Z0-9]", "").toLowerCase());
            UserEnrollmentFileEnum columnName = UserEnrollmentFileEnum.getColumn(columnData);
            headers.put(headerIndex, columnName);
            headerIndex++;
        }
        return headers;
    }

    private List<List<String>> read(Reader fileReader) throws ImportException {
        List<List<String>> records = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(fileReader)){
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);
                records.add(Arrays.asList(values));
            }
        } catch(Exception e) {
            throw new ImportException(e.getLocalizedMessage(), ImportException.USER_ENROLLMENT_IMPORT_ERRORS.ERROR_READING_FILE);
        }
        return records;
    }

    private void verifyRequiredColumns(Set<UserEnrollmentFileEnum> columns) throws ImportException {
        Set<UserEnrollmentFileEnum> requiredColumns = Arrays.stream(UserEnrollmentFileEnum.values())
                .filter(c -> c != UserEnrollmentFileEnum.UNKNOWN)
                .collect(Collectors.toSet());
        if(!columns.containsAll(requiredColumns))
            throw new ImportException(ImportException.USER_ENROLLMENT_IMPORT_ERRORS.INVALID_HEADERS);
    }

    public void setColumnValue(UserEnrollment userEnrollment, UserEnrollmentFileEnum columnName, String data) {
        try {
            switch (columnName) {
                case USER_ID:
                    userEnrollment.setUserId(data);
                    break;
                case NAME:
                    userEnrollment.setName(data);
                    break;
                case VERSION:
                    userEnrollment.setVersion(Integer.parseInt(data));
                    break;
                case INSURANCE_COMPANY:
                    userEnrollment.setInsuranceCompany(data);
                    break;
                case UNKNOWN:
            }
        } catch (NumberFormatException e) {
            userEnrollment.setError(ImportException.USER_ENROLLMENT_IMPORT_ERRORS.INVALID_VERSION.name());
        }
    }

    private void validateRowData(UserEnrollment userEnrollment) {
        if(userEnrollment.getUserId().isBlank())
            userEnrollment.setError(ImportException.USER_ENROLLMENT_IMPORT_ERRORS.INVALID_USER_ID.name());
        if(userEnrollment.getName().isBlank())
            userEnrollment.setError(ImportException.USER_ENROLLMENT_IMPORT_ERRORS.INVALID_NAME.name());
        if(userEnrollment.getInsuranceCompany() == null || userEnrollment.getInsuranceCompany().isBlank())
            userEnrollment.setError(ImportException.USER_ENROLLMENT_IMPORT_ERRORS.INVALID_INSURANCE_COMPANY.name());
    }

    public static String deleteWhitespace(String str) {
        if (isEmpty(str)) {
            return str;
        } else {
            int sz = str.length();
            char[] chs = new char[sz];
            int count = 0;

            for(int i = 0; i < sz; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    chs[count++] = str.charAt(i);
                }
            }

            if (count == sz) {
                return str;
            } else {
                return new String(chs, 0, count);
            }
        }
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}
