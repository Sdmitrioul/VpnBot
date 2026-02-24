package com.dskroba.vpn.statemachine.effect;

import com.dskroba.vpn.type.ContentType;
import com.dskroba.vpn.type.FileData;
import com.dskroba.vpn.type.FileWithMessage;

public record FileEffect(FileWithMessage payload) implements Effect {
    public static FileEffect of(String message, String filename, ContentType contentType, byte[] data) {
        return new FileEffect(new FileWithMessage(new FileData(filename, contentType, data), message));
    }
}
