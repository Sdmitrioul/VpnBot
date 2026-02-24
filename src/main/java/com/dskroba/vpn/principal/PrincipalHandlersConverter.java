package com.dskroba.vpn.principal;

import com.dskroba.vpn.storage.ValueConverter;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.List;

public class PrincipalHandlersConverter implements ValueConverter<List<String>> {
    private static final Gson GSON = new GsonBuilder()
            .create();
    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>() {
    }.getType();

    @Override
    public List<String> parse(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return GSON.fromJson(value, STRING_LIST_TYPE);
    }

    @Override
    public String convertToString(List<String> value) {
        if (value == null) {
            return "";
        }
        return GSON.toJson(value);
    }
}
