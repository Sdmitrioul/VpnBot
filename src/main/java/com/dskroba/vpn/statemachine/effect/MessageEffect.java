package com.dskroba.vpn.statemachine.effect;

public record MessageEffect(String payload) implements Effect {
    public static MessageEffect of(String payload) {
        return new MessageEffect(payload);
    }
}
