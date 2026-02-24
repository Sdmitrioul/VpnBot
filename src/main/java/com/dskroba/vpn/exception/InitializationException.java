package com.dskroba.vpn.exception;

public final class InitializationException extends CustomException {
    public InitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitializationException(String message) {
        super(message);
    }
}