import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;



public class Bot extends TelegramLongPollingBot {
    public static void main(String[] args) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
            Message msg = update.getMessage();
            if (update.hasMessage() && update.getMessage().hasText()) {
                switch (msg.getText()){
                    case "/help":
                    sendMsg(msg, "Чем могу помочь?");
                    break;
                case "/setting":
                    sendMsg(msg, "Что будем настраивать?");
                    break;
                default:
                }
            }
    }

    public void sendMsg(Message msg, String text){
        SendMessage message = new SendMessage();
        message.setChatId(msg.getChatId().toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "CourseworkBot";
    }

    @Override
    public String getBotToken() {
        return "5596068203:AAGY5IXew9nlQsKmRbdbS56rc5xSCk3PYok";
    }

}
