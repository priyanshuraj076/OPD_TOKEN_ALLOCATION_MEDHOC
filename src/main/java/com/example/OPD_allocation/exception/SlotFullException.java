package com.example.OPD_allocation.exception;

public class SlotFullException extends RuntimeException {
    public SlotFullException(String message) {
        super(message);
    }
}
