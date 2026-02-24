package com.dskroba.vpn.statemachine.effect;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class CompositeEffect implements Effect {
    private final List<Effect> effects = new LinkedList<>();

    CompositeEffect(Effect first, Effect second) {
        effects.add(first);
        this.composite(second);
    }

    CompositeEffect(List<Effect> payload) {
        effects.addAll(payload);
    }

    @Override
    public CompositeEffect composite(Effect effect) {
        if (effect instanceof CompositeEffect compositeEffect) {
            this.effects.addAll(compositeEffect.payload());
            return this;
        }
        this.effects.add(effect);
        return this;
    }

    public List<Effect> payload() {
        return effects;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CompositeEffect that)) return false;
        return Objects.equals(effects, that.effects);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(effects);
    }

    @Override
    public String toString() {
        return "CompositeEffect{" +
                "effects=" + effects +
                '}';
    }
}
