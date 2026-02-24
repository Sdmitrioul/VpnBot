package com.dskroba.vpn.storage;

public class LongConverter implements ValueConverter<Long> {
    public static final LongConverter INSTANCE = new LongConverter();

    @Override
    public Long parse(String value) {
        return Long.parseLong(value);
    }

    @Override
    public String convertToString(Long value) {
        return value.toString();
    }

    private LongConverter() {
    }
}
