package com.dskroba.vpn.statemachine;

import com.dskroba.vpn.principal.Principal;
import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.event.Event;
import com.dskroba.vpn.telegram.TelegramId;

public interface IOModule<I> {
    Event parse(I input, Principal issuer);

    TelegramId provideIssuer(I input);

    void handleEffect(Effect effect);
}

