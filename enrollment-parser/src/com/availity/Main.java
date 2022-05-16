package com.availity;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        if(args.length == 0 || args[0].isBlank())
            System.out.println("Invalid input");
        EnrollmentFileManager enrollmentFileManager = new EnrollmentFileManager();
        try {
            EnrollmentFileResults results = enrollmentFileManager.processEnrollmentFile(new File(args[0]));
            results.getInsuranceToUserListMap().forEach((insurance, user) -> System.out.println("Created new file: " + insurance + ".csv"));
            if(!results.getLinesWithError().isEmpty()) {
                System.out.println("Lines with error: " + results.getLinesWithError().size());
                System.out.println("See FAILURES.csv for errors.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
