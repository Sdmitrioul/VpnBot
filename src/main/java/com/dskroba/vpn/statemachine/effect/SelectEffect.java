package com.dskroba.vpn.statemachine.effect;

import com.dskroba.vpn.type.SelectOption;

import java.util.ArrayList;
import java.util.List;

public record SelectEffect(SelectPayload payload) implements Effect {
    public record SelectPayload(String message, List<SelectOption> selectOptionList) {
    }

    public static SelectEffect withCancelOption(String message, List<SelectOption> options) {
        List<SelectOption> list = new ArrayList<>(options);
        list.add(SelectOption.CANCEL_OPTION);
        return of(message, list);
    }

    public static SelectEffect of(String message, List<SelectOption> selectOptionList) {
        return new SelectEffect(new SelectPayload(message, selectOptionList));
    }

    public static SelectEffect of(List<SelectOption> selectOptionList) {
        return of("Please, choose option from provided one!", selectOptionList);
    }

    public static SelectEffect unknownOption(SelectEffect effect) {
        return new SelectEffect(new SelectPayload("Unrecognized option!\nPlease, choose option from provided one!",
                effect.payload().selectOptionList()));
    }
}
