package com.dskroba.vpn.statemachine;

import com.dskroba.vpn.actor.ActorManager;
import com.dskroba.vpn.actor.Context;
import com.dskroba.vpn.actor.JobException;
import com.dskroba.vpn.actor.PrincipalActor;
import com.dskroba.vpn.exception.AuthorizationException;
import com.dskroba.vpn.principal.Principal;
import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.effect.MessageEffect;
import com.dskroba.vpn.statemachine.event.Event;
import com.dskroba.vpn.statemachine.state.ContextAccessor;
import com.dskroba.vpn.statemachine.state.State;
import com.dskroba.vpn.statemachine.state.descriptors.CommandStatesDescriptors;
import com.dskroba.vpn.telegram.TelegramId;

public abstract class StateMachine<I> {
    private final IOModule<I> ioModule;
    private final EventHandlerRegistry handlerRegistry;
    protected final AuthorizationModule authorizationModule;
    private final ActorManager actorManager;

    protected StateMachine(IOModule<I> ioModule,
                           EventHandlerRegistry handlerRegistry,
                           AuthorizationModule authorizationModule,
                           ActorManager actorManager) {
        this.ioModule = ioModule;
        this.handlerRegistry = handlerRegistry;
        this.authorizationModule = authorizationModule;
        this.actorManager = actorManager;
    }

    public final void processEvent(I input) throws AuthorizationException {
        TelegramId issuer = ioModule.provideIssuer(input);
        Principal authorizedIssuer = authorizationModule.authorize(issuer);
        PrincipalActor actor = actorManager.getActor(authorizedIssuer);
        actor.submit(() -> {
            try {
                Event parsedEvent = ioModule.parse(input, authorizedIssuer);
                Context stateContext = Context.PROVIDER.get();
                EventHandler eventHandler = handlerRegistry.getHandler(stateContext.getState());
                Effect effects = eventHandler.handleEvent(parsedEvent);
                ioModule.handleEffect(effects);
            } catch (Exception e) {
                ioModule.handleEffect(MessageEffect.of("Server error!\nReturning to menu").composite(switchToMenu()));
                throw new JobException(e);
            }
        });
    }

    private Effect switchToMenu() {
        State menuState = handlerRegistry.getFactory(CommandStatesDescriptors.MENU).createState();
        ContextAccessor.context().updateStateContext(stateContext -> {
            stateContext.clearAttributes();
            stateContext.setState(menuState);
        });
        return menuState.initialEffect();
    }
}
