package com.dskroba.vpn.statemachine;

import com.dskroba.vpn.statemachine.handler.DefaultEventHandler;
import com.dskroba.vpn.statemachine.state.State;
import com.dskroba.vpn.statemachine.state.StateFactory;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EventHandlerRegistryImpl implements EventHandlerRegistry {
    private final Map<StateDescriptor, EventHandler> handlers;
    private final Map<StateDescriptor, StateFactory> factories;
    private final DefaultEventHandler defaultHandler;

    @Autowired
    public EventHandlerRegistryImpl(
            DefaultEventHandler defaultHandler,
            List<EventHandler> handlers,
            List<StateFactory> factories
    ) {
        Map<StateDescriptor, EventHandler> handlerMap = new HashMap<>();
        for (EventHandler handler : handlers) {
            if (handler.stateDescriptor() != null) {
                handlerMap.put(handler.stateDescriptor(), handler);
            }
        }
        this.handlers = Map.copyOf(handlerMap);
        this.defaultHandler = defaultHandler;

        Map<StateDescriptor, StateFactory> factoryMap = new HashMap<>();
        for (StateFactory factory : factories) {
            factoryMap.put(factory.stateDescriptor(), factory);
        }
        this.factories = Map.copyOf(factoryMap);
    }

    @PostConstruct
    void init() {
        for (EventHandler handler : handlers.values()) {
            if (handler instanceof EventHandlerRegistryAware aware) {
                aware.setRegistry(this);
            }
        }
        if (defaultHandler instanceof EventHandlerRegistryAware aware) {
            aware.setRegistry(this);
        }
    }

    @Override
    public EventHandler getHandler(State state) {
        if (state == null) return defaultHandler;
        return handlers.getOrDefault(state.descriptor(), defaultHandler);
    }

    @Override
    public StateFactory getFactory(StateDescriptor descriptor) {
        return factories.get(descriptor);
    }
}