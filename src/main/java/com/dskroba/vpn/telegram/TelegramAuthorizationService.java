package com.dskroba.vpn.telegram;

import com.dskroba.vpn.exception.AuthorizationException;
import com.dskroba.vpn.principal.Principal;
import com.dskroba.vpn.principal.PrincipalService;
import com.dskroba.vpn.statemachine.AuthorizationModule;
import com.dskroba.vpn.telegram.user.UserHandlersProvider;

public class TelegramAuthorizationService implements AuthorizationModule {
    private final PrincipalService principalService;
    private final UserHandlersProvider userHandlersProvider;

    public TelegramAuthorizationService(PrincipalService principalService, UserHandlersProvider userHandlersProvider) {
        this.principalService = principalService;
        this.userHandlersProvider = userHandlersProvider;
    }

    @Override
    public Principal authorize(TelegramId issuer) throws AuthorizationException {
        if (issuer == null
                || (!userHandlersProvider.adminHandlers().contains(issuer.telegramHandle())
                && !userHandlersProvider.userHandles().contains(issuer.telegramHandle()))) {
            throw new AuthorizationException("User is not authorized");
        }
        return principalService.authenticate(issuer);
    }
}