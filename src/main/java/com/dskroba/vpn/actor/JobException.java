package com.dskroba.vpn.actor;

import com.dskroba.vpn.exception.CustomException;

public class JobException extends CustomException {
    public JobException(String message) {
        super(message);
    }

    public JobException(Exception e) {
        super(e);
    }
}
