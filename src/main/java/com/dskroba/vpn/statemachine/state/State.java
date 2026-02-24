package com.dskroba.vpn.statemachine.state;


import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;

public record State(StateDescriptor descriptor, Effect initialEffect, boolean isCancellable) {
    public static State cancelable(StateDescriptor descriptor, Effect initialEffect) {
        return new State(descriptor, initialEffect, true);
    }

    public static State requiring(StateDescriptor descriptor, Effect initialEffect) {
        return new State(descriptor, initialEffect, false);
    }
}
