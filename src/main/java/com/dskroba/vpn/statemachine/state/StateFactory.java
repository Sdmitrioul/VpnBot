package com.dskroba.vpn.statemachine.state;

import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;

public interface StateFactory {
    State createState();

    StateDescriptor stateDescriptor();
}
