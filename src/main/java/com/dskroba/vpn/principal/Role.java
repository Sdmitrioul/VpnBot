package com.dskroba.vpn.principal;

public enum Role {
    ADMIN, USER;

    public boolean allowed(Role role) {
        return this != ADMIN || role == ADMIN;
    }
}
