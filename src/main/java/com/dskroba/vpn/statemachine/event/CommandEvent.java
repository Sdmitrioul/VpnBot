package com.dskroba.vpn.statemachine.event;

import com.dskroba.vpn.principal.Principal;

public record CommandEvent(String payload, Principal issuer) implements Event {
}
