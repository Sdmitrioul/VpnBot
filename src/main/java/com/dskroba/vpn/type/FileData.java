package com.dskroba.vpn.type;

public record FileData(String filename, ContentType contentType, byte[] data) {
}
