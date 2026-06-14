package com.codeforge.ai.exception;

public class SubmissionConfigurationException extends RuntimeException {
    public SubmissionConfigurationException(String message) {
        super(message);
    }

    public SubmissionConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
