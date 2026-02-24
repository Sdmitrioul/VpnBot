package com.dskroba.vpn.telegram;

import com.dskroba.vpn.exception.CustomException;
import com.dskroba.vpn.principal.Principal;
import com.dskroba.vpn.statemachine.IOModule;
import com.dskroba.vpn.statemachine.effect.*;
import com.dskroba.vpn.statemachine.event.CommandEvent;
import com.dskroba.vpn.statemachine.event.Event;
import com.dskroba.vpn.statemachine.event.MessageEvent;
import com.dskroba.vpn.statemachine.state.ContextAccessor;
import com.dskroba.vpn.type.ContentType;
import com.dskroba.vpn.type.FileData;
import com.dskroba.vpn.type.SelectOption;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static com.dskroba.vpn.base.Utils.getFileExtension;

public class TelegramIOModule implements IOModule<Update> {
    private static final Logger log = LogManager.getLogger(TelegramIOModule.class);

    private final TelegramBot bot;

    public TelegramIOModule(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public Event parse(Update input, Principal issuer) {
        String message = TelegramUtils.text(input);
        if (message != null && message.startsWith("/")) {
            return new CommandEvent(message.substring(1), issuer);
        }
        String fileId = TelegramUtils.fileId(input);
        if (fileId == null) {
            return new MessageEvent(message, issuer);
        }
        //FileData fileData = load(fileId, TelegramUtils.memeType(input));
        //return FileUploadEvent.create(message, fileData, issuer);
        return new MessageEvent(message, issuer);
    }

    public FileData load(String fileId, String memeType) {
        GetFileResponse getFileResponse = bot.execute(new GetFile(fileId));
        File file = getFileResponse.file();
        String fileUrl = bot.getFullFilePath(file);

        try (BufferedInputStream in = new BufferedInputStream(new URI(fileUrl).toURL().openStream());
             ByteArrayOutputStream fileOutputStream = new ByteArrayOutputStream()) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            String filename = file.filePath();
            return new FileData(filename, new ContentType(memeType, getFileExtension(filename)), fileOutputStream.toByteArray());
        } catch (URISyntaxException e) {
            log.error("Invalid URI: {}", fileUrl, e);
            throw new CustomException("Invalid URI: " + fileUrl, e);
        } catch (MalformedURLException e) {
            log.error("Malformed URL: {}", fileUrl, e);
            throw new CustomException("Malformed URL: " + fileUrl, e);
        } catch (IOException e) {
            log.error("Error while reading file: {}", fileUrl, e);
            throw new CustomException("Error while reading file: " + fileUrl, e);
        }
    }

    @Override
    public TelegramId provideIssuer(Update input) {
        User user = TelegramUtils.user(input);
        if (user == null) {
            return null;
        }
        Long id = user.id();
        String name = user.username();
        Long chatId = TelegramUtils.chatId(input);
        return new TelegramId(id, name, chatId);
    }

    @Override
    public void handleEffect(Effect effect) {
        switch (effect) {
            case MessageEffect messageEffect -> handleEffect(messageEffect);
            case FileEffect fileEffect -> handleEffect(fileEffect);
            case SelectEffect selectEffect -> handleEffect(selectEffect);
            case CompositeEffect compositeEffect -> handleEffect(compositeEffect);
        }
    }

    private void handleEffect(CompositeEffect effect) {
        effect.payload().forEach(this::handleEffect);
    }

    private void handleEffect(MessageEffect effect) {
        bot.execute(new SendMessage(chatId(), effect.payload()));
    }

    private void handleEffect(FileEffect effect) {
        FileData fileData = effect.payload().fileData();
        SendDocument message = new SendDocument(chatId(), fileData.data())
                .contentType(fileData.contentType().value())
                .fileName(fileData.filename());
        String caption = effect.payload().message();
        if (caption != null) {
            message = message.caption(caption);
        }
        bot.execute(message);
    }

    private void handleEffect(SelectEffect effect) {
        var payload = effect.payload();
        bot.execute(new SendMessage(chatId(), payload.message())
                .replyMarkup(generateReplyMarkup(payload.selectOptionList())));
    }

    private Keyboard generateReplyMarkup(List<SelectOption> selectOptions) {
        List<SelectOption> sortedOptions = selectOptions.stream()
                .filter(Predicate.not(SelectOption::isHidden))
                .sorted(Comparator.comparingInt(SelectOption::priority).reversed())
                .toList();
        KeyboardButton[][] buttonArray = TelegramUtils.getKeyboardButtons(sortedOptions)
                .toArray(KeyboardButton[][]::new);
        return new ReplyKeyboardMarkup(buttonArray)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true);
    }

    private static Long chatId() {
        return ContextAccessor.principalChatId();
    }
}
