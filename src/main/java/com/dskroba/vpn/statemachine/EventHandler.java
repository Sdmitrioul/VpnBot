package com.dskroba.vpn.statemachine;

import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.event.Event;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;

public interface EventHandler {
    Effect handleEvent(Event event);

    StateDescriptor stateDescriptor();
}
