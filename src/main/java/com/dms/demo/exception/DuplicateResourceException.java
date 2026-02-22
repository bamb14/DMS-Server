package com.dms.demo.exception;

@SuppressWarnings("serial")
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}