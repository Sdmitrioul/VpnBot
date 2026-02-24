package com.dskroba.vpn.exception;

public class RepositoryException extends CustomException {
    public RepositoryException(String message, Exception e) {
        super(message, e);
    }

    public RepositoryException(String message) {
        super(message);
    }
}