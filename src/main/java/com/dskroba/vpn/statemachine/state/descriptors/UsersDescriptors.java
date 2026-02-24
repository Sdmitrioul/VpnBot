package com.dskroba.vpn.statemachine.state.descriptors;

import com.dskroba.vpn.principal.Role;

public enum UsersDescriptors implements StateDescriptor {
    SELECT_USER,
    MANAGE_USER,
    BLOCK_USER,
    ADD_USER;

    public static final String USER_ID_STATE_ATTRIBUTE = "$user_id_attribute$";

    @Override
    public Role userRole() {
        return Role.ADMIN;
    }
}
