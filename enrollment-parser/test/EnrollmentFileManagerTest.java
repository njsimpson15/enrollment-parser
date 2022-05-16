import com.availity.EnrollmentFileManager;
import com.availity.EnrollmentFileResults;
import com.availity.ImportException;
import com.availity.UserEnrollment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

class EnrollmentFileManagerTest {

    @Test
    void processEnrollmentFile() throws FileNotFoundException, ImportException {
        ClassLoader classLoader = getClass().getClassLoader();
        File enrollmentFile = new File(Objects.requireNonNull(classLoader.getResource("Valid_Enrollment_File.csv")).getFile());
        EnrollmentFileManager enrollmentFileManager = new EnrollmentFileManager();
        EnrollmentFileResults results = enrollmentFileManager.processEnrollmentFile(enrollmentFile);
        Assertions.assertEquals(0, results.getLinesWithError().size());

        // Validate Aetna File
        File aetnaFile = new File("output/Aetna.csv");
        List<UserEnrollment> aetnaResults = enrollmentFileManager.parseFile(new FileInputStream(aetnaFile));
        Assertions.assertEquals(1, aetnaResults.size());
        Assertions.assertTrue(usersMatch(new UserEnrollment("2E0", "Nick Simpson", 2, "Aetna"), aetnaResults.get(0)));

        // Validate Cigna File
        File cignaFile = new File("output/Cigna.csv");
        List<UserEnrollment> cignaResults = enrollmentFileManager.parseFile(new FileInputStream(cignaFile));
        Assertions.assertEquals(2, cignaResults.size());
        Assertions.assertTrue(usersMatch(new UserEnrollment("1", "Jane Doe", 5, "Cigna"), cignaResults.get(0)));
        Assertions.assertTrue(usersMatch(new UserEnrollment("2", "John Smith", 1, "Cigna"), cignaResults.get(1)));

        // Validate Anthem File
        File anthemFile = new File("output/Anthem.csv");
        List<UserEnrollment> anthemResults = enrollmentFileManager.parseFile(new FileInputStream(anthemFile));
        Assertions.assertEquals(3, anthemResults.size());
        Assertions.assertTrue(usersMatch(new UserEnrollment("2", "Mike Brown", 1, "Anthem"), anthemResults.get(0)));
        Assertions.assertTrue(usersMatch(new UserEnrollment("1", "Ted Lasso", 3, "Anthem"), anthemResults.get(1)));
        Assertions.assertTrue(usersMatch(new UserEnrollment("3", "Amy Smith", 2, "Anthem"), anthemResults.get(2)));


    }

    @Test
    void removeDuplicates() {
        UserEnrollment johnSmith = new UserEnrollment("1", "John Smith", 1, "Anthem");
        UserEnrollment mikeBrown = new UserEnrollment("1", "Mike Brown", 3, "Anthem");
        UserEnrollment johnWilliams  = new UserEnrollment("1", "John Williams", 2, "Anthem");
        List<UserEnrollment> userEnrollmentList = new ArrayList<>();
        userEnrollmentList.add(johnSmith);
        userEnrollmentList.add(mikeBrown);
        userEnrollmentList.add(johnWilliams);
        EnrollmentFileManager enrollmentFileManager = new EnrollmentFileManager();
        enrollmentFileManager.removeDuplicates(userEnrollmentList);
        Assertions.assertEquals(1, userEnrollmentList.size());
        Assertions.assertEquals("Mike Brown", userEnrollmentList.get(0).getName());
    }

    @Test
    void invalidHeaders() {
        ClassLoader classLoader = getClass().getClassLoader();
        File enrollmentFile = new File(Objects.requireNonNull(classLoader.getResource("Invalid_Headers.csv")).getFile());
        EnrollmentFileManager enrollmentFileManager = new EnrollmentFileManager();
        try {
            enrollmentFileManager.processEnrollmentFile(enrollmentFile);
        } catch (ImportException e) {
            Assertions.assertEquals(ImportException.USER_ENROLLMENT_IMPORT_ERRORS.INVALID_HEADERS, e.getError());
        }
    }

    @Test
    void invalidVersion() {
        ClassLoader classLoader = getClass().getClassLoader();
        File enrollmentFile = new File(Objects.requireNonNull(classLoader.getResource("Invalid_Version.csv")).getFile());
        EnrollmentFileManager enrollmentFileManager = new EnrollmentFileManager();
        try {
            enrollmentFileManager.processEnrollmentFile(enrollmentFile);
        } catch (ImportException e) {
            Assertions.assertEquals(ImportException.USER_ENROLLMENT_IMPORT_ERRORS.INVALID_VERSION, e.getError());
        }
    }

    @Test
    void invalidRowData() throws ImportException {
        ClassLoader classLoader = getClass().getClassLoader();
        File enrollmentFile = new File(Objects.requireNonNull(classLoader.getResource("Invalid_Row_Data.csv")).getFile());
        EnrollmentFileManager enrollmentFileManager = new EnrollmentFileManager();
        EnrollmentFileResults results = enrollmentFileManager.processEnrollmentFile(enrollmentFile);
        Assertions.assertEquals(3, results.getLinesWithError().size());
        Assertions.assertEquals(ImportException.USER_ENROLLMENT_IMPORT_ERRORS.INVALID_USER_ID.name(), results.getLinesWithError().get(0).getError());
        Assertions.assertEquals(ImportException.USER_ENROLLMENT_IMPORT_ERRORS.INVALID_NAME.name(), results.getLinesWithError().get(1).getError());
        Assertions.assertEquals(ImportException.USER_ENROLLMENT_IMPORT_ERRORS.INVALID_INSURANCE_COMPANY.name(), results.getLinesWithError().get(2).getError());
    }

    private boolean usersMatch(UserEnrollment ue1, UserEnrollment ue2) {
        return ue1.getUserId().equals(ue2.getUserId())
                && ue1.getName().equals(ue2.getName())
                && ue1.getVersion().equals(ue2.getVersion())
                && ue1.getInsuranceCompany().equals(ue2.getInsuranceCompany());
    }
}