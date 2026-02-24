package com.dskroba.vpn.statemachine.handler.command;

import com.dskroba.vpn.statemachine.handler.StateSelector;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.dskroba.vpn.statemachine.state.descriptors.CommandStatesDescriptors.MANAGE_USERS;
import static com.dskroba.vpn.statemachine.state.descriptors.CommandStatesDescriptors.MENU;
import static com.dskroba.vpn.statemachine.state.descriptors.VpnDescriptors.ADD_VPN_CONFIGURATION;
import static com.dskroba.vpn.statemachine.state.descriptors.VpnDescriptors.SELECT_VPN_CONFIGURATION;

@Component
public class MenuHandler extends StateSelector {
    private static final List<StateSelectorData> STATE_DESCRIPTORS = List.of(
            userSelector("Add device", ADD_VPN_CONFIGURATION),
            userSelector("Manage devices", SELECT_VPN_CONFIGURATION),
            adminSelector("Manage users", MANAGE_USERS)
    );

    public MenuHandler() {
        super(STATE_DESCRIPTORS);
    }

    @Override
    public StateDescriptor stateDescriptor() {
        return MENU;
    }
}
