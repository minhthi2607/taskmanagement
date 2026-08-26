package com.project.taskmanagement.exception;

public class LabelNotFoundException extends ResourceNotFoundException {
    public LabelNotFoundException(String message) {
        super(message);
    }
}
