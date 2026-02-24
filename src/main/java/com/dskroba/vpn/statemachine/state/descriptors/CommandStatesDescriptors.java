package com.dskroba.vpn.statemachine.state.descriptors;

import com.dskroba.vpn.principal.Role;

public enum CommandStatesDescriptors implements StateDescriptor {
    MENU("Provides bot menu.", Role.USER),
    MANAGE_USERS("Menu for managing users", Role.ADMIN);

    private final String description;
    private final Role role;

    CommandStatesDescriptors(String description, Role role) {
        this.description = description;
        this.role = role;
    }

    public String code() {
        return "/" + name().toLowerCase();
    }

    public String getDescription() {
        return description;
    }

    @Override
    public Role userRole() {
        return role;
    }
}


