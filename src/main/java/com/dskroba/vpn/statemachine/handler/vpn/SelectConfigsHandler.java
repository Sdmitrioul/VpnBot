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
    static final String CONFIG_KEY_SEPARATOR = "|";

    @Autowired
    public SelectConfigsHandler(VpnService vpnService, PrincipalService principalService) {
        super(vpnService, principalService);
    }

    @Override
    protected Optional<Effect> handleStateEvents(Event event) {
        return handleMessageEvent(event)
                .filter(this::checkOption)
                .flatMap(this::configKeyFromLabel)
                .map(this::storeConfigurationKey)
                .or(() -> handleMessageEvent(event)
                        .map(this::gotToMenu));
    }

    private Effect gotToMenu(String message) {
        if (GO_TO_MENU_OPTION.equals(message)) {
            return switchToMenu();
        }
        return null;
    }

    private Effect storeConfigurationKey(String configKey) {
        return moveToState(MANAGE_VPN_CONFIGURATION,
                context -> context.setAttribute(CONFIGURATION_NAME_ATTRIBUTE, configKey));
    }

    private boolean checkOption(String message) {
        if (GO_TO_MENU_OPTION.equals(message)) {
            return false;
        }
        return configKeyFromLabel(message).isPresent();
    }

    private Optional<String> configKeyFromLabel(String label) {
        List<UserVpnConfiguration> configs = userVpnConfigurations();
        for (UserVpnConfiguration c : configs) {
            if (configLabel(c).equals(label)) {
                return Optional.of(configKey(c.name(), c.vpnInterface()));
            }
        }
        return Optional.empty();
    }

    private String configLabel(UserVpnConfiguration c) {
        String displayName = vpnService.getInterfaceConfig(c.vpnInterface()).displayName();
        return c.name() + " (" + displayName + ")";
    }

    static String configKey(String name, String vpnInterface) {
        return name + CONFIG_KEY_SEPARATOR + (vpnInterface != null ? vpnInterface : "");
    }

    public static String parseConfigName(String configKey) {
        if (configKey == null || !configKey.contains(CONFIG_KEY_SEPARATOR)) {
            return configKey != null ? configKey : "";
        }
        return configKey.substring(0, configKey.indexOf(CONFIG_KEY_SEPARATOR));
    }

    public static String parseConfigInterface(String configKey) {
        if (configKey == null || !configKey.contains(CONFIG_KEY_SEPARATOR)) {
            return null;
        }
        String id = configKey.substring(configKey.indexOf(CONFIG_KEY_SEPARATOR) + 1);
        return id.isEmpty() ? null : id;
    }

    @Override
    public State createState() {
        List<UserVpnConfiguration> userVpnConfigurations = userVpnConfigurations();
        if (userVpnConfigurations.isEmpty()) {
            return State.cancelable(SELECT_VPN_CONFIGURATION,
                    SelectEffect.withCancelOption("No available devices, go to menu.", List.of(SelectOption.fullRowOption(GO_TO_MENU_OPTION))));
        }
        return State.cancelable(SELECT_VPN_CONFIGURATION,
                SelectEffect.withCancelOption("Select device", userVpnConfigurations
                        .stream()
                        .map(c -> SelectOption.fullRowOption(configLabel(c)))
                        .toList()));
    }

    @Override
    public StateDescriptor stateDescriptor() {
        return SELECT_VPN_CONFIGURATION;
    }
}
