package com.smartrecruit.interview.exception;

public class DelayRequestNotFoundException extends RuntimeException {
    public DelayRequestNotFoundException() { super("Delay request not found"); }
}
