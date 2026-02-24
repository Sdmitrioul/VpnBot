package com.dskroba.vpn.statemachine.effect;

public sealed interface Effect permits FileEffect, MessageEffect, SelectEffect, CompositeEffect {
    default CompositeEffect composite(Effect effect) {
        return new CompositeEffect(this, effect);
    }
}
