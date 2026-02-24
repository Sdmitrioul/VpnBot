package com.dskroba.vpn.statemachine.handler;

import com.dskroba.vpn.statemachine.EventHandler;
import com.dskroba.vpn.statemachine.EventHandlerRegistry;
import com.dskroba.vpn.statemachine.EventHandlerRegistryAware;
import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.effect.MessageEffect;
import com.dskroba.vpn.statemachine.event.CommandEvent;
import com.dskroba.vpn.statemachine.event.Event;
import com.dskroba.vpn.statemachine.event.MessageEvent;
import com.dskroba.vpn.statemachine.state.ContextAccessor;
import com.dskroba.vpn.statemachine.state.State;
import com.dskroba.vpn.statemachine.state.StateContext;
import com.dskroba.vpn.statemachine.state.descriptors.CommandStatesDescriptors;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static com.dskroba.vpn.statemachine.state.descriptors.CommandStatesDescriptors.MENU;
import static com.dskroba.vpn.utils.FP.constant;

public abstract class AbstractHandler implements EventHandler, EventHandlerRegistryAware {
    protected static final Logger log = LoggerFactory.getLogger(AbstractHandler.class);
    private static final String START_COMMAND = "start";
    private static final Set<String> INITIAL_COMMANDS = Set.of(START_COMMAND, "help");
    private EventHandlerRegistry registry;

    @Override
    public Effect handleEvent(Event event) {
        return tryHandleCommand(event)
                .or(() -> tryHandleCancelRequest(event))
                .or(() -> tryHandleStateEvents(event))
                .orElseGet(() -> unrecognizedEventHandler(event));
    }

    @Override
    public void setRegistry(EventHandlerRegistry registry) {
        this.registry = registry;
    }

    private Optional<Effect> tryHandleCommand(Event event) {
        if (!(event instanceof CommandEvent commandEvent)) {
            return Optional.empty();
        }
        return Optional.of(handleCommand(commandEvent));
    }

    private Optional<Effect> tryHandleCancelRequest(Event event) {
        State state = ContextAccessor.getState();
        if (!isCancelRequest(event) || (state != null && !state.isCancellable())) {
            return Optional.empty();
        }
        return Optional.of(switchToMenu());
    }

    private Optional<Effect> tryHandleStateEvents(Event event) {
        return handleStateEvents(event);
    }

    private static boolean isCancelRequest(Event event) {
        return event instanceof MessageEvent messageEvent
                && "cancel".equalsIgnoreCase(messageEvent.payload());
    }

    private Effect unrecognizedEventHandler(Event event) {
        log.warn("Unrecognized event: {}", event);
        return MessageEffect.of("Unrecognized option.\nPlease, try again.")
                .composite(ContextAccessor.getState().initialEffect());
    }

    private Effect handleCommand(CommandEvent commandEvent) {
        String command = commandEvent.payload();

        if (START_COMMAND.equalsIgnoreCase(command)) {
            return startEffects();
        }

        if (INITIAL_COMMANDS.contains(command.toLowerCase(Locale.ROOT))) {
            return MessageEffect.of("This bot provides interface to track expenses in google sheet.")
                    .composite(switchToMenu());
        }

        return parseAndHandleStateCommand(command);
    }

    private Effect startEffects() {
        return moveToState(MENU);
    }

    private Effect parseAndHandleStateCommand(String command) {
        try {
            CommandStatesDescriptors state = CommandStatesDescriptors.valueOf(command.toUpperCase(Locale.ROOT));
            return moveToState(state);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown command received: {}", command);
            return MessageEffect.of("Unknown command: " + command)
                    .composite(switchToMenu());
        }
    }

    protected Optional<String> handleMessageEvent(Event event) {
        return Optional.ofNullable(event)
                .filter(e -> e instanceof MessageEvent)
                .map(e -> (MessageEvent) e)
                .map(MessageEvent::payload);
    }

    protected Effect switchToMenu() {
        return handleChangeOfState(MENU, constant(StateContext.class), true);
    }

    protected Effect switchToMenu(Consumer<StateContext> updater) {
        return handleChangeOfState(MENU, updater, true);
    }

    protected Effect switchToState(StateDescriptor stateDescriptor) {
        return handleChangeOfState(stateDescriptor, constant(StateContext.class), true);
    }

    protected Effect moveToState(StateDescriptor stateDescriptor) {
        return handleChangeOfState(stateDescriptor, constant(StateContext.class), false);
    }

    protected Effect moveToState(StateDescriptor stateDescriptor, Consumer<StateContext> updater) {
        return handleChangeOfState(stateDescriptor, updater, false);
    }

    private Effect handleChangeOfState(StateDescriptor stateDescriptor, Consumer<StateContext> updater, boolean clearContext) {
        if (!stateDescriptor.userRole().allowed(ContextAccessor.getPrincipalRole())) {
            return MessageEffect.of("Unrecognized option.");
        }

        State nextState = registry.getFactory(stateDescriptor).createState();

        ContextAccessor.context().updateStateContext(stateContext -> {
            updater.accept(stateContext);
            if (clearContext) {
                stateContext.clearAttributes();
            }
            stateContext.setState(nextState);
        });
        return buildStateTransitionEffects(nextState);
    }

    private Effect buildStateTransitionEffects(State state) {
        return state.initialEffect();
    }

    protected abstract Optional<Effect> handleStateEvents(Event event);
}
