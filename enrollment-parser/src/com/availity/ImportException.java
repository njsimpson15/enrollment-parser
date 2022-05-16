package com.availity;

public class ImportException extends Exception {

    private final USER_ENROLLMENT_IMPORT_ERRORS error;

    public USER_ENROLLMENT_IMPORT_ERRORS getError() {
        return error;
    }

    public ImportException(USER_ENROLLMENT_IMPORT_ERRORS error) {
        super();
        this.error = error;
    }

    public ImportException(String message, USER_ENROLLMENT_IMPORT_ERRORS error) {
        super(message);
        this.error = error;
    }

    public enum USER_ENROLLMENT_IMPORT_ERRORS {
        INVALID_HEADERS,
        INVALID_USER_ID,
        INVALID_NAME,
        INVALID_VERSION,
        INVALID_INSURANCE_COMPANY,
        ERROR_READING_FILE,
        FILE_OUTPUT_ERROR,
        UNKNOWN
    }
}
