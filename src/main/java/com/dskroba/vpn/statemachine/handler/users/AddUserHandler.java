package com.dskroba.vpn.statemachine.handler.users;

import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.effect.MessageEffect;
import com.dskroba.vpn.statemachine.event.Event;
import com.dskroba.vpn.statemachine.handler.AbstractHandlerWithFactory;
import com.dskroba.vpn.statemachine.state.State;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;
import com.dskroba.vpn.telegram.user.UserHandlerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.dskroba.vpn.statemachine.state.descriptors.UsersDescriptors.ADD_USER;

@Component
public class AddUserHandler extends AbstractHandlerWithFactory {
    private final UserHandlerManager userHandlerManager;

    @Autowired
    public AddUserHandler(UserHandlerManager userHandlerManager) {
        this.userHandlerManager = userHandlerManager;
    }

    @Override
    protected Optional<Effect> handleStateEvents(Event event) {
        return handleMessageEvent(event)
                .map(String::strip)
                .map(this::formatTelegramHandler)
                .map(this::saveTelegramHandler);
    }

    private Effect saveTelegramHandler(String handler) {
        userHandlerManager.addUser(handler);
        return MessageEffect.of("User with handler @%s, successfully added".formatted(handler))
                .composite(switchToMenu());
    }

    private String formatTelegramHandler(String handler) {
        if (handler.startsWith("@")) {
            handler = handler.substring(1);
        }
        return handler.isEmpty() ? null : handler;
    }

    @Override
    public State createState() {
        return State.cancelable(ADD_USER, new MessageEffect("Provide telegram handler of the user"));
    }

    @Override
    public StateDescriptor stateDescriptor() {
        return ADD_USER;
    }
}
