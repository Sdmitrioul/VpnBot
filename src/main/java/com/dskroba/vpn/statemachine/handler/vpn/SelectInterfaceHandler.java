package com.dskroba.vpn.statemachine.handler.vpn;

import com.dskroba.vpn.service.VpnService;
import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.effect.SelectEffect;
import com.dskroba.vpn.statemachine.event.Event;
import com.dskroba.vpn.statemachine.handler.AbstractHandlerWithFactory;
import com.dskroba.vpn.statemachine.state.State;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;
import com.dskroba.vpn.type.InterfaceConfig;
import com.dskroba.vpn.type.SelectOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.dskroba.vpn.statemachine.state.descriptors.VpnDescriptors.ADD_VPN_CONFIGURATION;
import static com.dskroba.vpn.statemachine.state.descriptors.VpnDescriptors.PENDING_INTERFACE_ATTRIBUTE;
import static com.dskroba.vpn.statemachine.state.descriptors.VpnDescriptors.SELECT_VPN_INTERFACE;

@Component
public class SelectInterfaceHandler extends AbstractHandlerWithFactory {
    private final VpnService vpnService;

    @Autowired
    public SelectInterfaceHandler(VpnService vpnService) {
        this.vpnService = vpnService;
    }

    @Override
    protected Optional<Effect> handleStateEvents(Event event) {
        return handleMessageEvent(event)
                .flatMap(this::findInterfaceByDisplayName)
                .map(interfaceId -> moveToState(ADD_VPN_CONFIGURATION, ctx -> ctx.setAttribute(PENDING_INTERFACE_ATTRIBUTE, interfaceId)));
    }

    private Optional<String> findInterfaceByDisplayName(String displayName) {
        return vpnService.getInterfaceConfigs().stream()
                .filter(c -> c.displayName().equals(displayName))
                .map(InterfaceConfig::interfaceId)
                .findFirst();
    }

    @Override
    public State createState() {
        List<SelectOption> options = vpnService.getInterfaceConfigs().stream()
                .map(InterfaceConfig::displayName)
                .map(SelectOption::fullRowOption)
                .toList();
        return State.cancelable(SELECT_VPN_INTERFACE, SelectEffect.withCancelOption("Select network for new device", options));
    }

    @Override
    public StateDescriptor stateDescriptor() {
        return SELECT_VPN_INTERFACE;
    }
}
