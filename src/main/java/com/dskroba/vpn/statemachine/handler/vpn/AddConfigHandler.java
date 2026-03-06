package com.dskroba.vpn.statemachine.handler.vpn;

import com.dskroba.vpn.principal.PrincipalService;
import com.dskroba.vpn.principal.Role;
import com.dskroba.vpn.service.VpnService;
import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.effect.FileEffect;
import com.dskroba.vpn.statemachine.effect.MessageEffect;
import com.dskroba.vpn.statemachine.event.Event;
import com.dskroba.vpn.statemachine.state.ContextAccessor;
import com.dskroba.vpn.statemachine.state.State;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;
import com.dskroba.vpn.statemachine.state.descriptors.VpnDescriptors;
import com.dskroba.vpn.type.UserVpnConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.dskroba.vpn.statemachine.state.descriptors.VpnDescriptors.ADD_VPN_CONFIGURATION;
import static com.dskroba.vpn.type.ContentType.CONF;
import static com.dskroba.vpn.type.ContentType.PNG;
import static com.dskroba.vpn.utils.VpnPrincipalConfigurationEncoder.configurationName;

@Component
public class AddConfigHandler extends AbstractVpnManagerHandler {
    private static final State STATE_INSTANCE = State.cancelable(VpnDescriptors.ADD_VPN_CONFIGURATION, MessageEffect.of("Provide unique name for device"));

    @Autowired
    public AddConfigHandler(VpnService vpnService, PrincipalService principalService) {
        super(vpnService, principalService);
    }

    @Override
    protected Optional<Effect> handleStateEvents(Event event) {
        return handleMessageEvent(event)
                .map(configurationName ->
                        checkAgainstConstraints(configurationName)
                                .orElseGet(() -> generateConfiguration(configurationName)));
    }

    private Effect generateConfiguration(String configurationName) {
        VpnService.UserConfiguration configuration = vpnService.createUserConfiguration(configurationName(ContextAccessor.principal().key(), configurationName));
        ContextAccessor.context().setPrincipal(
                ContextAccessor.principal().toBuilder()
                        .addVpnConfiguration(new UserVpnConfiguration(configurationName, configuration.file()))
                        .build());
        return FileEffect.of("Paste this configuration in WireGuard app.\nTreat this configuration as password.", configurationName + CONF.filenameExtension(), CONF, configuration.file())
                .composite(FileEffect.of("", configurationName + PNG.filenameExtension(), PNG, configuration.image()))
                .composite(switchToMenu());
    }

    private Optional<Effect> checkAgainstConstraints(String configurationName) {
        return checkLetters(configurationName)
                .or(() -> checkNameDuplication(configurationName))
                .or(this::checkLimits);
    }

    private Optional<Effect> checkLimits() {
        if (ContextAccessor.getPrincipalRole().allowed(Role.ADMIN)) {
            return Optional.empty();
        }
        if (ContextAccessor.principal().context().vpnConfigurations().size() >= 3) {
            return Optional.of(MessageEffect.of("Limit per user is 3 devices").composite(switchToMenu()));
        }
        return Optional.empty();
    }

    private static Optional<Effect> checkLetters(String configurationName) {
        for (char character : configurationName.toCharArray()) {
            if (!(Character.isLetter(character) || Character.isDigit(character) || character == '_' || character == '-')) {
                return Optional.of(MessageEffect.of("""
                        Name could contain only letters and digits or _ sign.
                        Try another one"""));
            }
        }
        return Optional.empty();
    }

    private static Optional<Effect> checkNameDuplication(String configurationName) {
        List<UserVpnConfiguration> existingConfiguration = ContextAccessor.getPrincipalVpnConfigurations();
        for (UserVpnConfiguration configuration : existingConfiguration) {
            if (Objects.equals(configuration.name(), configurationName)) {
                return Optional.of(MessageEffect.of("""
                        Device with name %s already exist.
                        Try another one
                        """.formatted(configuration)));
            }
        }
        return Optional.empty();
    }

    @Override
    public State createState() {
        return STATE_INSTANCE;
    }

    @Override
    public StateDescriptor stateDescriptor() {
        return ADD_VPN_CONFIGURATION;
    }
}
