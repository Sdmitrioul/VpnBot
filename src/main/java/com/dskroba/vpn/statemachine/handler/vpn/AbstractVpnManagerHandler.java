package com.dskroba.vpn.statemachine.handler.vpn;

import com.dskroba.vpn.principal.Principal;
import com.dskroba.vpn.principal.PrincipalData;
import com.dskroba.vpn.principal.PrincipalService;
import com.dskroba.vpn.service.VpnService;
import com.dskroba.vpn.statemachine.handler.AbstractHandlerWithFactory;
import com.dskroba.vpn.statemachine.state.ContextAccessor;
import com.dskroba.vpn.telegram.TelegramId;
import com.dskroba.vpn.type.UserVpnConfiguration;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.dskroba.vpn.statemachine.state.descriptors.UsersDescriptors.USER_ID_STATE_ATTRIBUTE;

public abstract class AbstractVpnManagerHandler extends AbstractHandlerWithFactory {
    protected final VpnService vpnService;
    protected final PrincipalService principalService;

    public AbstractVpnManagerHandler(VpnService vpnService, PrincipalService principalService) {
        this.vpnService = vpnService;
        this.principalService = principalService;
    }

    protected List<UserVpnConfiguration> userVpnConfigurations() {
        TelegramId principalId = getPrincipalTelegramId();
        if (principalId == null) {
            return ContextAccessor.getPrincipalVpnConfigurations();
        }
        return loadPrincipalVpnConfigurations(principalId);
    }

    protected TelegramId getPrincipalTelegramId() {
        return ContextAccessor.context().getAttribute(USER_ID_STATE_ATTRIBUTE, TelegramId.class);
    }

    protected Optional<byte[]> loadVpnConfiguration(String configurationName, String vpnInterface) {
        return Optional.ofNullable(getPrincipalTelegramId())
                .map(principalService::loadPrincipal)
                .or(() -> Optional.ofNullable(ContextAccessor.principal()))
                .map(Principal::context)
                .map(PrincipalData::vpnConfigurations)
                .flatMap(configurations -> configurations.stream()
                        .filter(conf -> conf.name().equals(configurationName) && Objects.equals(conf.vpnInterface(), vpnInterface))
                        .findAny())
                .map(UserVpnConfiguration::content);
    }

    private List<UserVpnConfiguration> loadPrincipalVpnConfigurations(TelegramId principalId) {
        return Optional.ofNullable(principalService.loadPrincipal(principalId))
                .map(Principal::context)
                .map(PrincipalData::vpnConfigurations)
                .orElse(List.of());
    }
}
