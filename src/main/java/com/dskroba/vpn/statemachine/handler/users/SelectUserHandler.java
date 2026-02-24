package com.dskroba.vpn.statemachine.handler.users;

import com.dskroba.vpn.principal.Principal;
import com.dskroba.vpn.principal.PrincipalService;
import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.effect.MessageEffect;
import com.dskroba.vpn.statemachine.effect.SelectEffect;
import com.dskroba.vpn.statemachine.event.Event;
import com.dskroba.vpn.statemachine.handler.AbstractHandlerWithFactory;
import com.dskroba.vpn.statemachine.state.ContextAccessor;
import com.dskroba.vpn.statemachine.state.State;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;
import com.dskroba.vpn.telegram.TelegramId;
import com.dskroba.vpn.telegram.user.UserHandlersProvider;
import com.dskroba.vpn.type.SelectOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.dskroba.vpn.statemachine.state.descriptors.UsersDescriptors.*;

@Component
public class SelectUserHandler extends AbstractHandlerWithFactory {
    private final PrincipalService principalService;
    private final UserHandlersProvider userHandlersProvider;

    @Autowired
    public SelectUserHandler(PrincipalService principalService, UserHandlersProvider userHandlersProvider) {
        this.principalService = principalService;
        this.userHandlersProvider = userHandlersProvider;
    }

    @Override
    protected Optional<Effect> handleStateEvents(Event event) {
        return handleMessageEvent(event)
                .map(this::formatTelegramHandler)
                .map(this::consumeHandler);
    }

    private String formatTelegramHandler(String handler) {
        handler = handler.strip();
        if (handler.startsWith("@")) {
            handler = handler.substring(1);
        }
        if (handler.endsWith("*")) {
            handler = handler.substring(0, handler.length() - 1);
        }
        return handler.isEmpty() ? null : handler;
    }

    private Effect consumeHandler(String handler) {
        var id = loadUserIds().stream().filter(userId -> userId.telegramHandle().equals(handler)).findAny();
        if (id.isEmpty()) {
            return MessageEffect.of("User with handler %s could not be found".formatted(handler)).composite(switchToMenu());
        }
        ContextAccessor.context().updateStateContext(context -> context.setAttribute(USER_ID_STATE_ATTRIBUTE, id.get()));
        return moveToState(MANAGE_USER);
    }

    @Override
    public State createState() {
        Collection<TelegramId> userIds = loadUserIds();
        if (userIds.isEmpty()) {
            return State.cancelable(SELECT_USER, SelectEffect.withCancelOption("No user is available", List.of()));
        }
        return State.cancelable(SELECT_USER, SelectEffect.withCancelOption("""
                        Select one of the users.
                        * - marks users that still do not use VPN.
                        """,
                userIds.stream()
                        .map(this::selectOptionFromId)
                        .toList()
        ));
    }

    private Collection<TelegramId> loadUserIds() {
        Map<String, TelegramId> result = new HashMap<>();
        userHandlersProvider.userHandles().forEach(handler -> result.put(handler, TelegramId.unresolved(handler)));
        Set<String> admins = Set.copyOf(userHandlersProvider.adminHandlers());
        principalService.getAllPrincipals().stream()
                .map(Principal::key)
                .filter(key -> !admins.contains(key.telegramHandle()))
                .forEach(id -> result.put(id.telegramHandle(), id));
        return result.values();
    }

    private SelectOption selectOptionFromId(TelegramId telegramId) {
        String label = "@" + telegramId.telegramHandle();
        if (!telegramId.isResolved()) {
            label += "*";
        }
        return SelectOption.fullRowOption(label);
    }

    @Override
    public StateDescriptor stateDescriptor() {
        return SELECT_USER;
    }
}
