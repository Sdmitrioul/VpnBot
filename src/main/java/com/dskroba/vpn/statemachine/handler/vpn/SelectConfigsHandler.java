package com.dskroba.vpn.statemachine.handler.vpn;

import com.dskroba.vpn.principal.PrincipalService;
import com.dskroba.vpn.service.VpnService;
import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.effect.SelectEffect;
import com.dskroba.vpn.statemachine.event.Event;
import com.dskroba.vpn.statemachine.state.State;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;
import com.dskroba.vpn.type.SelectOption;
import com.dskroba.vpn.type.UserVpnConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.dskroba.vpn.statemachine.state.descriptors.VpnDescriptors.*;

@Component
public class SelectConfigsHandler extends AbstractVpnManagerHandler {
    private static final String GO_TO_MENU_OPTION = "Go to menu";

    @Autowired
    public SelectConfigsHandler(VpnService vpnService, PrincipalService principalService) {
        super(vpnService, principalService);
    }

    @Override
    protected Optional<Effect> handleStateEvents(Event event) {
        return handleMessageEvent(event)
                .filter(this::checkOption)
                .map(this::storeConfigurationName)
                .or(() -> handleMessageEvent(event)
                        .map(this::gotToMenu));
    }

    private Effect gotToMenu(String message) {
        if (GO_TO_MENU_OPTION.equals(message)) {
            return switchToMenu();
        }
        return null;
    }

    private Effect storeConfigurationName(String configurationName) {
        return moveToState(MANAGE_VPN_CONFIGURATION,
                context -> context.setAttribute(CONFIGURATION_NAME_ATTRIBUTE, configurationName));
    }

    private boolean checkOption(String configurationName) {
        return userVpnConfigurations().stream().map(UserVpnConfiguration::name).anyMatch(configurationName::equals);
    }

    @Override
    public State createState() {
        List<UserVpnConfiguration> userVpnConfigurations = userVpnConfigurations();
        if (userVpnConfigurations().isEmpty()) {
            return State.cancelable(SELECT_VPN_CONFIGURATION,
                    SelectEffect.withCancelOption("No available devices, go to menu.", List.of(SelectOption.fullRowOption(GO_TO_MENU_OPTION))));
        }
        return State.cancelable(SELECT_VPN_CONFIGURATION,
                SelectEffect.withCancelOption("Select device", userVpnConfigurations
                        .stream()
                        .map(UserVpnConfiguration::name)
                        .map(SelectOption::fullRowOption)
                        .toList()));
    }

    @Override
    public StateDescriptor stateDescriptor() {
        return SELECT_VPN_CONFIGURATION;
    }
}
