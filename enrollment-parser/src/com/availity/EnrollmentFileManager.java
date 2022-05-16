package com.availity;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class EnrollmentFileManager {

    private static final String OUTPUT_CSV_FILE_PATH = "output";
    private static final String CSV_EXT = ".csv";
    private static final String FAILURES_FILE_NAME = "FAILURES";
    private static final String ERROR_COLUMN = "Error";
    private static final String FW_SLASH = "/";

    public EnrollmentFileResults processEnrollmentFile(File file) throws ImportException {
        EnrollmentFileResults enrollmentFileResults = new EnrollmentFileResults();
        try {
            cleanUpOutputDirectory(new File(OUTPUT_CSV_FILE_PATH));
            List<UserEnrollment> userEnrollmentList = parseFile(new FileInputStream(file));
            // Add rows with error to results object and remove them from the results list
            List<UserEnrollment> linesWithError = userEnrollmentList.stream()
                    .filter(r -> !r.getError().isBlank())
                    .collect(Collectors.toList());
            enrollmentFileResults.getLinesWithError().addAll(linesWithError);
            // If there are lines with error, then generate CSV file with failures and error reason
            if(!linesWithError.isEmpty())
                generateCsvFile(FAILURES_FILE_NAME, generateOutputList(linesWithError, true));
            userEnrollmentList.removeAll(linesWithError);
            // Separate the users by insurance company and sort by last name and first name ascending
            Map<String, List<UserEnrollment>> insuranceToUserListMap = sortAndSeparateByInsuranceCompany(userEnrollmentList);
            // For each insurance company user list, remove any duplicates and generate a CSV file
            for (Map.Entry<String, List<UserEnrollment>> entry : insuranceToUserListMap.entrySet()) {
                removeDuplicates(entry.getValue());
                generateCsvFile(entry.getKey(), generateOutputList(entry.getValue(), false));
            }
            enrollmentFileResults.setInsuranceToUserListMap(insuranceToUserListMap);
        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return enrollmentFileResults;
    }

    public List<UserEnrollment> parseFile(InputStream inputStream) throws ImportException {
        EnrollmentFileParser enrollmentFileParser = new EnrollmentFileParser();
        return enrollmentFileParser.parse(inputStream);
    }

    private Map<String, List<UserEnrollment>> sortAndSeparateByInsuranceCompany(List<UserEnrollment> userEnrollmentList) {
        // CompareTo method used by sort() for UserEnrollment compares the last name.
        // If the last name is the same, then the first name is compared.
        Collections.sort(userEnrollmentList);
        return userEnrollmentList.stream()
                .collect(Collectors.groupingBy(UserEnrollment::getInsuranceCompany));
    }

    public void removeDuplicates(List<UserEnrollment> userEnrollments) {
        // Find any users with the same user ID.
        // Keep the user with the highest version and remove the other duplicates.
        Map<String, List<UserEnrollment>> userIdToEnrollmentsMap = userEnrollments.stream()
                .filter(u -> !u.getUserId().isEmpty())
                .collect(Collectors.groupingBy(UserEnrollment::getUserId));
        for (List<UserEnrollment> userIdDuplicateList : userIdToEnrollmentsMap.values()) {
            if(userIdDuplicateList.size() > 1) {
                Optional<UserEnrollment> highestVersion = userIdDuplicateList.stream()
                        .max(Comparator.comparingInt(UserEnrollment::getVersion));
                highestVersion.ifPresent(h -> {
                    userIdDuplicateList.remove(h);
                    userEnrollments.removeAll(userIdDuplicateList);
                });
            }
        }
    }

    private List<List<String>> generateOutputList(List<UserEnrollment> userEnrollments, boolean includeErrorColumn) {
        List<List<String>> outputList = new ArrayList<>();
        // Add column headers to first row in the file
        outputList.add(UserEnrollmentFileEnum.getDefaultColumns());
        if(includeErrorColumn) outputList.get(0).add(ERROR_COLUMN);
        for (UserEnrollment userEnrollment : userEnrollments) {
            List<String> rowData = new ArrayList<>();
            rowData.add(userEnrollment.getUserId());
            rowData.add(userEnrollment.getName());
            rowData.add(userEnrollment.getVersion().toString());
            rowData.add(userEnrollment.getInsuranceCompany());
            if(includeErrorColumn) rowData.add(userEnrollment.getError());
            outputList.add(rowData);
        }
        return outputList;
    }

    private void generateCsvFile(String filename, List<List<String>> outputList) throws ImportException {
        new File(OUTPUT_CSV_FILE_PATH).mkdirs();
        File csvOutputFile = new File(OUTPUT_CSV_FILE_PATH + FW_SLASH + filename + CSV_EXT);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            outputList.stream()
                    .map(o -> String.join(",", o))
                    .forEach(pw::println);
        } catch (Exception e) {
            throw new ImportException("Unable to output CSV file for " + filename, ImportException.USER_ENROLLMENT_IMPORT_ERRORS.FILE_OUTPUT_ERROR);
        }
    }

    private void cleanUpOutputDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                cleanUpOutputDirectory(file);
            }
        }
    }
}
