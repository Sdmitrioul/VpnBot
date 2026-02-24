package com.dskroba.vpn.statemachine.state.descriptors;

import com.dskroba.vpn.principal.Role;

public sealed interface StateDescriptor permits CommandStatesDescriptors, UsersDescriptors, VpnDescriptors {
    String name();

    default Role userRole() {
        return Role.USER;
    }
}
