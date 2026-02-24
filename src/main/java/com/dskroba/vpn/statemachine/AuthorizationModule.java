package com.dskroba.vpn.statemachine;

import com.dskroba.vpn.exception.AuthorizationException;
import com.dskroba.vpn.principal.Principal;
import com.dskroba.vpn.telegram.TelegramId;

public interface AuthorizationModule {
    Principal authorize(TelegramId issuer) throws AuthorizationException;
}