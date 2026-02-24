package com.dskroba.vpn.statemachine.state.descriptors;

public enum VpnDescriptors implements StateDescriptor {
    ADD_VPN_CONFIGURATION,
    SELECT_VPN_CONFIGURATION,
    MANAGE_VPN_CONFIGURATION;

    public static final String CONFIGURATION_NAME_ATTRIBUTE = "$vpn_configuration_name_attribute$";
}
