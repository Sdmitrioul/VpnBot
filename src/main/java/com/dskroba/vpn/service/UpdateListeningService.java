package com.dskroba.vpn.service;

import com.dskroba.vpn.actor.Context;
import com.dskroba.vpn.actor.ContextProvider;
import com.dskroba.vpn.actor.ContextRemover;
import com.dskroba.vpn.principal.Principal;
import com.dskroba.vpn.principal.PrincipalService;
import com.dskroba.vpn.principal.Role;
import com.dskroba.vpn.statemachine.state.StateContext;
import com.dskroba.vpn.storage.KeyValueStorage;
import com.dskroba.vpn.telegram.TelegramId;
import com.dskroba.vpn.telegram.user.UserHandlersProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpdateListeningService implements ContextProvider, ContextRemover {
    private final KeyValueStorage<Long, StateContext> stateStorage;
    private final PrincipalService principalService;
    private final UserHandlersProvider userHandlersProvider;

    @Autowired
    public UpdateListeningService(KeyValueStorage<Long, StateContext> stateStorage,
                                  PrincipalService principalService,
                                  UserHandlersProvider userHandlersProvider) {
        this.stateStorage = stateStorage;
        this.principalService = principalService;
        this.userHandlersProvider = userHandlersProvider;
    }

    @Override
    public Context getContext(Principal principal) {
        return new Context(principal,
                this::userRoleProvider,
                Optional.ofNullable(stateStorage.find(principal.id()))
                        .orElse(new StateContext(principal.id())),
                this::listenForPrincipal,
                this::listenForStateContextChange);
    }

    private Role userRoleProvider(Principal principal) {
        if (principal != null
                && userHandlersProvider.adminHandlers().contains(principal.key().telegramHandle())) {
            return Role.ADMIN;
        }
        return Role.USER;
    }

    private void listenForPrincipal(Principal principal) {
        principalService.savePrincipal(principal);
        //TODO: add executor service
    }

    private void listenForStateContextChange(StateContext stateContext) {
        stateStorage.save(stateContext.getPrincipalId(), stateContext);
        //TODO: add executor service
    }

    @Override
    public void removeAllPrincipalContext(TelegramId id) {
        if (id.isResolved()) {
            stateStorage.delete(id.telegramId());
        }
        principalService.deletePrincipalData(id);
    }
}
