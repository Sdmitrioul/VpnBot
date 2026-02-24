package com.dskroba.vpn.utils;

import com.dskroba.vpn.telegram.TelegramId;

public final class VpnPrincipalConfigurationEncoder {
    public static String configurationName(TelegramId principalId, String configurationName) {
        return "%s_%s".formatted(principalId.telegramId(), configurationName);
    }

    private VpnPrincipalConfigurationEncoder() {
    }
}
