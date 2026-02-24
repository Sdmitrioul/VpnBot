package com.dskroba.vpn.telegram;

import java.util.Objects;

public record TelegramId(
        Long telegramId,
        String telegramHandle,
        Long chatId
) {
    public static TelegramId unresolved(String telegramHandle) {
        return new TelegramId(null, telegramHandle, null);
    }

    public boolean isResolved() {
        return telegramId != null;
    }

    public boolean hasSameMetadata(TelegramId other) {
        if (other == null) return false;
        return Objects.equals(this.telegramHandle, other.telegramHandle)
                && Objects.equals(this.chatId, other.chatId);
    }
}