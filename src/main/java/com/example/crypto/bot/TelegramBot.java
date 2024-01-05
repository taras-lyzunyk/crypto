package com.example.crypto.bot;

import com.example.crypto.configuration.BotConfig;
import com.example.crypto.service.CryptocurrencyService;
import com.example.crypto.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final CryptocurrencyService cryptocurrencyService;
    private final UserService userService;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    public void sendMessage(Long chatId, String textToSend, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        sendMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();


            if (userService.isUserLimitReached()) {
                sendMessage(chatId, "Sorry, there is no available space for you. Please try again later",
                        getStartKeyboardMarkup());
                return;
            }

            if (messageText.equals("/start")) {
                if (userService.isUserExists(chatId)) {
                    sendMessage(chatId, "Analysis is already in progress", getStopRestartKeyboardMarkup());
                } else {
                    sendMessage(chatId, "Hello, press start to analysis", getStartKeyboardMarkup());
                }
            }

            if (messageText.equals("Start")) {
                if (userService.isUserExists(chatId)) {
                    sendMessage(chatId, "Analysis is already in progress", getStopRestartKeyboardMarkup());
                } else {
                    sendMessage(chatId, "Analysis initiated \uD83D\uDC4D", getStopRestartKeyboardMarkup());
                    cryptocurrencyService.startCheck(chatId);
                }
            }

            if (messageText.equals("Restart")) {
                sendMessage(chatId, "Analysis restarted \uD83D\uDC4D", getStopRestartKeyboardMarkup());
                cryptocurrencyService.restartCheck(chatId);
            }

            if (messageText.equals("Stop")) {
                sendMessage(chatId, "Analysis stopped \uD83D\uDC4D", getStartKeyboardMarkup());
                cryptocurrencyService.stopCheck(chatId);
            }
        }
    }

    private ReplyKeyboardMarkup getStartKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Start");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup getStopRestartKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Restart");
        row.add("Stop");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}
