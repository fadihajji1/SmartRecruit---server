package com.smartrecruit.interview.exception;

public class InterviewNotFoundException extends RuntimeException {
    public InterviewNotFoundException() { super("Interview not found"); }
}
