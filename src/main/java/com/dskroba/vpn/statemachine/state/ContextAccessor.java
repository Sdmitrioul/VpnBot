package com.dskroba.vpn.statemachine.state;

import com.dskroba.vpn.actor.Context;
import com.dskroba.vpn.principal.Principal;
import com.dskroba.vpn.principal.Role;
import com.dskroba.vpn.type.UserVpnConfiguration;

import java.util.List;

public final class ContextAccessor {

    public static Long principalChatId() {
        return principal().key().chatId();
    }

    public static String principalTelegramHandle() {
        return principal().key().telegramHandle();
    }

    public static Principal principal() {
        return context().getPrincipal();
    }

    public static State getState() {
        return context().getState();
    }

    public static Role getPrincipalRole() {
        return context().getPrincipalRole();
    }

    public static List<UserVpnConfiguration> getPrincipalVpnConfigurations() {
        return principal().context().vpnConfigurations();
    }

    public static Context context() {
        return Context.PROVIDER.get();
    }

    private ContextAccessor() {
    }
}
