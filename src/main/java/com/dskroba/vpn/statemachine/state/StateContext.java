package com.dskroba.vpn.statemachine.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StateContext {
    private final Long principalId;
    private volatile State state;
    private final Map<String, Object> attributes;

    public StateContext(Long principalId) {
        this(principalId, null, new HashMap<>());
    }

    private StateContext(Long principalId, State state, Map<String, Object> attributes) {
        this.principalId = principalId;
        this.state = state;
        this.attributes = attributes;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public <T> T getAttribute(String key, Class<T> type) {
        return type.cast(attributes.get(key));
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public void clearAttributes() {
        attributes.clear();
    }

    public Long getPrincipalId() {
        return principalId;
    }

    public StateContext cloneInstance() {
        return new StateContext(principalId, state, Map.copyOf(attributes));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StateContext that)) return false;
        return Objects.equals(principalId, that.principalId) && Objects.equals(state, that.state) && Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principalId, state, attributes);
    }

    @Override
    public String toString() {
        return "StateContext{" +
                "state=" + state +
                ", attributes=" + attributes +
                '}';
    }
}
