package com.dskroba.vpn.statemachine.handler;

import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.effect.MessageEffect;
import com.dskroba.vpn.statemachine.event.Event;
import com.dskroba.vpn.statemachine.state.ContextAccessor;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DefaultEventHandler extends AbstractHandler {
    @Override
    public StateDescriptor stateDescriptor() {
        return null;
    }

    @Override
    protected Optional<Effect> handleStateEvents(Event event) {
        log.warn("Unhandled state: {}", ContextAccessor.getState());
        return Optional.of(MessageEffect.of("""
                Unable to process this request.
                You will be redirect to the main menu.""").composite(switchToMenu()));
    }
}