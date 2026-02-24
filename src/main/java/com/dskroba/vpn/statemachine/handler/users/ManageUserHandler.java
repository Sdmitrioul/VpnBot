package com.dskroba.vpn.statemachine.handler.users;

import com.dskroba.vpn.actor.ContextRemover;
import com.dskroba.vpn.actor.JobService;
import com.dskroba.vpn.actor.jobs.RemoveUserJob;
import com.dskroba.vpn.principal.PrincipalService;
import com.dskroba.vpn.service.VpnService;
import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.effect.MessageEffect;
import com.dskroba.vpn.statemachine.effect.SelectEffect;
import com.dskroba.vpn.statemachine.event.Event;
import com.dskroba.vpn.statemachine.handler.AbstractHandlerWithFactory;
import com.dskroba.vpn.statemachine.state.ContextAccessor;
import com.dskroba.vpn.statemachine.state.State;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;
import com.dskroba.vpn.telegram.TelegramId;
import com.dskroba.vpn.telegram.user.UserHandlerManager;
import com.dskroba.vpn.type.SelectOption;
import com.dskroba.vpn.utils.VpnPrincipalConfigurationEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Optional;

import static com.dskroba.vpn.statemachine.state.descriptors.UsersDescriptors.MANAGE_USER;
import static com.dskroba.vpn.statemachine.state.descriptors.UsersDescriptors.USER_ID_STATE_ATTRIBUTE;
import static com.dskroba.vpn.statemachine.state.descriptors.VpnDescriptors.SELECT_VPN_CONFIGURATION;

@Component
public class ManageUserHandler extends AbstractHandlerWithFactory {
    private static final String MANAGE_USER_DEVICES_OPTION = "Manage user devices";
    private static final String BLOCK_USER_OPTION = "Block user";

    private final PrincipalService principalService;
    private final UserHandlerManager userHandlerManager;
    private final JobService jobService;
    private final ContextRemover contextRemover;
    private final VpnService vpnService;

    @Autowired
    public ManageUserHandler(PrincipalService principalService, UserHandlerManager userHandlerManager, JobService jobService, ContextRemover contextRemover, VpnService vpnService) {
        this.principalService = principalService;
        this.userHandlerManager = userHandlerManager;
        this.jobService = jobService;
        this.contextRemover = contextRemover;
        this.vpnService = vpnService;
    }

    @Override
    protected Optional<Effect> handleStateEvents(Event event) {
        return handleMessageEvent(event)
                .flatMap(message -> handleBlockUser(message)
                        .or(() -> handleManageDevices(message)));
    }

    private Optional<Effect> handleManageDevices(String action) {
        if (!MANAGE_USER_DEVICES_OPTION.equals(action)) {
            return Optional.empty();
        }
        TelegramId storedHandler = ContextAccessor.context().getAttribute(USER_ID_STATE_ATTRIBUTE, TelegramId.class);
        if (storedHandler.isResolved()) {
            return Optional.of(moveToState(SELECT_VPN_CONFIGURATION));
        }
        var principal = principalService.loadPrincipal(storedHandler);
        if (principal == null) {
            return Optional.of(MessageEffect.of("User don't use VPN yet. Nothing to manage.").composite(switchToMenu()));
        }
        return Optional.of(moveToState(SELECT_VPN_CONFIGURATION));
    }

    private Optional<Effect> handleBlockUser(String action) {
        if (!BLOCK_USER_OPTION.equals(action)) {
            return Optional.empty();
        }
        TelegramId storedHandler = ContextAccessor.context().getAttribute(USER_ID_STATE_ATTRIBUTE, TelegramId.class);
        userHandlerManager.removeUser(storedHandler.telegramHandle());
        var principal = principalService.loadPrincipal(storedHandler);
        if (principal == null) {
            return Optional.of(MessageEffect.of("User with handler %s blocked".formatted(storedHandler.telegramHandle())).composite(switchToMenu()));
        }
        principal.context().vpnConfigurations().forEach(
                config -> vpnService.deleteUserConfig(VpnPrincipalConfigurationEncoder.configurationName(principal.key(), config.name()))
        );
        jobService.submitJob(principal.key(), new RemoveUserJob(contextRemover));
        return Optional.of(MessageEffect.of("User was blocked and all his devices disconnected!").composite(switchToMenu()));
    }

    @Override
    public State createState() {
        var selectOptions = new LinkedList<SelectOption>();
        String message;
        TelegramId selectedTelegramId = ContextAccessor.context().getAttribute(USER_ID_STATE_ATTRIBUTE, TelegramId.class);
        if (selectedTelegramId.isResolved()) {
            selectOptions.add(SelectOption.fullRowOption(MANAGE_USER_DEVICES_OPTION));
            message = "Choose option action";
        } else {
            message = "User do not use VPN, you can block him";
        }
        selectOptions.add(SelectOption.fullRowOption(BLOCK_USER_OPTION));
        return State.cancelable(stateDescriptor(), SelectEffect.withCancelOption(message, selectOptions));
    }

    @Override
    public StateDescriptor stateDescriptor() {
        return MANAGE_USER;
    }
}
