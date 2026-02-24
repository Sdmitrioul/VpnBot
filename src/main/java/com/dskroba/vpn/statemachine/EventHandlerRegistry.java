package com.dskroba.vpn.statemachine;

import com.dskroba.vpn.statemachine.state.State;
import com.dskroba.vpn.statemachine.state.StateFactory;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;

public interface EventHandlerRegistry {
    EventHandler getHandler(State state);

    StateFactory getFactory(StateDescriptor descriptor);
}