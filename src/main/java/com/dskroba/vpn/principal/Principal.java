package com.dskroba.vpn.principal;

import com.dskroba.vpn.telegram.TelegramId;
import com.dskroba.vpn.type.UserVpnConfiguration;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public record Principal(
        TelegramId key,
        PrincipalData context
) {

    public Long id() {
        return key.telegramId();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(TelegramId telegramId) {
        return new Builder().telegramId(telegramId);
    }

    public static Builder builder(Principal source) {
        return new Builder()
                .telegramId(source.key())
                .context(source.context());
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static class Builder {
        private TelegramId key;
        private PrincipalData context = new PrincipalData(List.of());

        public Builder telegramId(TelegramId telegramId) {
            this.key = telegramId;
            return this;
        }

        public Builder telegramId(Long telegramId, String handle, Long chatId) {
            this.key = new TelegramId(telegramId, handle, chatId);
            return this;
        }

        public Builder context(PrincipalData context) {
            this.context = context;
            return this;
        }

        public Builder addVpnConfiguration(UserVpnConfiguration configuration) {
            if (context == null) {
                this.context = new PrincipalData(List.of(configuration));
            } else {
                List<UserVpnConfiguration> vpnConfigurations = new LinkedList<>(context.vpnConfigurations());
                vpnConfigurations.add(configuration);
                this.context = new PrincipalData(List.copyOf(vpnConfigurations));
            }
            return this;
        }

        public Builder removeVpnConfiguration(String name) {
            if (context == null) {
                this.context = new PrincipalData(List.of());
            } else {
                this.context = new PrincipalData(context.vpnConfigurations()
                        .stream()
                        .filter(conf -> !Objects.equals(conf.name(), name))
                        .toList());
            }
            return this;
        }

        public Principal build() {
            return new Principal(key, context);
        }
    }
}
