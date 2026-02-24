package com.dskroba.vpn.telegram;

import com.dskroba.vpn.actor.ActorManager;
import com.dskroba.vpn.statemachine.AuthorizationModule;
import com.dskroba.vpn.statemachine.StateMachine;
import com.dskroba.vpn.statemachine.EventHandlerRegistry;
import com.pengrad.telegrambot.model.Update;

public class TelegramStateMachine extends StateMachine<Update> {
    public TelegramStateMachine(TelegramIOModule ioModule,
                                EventHandlerRegistry handlerRegistry,
                                AuthorizationModule authorizationService,
                                ActorManager actorManager) {
        super(ioModule, handlerRegistry, authorizationService, actorManager);
    }
}
