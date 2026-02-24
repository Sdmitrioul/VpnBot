package com.dskroba.vpn.telegram;

import com.dskroba.vpn.type.SelectOption;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class TelegramUtils {
    public static User user(Update update) {
        return getMessage(update)
                .map(Message::from)
                .orElse(null);
    }

    public static Long chatId(Update update) {
        return getMessage(update)
                .map(Message::chat)
                .map(Chat::id)
                .orElse(null);
    }

    public static String text(Update update) {
        return getMessage(update)
                .map(Message::text)
                .orElse(null);
    }

    public static String fileId(Update update) {
        return getDocument(update)
                .map(Document::fileId)
                .orElse(null);
    }

    public static String memeType(Update update) {
        return getDocument(update)
                .map(Document::mimeType)
                .orElse(null);
    }

    @NotNull
    private static Optional<Message> getMessage(Update update) {
        return Optional.ofNullable(update)
                .map(Update::message);
    }

    @NotNull
    private static Optional<Document> getDocument(Update update) {
        return getMessage(update)
                .map(Message::document);
    }

    @NotNull
    public static List<KeyboardButton[]> getKeyboardButtons(List<SelectOption> sortedOptions) {
        List<KeyboardButton[]> buttons = new ArrayList<>();
        Queue<KeyboardButton> queue = new LinkedList<>();
        for (SelectOption option : sortedOptions) {
            if (option.isFullRow()) {
                buttons.add(queue.toArray(new KeyboardButton[0]));
                queue.clear();
                buttons.add(new KeyboardButton[]{new KeyboardButton(option.value())});
                continue;
            } else if (queue.size() == 3) {
                buttons.add(queue.toArray(new KeyboardButton[0]));
                queue.clear();
            }
            queue.add(new KeyboardButton(option.value()));
        }
        if (!queue.isEmpty()) {
            buttons.add(queue.toArray(new KeyboardButton[0]));
        }
        return buttons;
    }

    private TelegramUtils() {
    }
}

