package com.dskroba.vpn.storage;

public interface ValueConverter<V> {
    V parse(String value);

    String convertToString(V value);
}