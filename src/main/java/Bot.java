
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;



import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;



public class Bot extends TelegramLongPollingBot {

    public static void main(String[] args){
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
            @Override
            public void onUpdateReceived (Update update){
                Message msg = update.getMessage();
                Model model = new Model();
                if (update.hasMessage()) {
                    if (update.getMessage().hasText()) {
                        switch (msg.getText()) {
                            case "/start":
                                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                                inlineKeyboardButton.setText("Тык");
                                inlineKeyboardButton.setCallbackData("Кнопка \"Тык\" была нажата");
                                List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
                                keyboardButtons.add(inlineKeyboardButton);
                                List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                                rowList.add(keyboardButtons);
                                inlineKeyboardMarkup.setKeyboard(rowList);
                                SendMessage message = new SendMessage();
                                message.setChatId(msg.getChatId().toString());
                                message.setText("Пример");
                                message.setReplyMarkup(inlineKeyboardMarkup);
                                try {

                                    execute(message);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "/help":
                                sendMsg(msg, "Тут буду помогать");
                                break;
                            case "/setting":
                                sendMsg(msg, "Тут буду настраивать");
                                break;
                            default:
                                try {
                                    Weather.getWeather(msg.getText(), model);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                //JSONObject object = new JSONObject(response);
                                //System.out.println(object.getString("limit_type"));
//                            try {
//                                sendMsg(msg, Weather.getWeather(msg.getText(), model));
//                            } catch (IOException e) {
//                                sendMsg(msg, "Город не найден!");
//                            }

//                    } else if (update.hasCallbackQuery()) {
//                        String str = update.getCallbackQuery().getData();
//                        SendMessage message = new SendMessage();
//                        message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
//                        message.setText(str);
//                        try {
//                            execute(message);
//                        } catch (TelegramApiException e) {
//                            e.printStackTrace();
//                        }
//                    }
                        }
                    }
                }
            }

//    public void sendMsg(Message msg, String text){
//        SendMessage message = new SendMessage();
//        message.setChatId(msg.getChatId().toString());
//        message.setText(text);
//        try {
//            execute(message);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//    }

            public void sendMsg (Message message, String text){
                SendMessage sendMessage = new SendMessage();
                sendMessage.enableMarkdown(true);
                sendMessage.setChatId(message.getChatId().toString());
                sendMessage.setText(text);
                try {
                    setButtons(sendMessage);
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

            public void setButtons (SendMessage sendMessage){
                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                replyKeyboardMarkup.setSelective(true);
                replyKeyboardMarkup.setResizeKeyboard(true);
                replyKeyboardMarkup.setOneTimeKeyboard(false);

                List<KeyboardRow> keyboardRowList = new ArrayList<>();
                KeyboardRow keyboardFirstRow = new KeyboardRow();

                keyboardFirstRow.add(new KeyboardButton("/help"));
                keyboardFirstRow.add(new KeyboardButton("/setting"));
                keyboardFirstRow.add(new KeyboardButton("/start"));

                keyboardRowList.add(keyboardFirstRow);
                replyKeyboardMarkup.setKeyboard(keyboardRowList);

            }

            @Override
            public String getBotUsername () {
                return "CourseworkBot";
            }

            @Override
            public String getBotToken () {
                return "5596068203:";
            }

        }
