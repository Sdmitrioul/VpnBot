package com.dskroba.vpn.actor;

import com.dskroba.vpn.base.Listener;
import com.dskroba.vpn.principal.Principal;
import com.dskroba.vpn.principal.Role;
import com.dskroba.vpn.statemachine.state.State;
import com.dskroba.vpn.statemachine.state.StateContext;

import java.util.function.Consumer;
import java.util.function.Function;

public final class Context {
    public static final ScopedValue<Context> PROVIDER = ScopedValue.newInstance();

    private volatile Principal principal;
    private final Function<Principal, Role> roleProvider;
    private final StateContext stateContext;
    private final Listener<Principal> principalChangeListener;
    private final Listener<StateContext> stateContextListener;

    public Context(Principal principal,
                   Function<Principal, Role> roleProvider,
                   StateContext stateContext,
                   Listener<Principal> principalChangeListener,
                   Listener<StateContext> stateContextListener) {
        this.principal = principal;
        this.roleProvider = roleProvider;
        this.stateContext = stateContext;
        this.principalChangeListener = principalChangeListener;
        this.stateContextListener = stateContextListener;
    }

    public Role getPrincipalRole() {
        return roleProvider.apply(principal);
    }

    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
        this.principalChangeListener.receive(principal);
    }

    public State getState() {
        return stateContext.getState();
    }


    public <T> T getAttribute(String key, Class<T> type) {
        return stateContext.getAttribute(key, type);
    }

    public void updateStateContext(Consumer<StateContext> updater) {
        updater.accept(stateContext);
        var cloned = stateContext.cloneInstance();
        this.stateContextListener.receive(cloned);
    }
}
