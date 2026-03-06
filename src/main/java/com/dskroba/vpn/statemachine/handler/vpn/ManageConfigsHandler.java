package com.dskroba.vpn.statemachine.handler.vpn;

import com.dskroba.vpn.actor.JobService;
import com.dskroba.vpn.actor.jobs.DeleteVpnConfigurationJob;
import com.dskroba.vpn.principal.PrincipalService;
import com.dskroba.vpn.service.VpnService;
import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.effect.FileEffect;
import com.dskroba.vpn.statemachine.effect.MessageEffect;
import com.dskroba.vpn.statemachine.effect.SelectEffect;
import com.dskroba.vpn.statemachine.event.Event;
import com.dskroba.vpn.statemachine.state.ContextAccessor;
import com.dskroba.vpn.statemachine.state.State;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;
import com.dskroba.vpn.type.SelectOption;
import com.dskroba.vpn.utils.VpnUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.dskroba.vpn.statemachine.state.descriptors.VpnDescriptors.CONFIGURATION_NAME_ATTRIBUTE;
import static com.dskroba.vpn.statemachine.state.descriptors.VpnDescriptors.MANAGE_VPN_CONFIGURATION;
import static com.dskroba.vpn.type.ContentType.CONF;
import static com.dskroba.vpn.type.ContentType.PNG;

@Component
public class ManageConfigsHandler extends AbstractVpnManagerHandler {
    private final JobService jobService;

    @Autowired
    public ManageConfigsHandler(VpnService vpnService, PrincipalService principalService, JobService jobService) {
        super(vpnService, principalService);
        this.jobService = jobService;
    }

    @Override
    protected Optional<Effect> handleStateEvents(Event event) {
        return handleMessageEvent(event)
                .flatMap(message -> handleView(message)
                        .or(() -> handleDelete(message)));
    }

    private Optional<Effect> handleDelete(String message) {
        if (!"Delete device configuration".equalsIgnoreCase(message)) {
            return Optional.empty();
        }
        jobService.submitJob(Optional.ofNullable(getPrincipalTelegramId())
                .orElse(ContextAccessor.principal().key()), new DeleteVpnConfigurationJob(getVpnConfigurationName(), vpnService));
        return Optional.of(MessageEffect.of("Configuration removed successfully!")
                .composite(switchToMenu()));
    }

    private Optional<Effect> handleView(String message) {
        if (!"View device configuration".equalsIgnoreCase(message)) {
            return Optional.empty();
        }
        String filename = getVpnConfigurationName();
        return Optional.of(loadVpnConfiguration(filename)
                .map(data -> FileEffect.of("Configuration file",
                                filename + CONF.filenameExtension(),
                                CONF,
                                data)
                        .composite(FileEffect.of("",
                                filename + PNG.filenameExtension(),
                                PNG,
                                VpnUtils.generateQrPng(new String(data))))
                        .composite(switchToMenu()))
                .orElse(MessageEffect.of("Unable to locate configuration, it may be removed by admin before.").composite(switchToMenu())));
    }

    private String getVpnConfigurationName() {
        return ContextAccessor.context().getAttribute(CONFIGURATION_NAME_ATTRIBUTE, String.class);
    }

    @Override
    public State createState() {
        return State.cancelable(MANAGE_VPN_CONFIGURATION,
                SelectEffect.withCancelOption("Delete or view device VPN configuration?",
                        List.of(SelectOption.fullRowOption("Delete device configuration"),
                                SelectOption.fullRowOption("View device configuration"))));
    }

    @Override
    public StateDescriptor stateDescriptor() {
        return MANAGE_VPN_CONFIGURATION;
    }
}