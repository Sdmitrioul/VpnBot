package com.dskroba.vpn.principal;

import com.dskroba.vpn.storage.ValueConverter;
import com.google.gson.Gson;

public class PrincipalConverter implements ValueConverter<Principal> {
    private static final Gson GSON = new Gson();

    @Override
    public Principal parse(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return GSON.fromJson(value, Principal.class);
    }

    @Override
    public String convertToString(Principal value) {
        if (value == null) {
            return "";
        }
        return GSON.toJson(value);
    }
}
