package com.example.poctelegram.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.ChosenInlineQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.telegram.telegrambots.meta.api.methods.ParseMode.MARKDOWN;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBot.class);

    private final String botUsername;

    public TelegramBot(@Value("${telegram.bot.username}") String botUsername,
                       @Value("${telegram.bot.token}") String botToken,
                       TelegramBotsApi telegramBotsApi) throws TelegramApiException {
        super(new DefaultBotOptions(), botToken);
        this.botUsername = botUsername;
        telegramBotsApi.registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        LOGGER.info("New update!");

        // Para comandos
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            LOGGER.info("Message: " + message.getText());

            try {
                if (message.getText().startsWith("/start"))
                    handleStartCommand(message);
                else if (message.getText().startsWith("/inline-keyboard")) {
                    handleInlineKeyboardCommand(message);
                } else if (message.getText().startsWith("/web-app")) {
                    handleWebAppInlineKeyboardCommand(message);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        // Para lo que devuelven las WebApps
        if (update.hasMessage() && update.getMessage().getWebAppData() != null) {
            Message message = update.getMessage();
            LOGGER.info("WebApp data: " + message.getWebAppData());

            try {
                handleWebAppData(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        // Para los resultados de presionar un botón en un InlineKeyboard
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String message = callbackQuery.getMessage().getText();
            String data = callbackQuery.getData();
            LOGGER.info("CallbackQuery: Message: " + message + " - Data: " + data);
        }

        if (update.hasChosenInlineQuery()) {
            ChosenInlineQuery query = update.getChosenInlineQuery();
            String resultId = query.getResultId();
            LOGGER.info("ChosenInlineQuery: " + resultId);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    private void handleStartCommand(Message message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Hi, @" + message.getFrom().getUserName() + "!");

        execute(sendMessage);
    }

    private void handleInlineKeyboardCommand(Message message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Acción 1");
        button1.setCallbackData("acción 1");
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Acción 2");
        button2.setCallbackData("acción 2");
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Acción 3");
        button3.setCallbackData("acción 3");
        row1.add(button1);
        row1.add(button2);
        row2.add(button3);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(row2);

        keyboardMarkup.setKeyboard(rows);

        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Select an option");
        sendMessage.setReplyMarkup(keyboardMarkup);

        execute(sendMessage);
    }

    private void handleWebAppKeyboardCommand(Message message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        KeyboardRow row1 = new KeyboardRow();
        KeyboardButton button1 = new KeyboardButton();
        button1.setText("Open");
        button1.setWebApp(new WebAppInfo("https://matibf99.github.io/telegram-web-app-bot-example/"));
        row1.add(button1);

        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row1);

        keyboardMarkup.setKeyboard(rows);

        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Open the web app");
        sendMessage.setReplyMarkup(keyboardMarkup);

        execute(sendMessage);
    }

    private void handleWebAppInlineKeyboardCommand(Message message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Open");
        button1.setWebApp(new WebAppInfo("https://matibf99.github.io/telegram-web-app-bot-example/"));
        row1.add(button1);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row1);

        keyboardMarkup.setKeyboard(rows);

        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Open the web app");
        sendMessage.setReplyMarkup(keyboardMarkup);

        execute(sendMessage);
    }

    private void handleDateEpochCommand(Message message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();

        Instant instant = Instant.ofEpochSecond(message.getDate().longValue());
        ZonedDateTime time = ZonedDateTime.ofInstant(instant, ZonedDateTime.now().getZone());

        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm").format(time));

        execute(sendMessage);
    }

    private void handleWebAppData(Message message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(message.getChatId());
        sendMessage.setParseMode(MARKDOWN);
        sendMessage.setText("Received:\n\n```\n" + message.getWebAppData().getData() + "\n```");
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));

        execute(sendMessage);
    }
}
