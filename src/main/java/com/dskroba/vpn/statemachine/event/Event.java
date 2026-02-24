package com.dskroba.vpn.statemachine.event;

import com.dskroba.vpn.principal.Principal;

public sealed interface Event permits MessageEvent, CommandEvent {
    Principal issuer();
}
