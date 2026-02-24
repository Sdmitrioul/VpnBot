package com.dskroba.vpn.statemachine.handler.command;

import com.dskroba.vpn.statemachine.handler.StateSelector;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.dskroba.vpn.statemachine.state.descriptors.CommandStatesDescriptors.MANAGE_USERS;
import static com.dskroba.vpn.statemachine.state.descriptors.UsersDescriptors.ADD_USER;
import static com.dskroba.vpn.statemachine.state.descriptors.UsersDescriptors.SELECT_USER;

@Component
public class ManageUsersHandler extends StateSelector {
    private static final List<StateSelectorData> STATE_DESCRIPTORS = List.of(
            adminSelector("Add new user", ADD_USER),
            adminSelector("Update user", SELECT_USER));

    public ManageUsersHandler() {
        super(STATE_DESCRIPTORS);
    }

    @Override
    public StateDescriptor stateDescriptor() {
        return MANAGE_USERS;
    }
}
