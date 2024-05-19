package com.ss.client.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {
    @Value("${telegram-bot.username}")
    private String botUsername;

    @Value("${telegram-bot.token}")
    private String botToken;

    @Value("${telegram-bot.group-id}")
    private Long groupId;

    @Override
    public void onUpdateReceived(Update update) {
//        try {
//            SendMessage sendMessage = new SendMessage();
//            sendMessage.setChatId(update.getMessage().getChatId());
//            sendMessage.setText("Hi!");
//            log.info(sendMessage.getChatId());
//            execute(sendMessage);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void sendMessage(String message) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setParseMode("Markdown");
            sendMessage.setChatId(groupId);
            sendMessage.setText(message);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendPhoto(List<String> pathFiles) {
        try {
            if (pathFiles.size() == 1) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(groupId);
                sendPhoto.setPhoto(new InputFile(pathFiles.get(0)));
                execute(sendPhoto);
            } else {
                List<InputMedia> media = new ArrayList<>();
                for (int i = 0; i < pathFiles.size() || i == 9; i++) {
                    InputMediaPhoto mediaPhoto = new InputMediaPhoto();
                    mediaPhoto.setMedia(pathFiles.get(i));
                    media.add(mediaPhoto);
                }
                SendMediaGroup sendMediaGroup = new SendMediaGroup();
                sendMediaGroup.setChatId(groupId);
                sendMediaGroup.setMedias(media);
                execute(sendMediaGroup);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public void sendDocument(File file) {
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(groupId);
        sendDocumentRequest.setDocument(new InputFile(file));

        try {
            execute(sendDocumentRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}