package com.dskroba.vpn.statemachine.handler;

import com.dskroba.vpn.principal.Role;
import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.effect.SelectEffect;
import com.dskroba.vpn.statemachine.event.Event;
import com.dskroba.vpn.statemachine.state.State;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;
import com.dskroba.vpn.type.SelectOption;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dskroba.vpn.statemachine.state.ContextAccessor.getPrincipalRole;

public abstract class StateSelector extends AbstractHandlerWithFactory {
    private final Map<String, StateSelectorData> stateSelectorData;

    protected StateSelector(List<StateSelectorData> eventToStateMap) {
        this.stateSelectorData = eventToStateMap
                .stream()
                .collect(Collectors.toMap(StateSelectorData::optionValue, Function.identity()));
    }

    @Override
    protected Optional<Effect> handleStateEvents(Event event) {
        return handleMessageEvent(event)
                .map(stateSelectorData::get)
                .filter(selector -> selector.userRole().allowed(getPrincipalRole()))
                .map(this::peek)
                .map(StateSelectorData::stateDescriptor)
                .map(this::moveToState);
    }

    protected StateSelectorData peek(StateSelectorData stateSelectorData) {
        return stateSelectorData;
    }

    @Override
    public State createState() {
        return new State(stateDescriptor(), SelectEffect.of(selectMessage(), stateSelectorData
                .values()
                .stream()
                .filter(data -> data.userRole().allowed(getPrincipalRole()))
                .map(StateSelectorData::optionValue)
                .map(SelectOption::fullRowOption)
                .toList()), cancelable());
    }

    protected String selectMessage() {
        return "Select available option";
    }

    protected boolean cancelable() {
        return false;
    }

    protected static StateSelectorData adminSelector(String optionValue, StateDescriptor stateDescriptor) {
        return new StateSelectorData(optionValue, stateDescriptor, Role.ADMIN);
    }

    protected static StateSelectorData userSelector(String optionValue, StateDescriptor stateDescriptor) {
        return new StateSelectorData(optionValue, stateDescriptor, Role.USER);
    }

    protected record StateSelectorData(String optionValue, StateDescriptor stateDescriptor, Role userRole) {
    }
}
