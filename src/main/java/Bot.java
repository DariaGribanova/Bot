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

import java.io.IOException;
import java.util.*;


public class Bot extends TelegramLongPollingBot {
    private Map<Long, List<Question>> surveyMap = new HashMap<Long, List<Question>>();
    private Map<Long, List<Question>> answerMap = new HashMap<Long, List<Question>>();
    private Map<Long, Long> userMap = new HashMap<Long, Long>();
    private Connection connection = new Connection();

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
        System.out.println(update);
        if (update.hasMessage()) {
            Long id = update.getMessage().getFrom().getId();
            if (update.getMessage().hasText()) {
                String str = update.getMessage().getText();
                String[] strings = str.split(" ");
                switch (strings[0]) {
                    case "/start":
                        if (strings[1]!= null) {
                            try {
                                userMap.remove(id);
                                answerMap.remove(id);
                                surveyMap.put(Long.valueOf(strings[1]), connection.getQuestions(strings[1]));
                                answerMap.put(id, new ArrayList<>());
                                userMap.put(id, Long.valueOf(strings[1]));
                                List<Question> questions = surveyMap.get(Long.valueOf(strings[1]));
                                Question question = questions.get(answerMap.get(id).size());
                                if (question.getItem().equals("SingleLineText") || question.getItem().equals("MultiLineText")){
                                    sendMsg(update.getMessage().getFrom().getId(), question.getName());
                                }
                                if (question.getItem().equals("RadioButtons")){
                                    sendKeyboard(update.getMessage().getFrom().getId(), question);
                                }
                                if (question.getItem().equals("Checkboxes")){
                                    sendKeyboard(update.getMessage().getFrom().getId(), question);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        break;
                    case "/token":
                        try {
                            connection.getToken();
                            sendMsg(update.getMessage().getFrom().getId(), "Токен получен");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    default:
                        if (surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem().equals("SingleLineText") || surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem().equals("MultiLineText")){
                            //List<Answer> answerList = new ArrayList<>(answerMap.get(id));
                            List<Question> questions = new ArrayList<>(answerMap.get(id));

                            Answer answer = new Answer(update.getMessage().getText(), update.getMessage().getText());
                            Question question = new Question();
                            question.setName(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getName());
                            question.setItem(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem());
                            question.setPage(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getPage());
                            question.setId(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getId());
                            question.setCompleted(true);
                            List<Answer> answerList = new ArrayList<>(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers());
                            //List<Answer> answerList = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers();
                            answerList.add(answer);
                            question.setAnswers(answerList);
                            questions.add(question);

                            answerMap.put(id, questions);
                            if (surveyMap.get(userMap.get(id)).size() == answerMap.get(id).size()){
                                try {
                                    connection.sendResponse(userMap.get(id), answerMap.get(id));
                                    sendMsg(update.getMessage().getFrom().getId(), "Опрос пройден!");
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                Question question1 = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size());
                                if (question1.getItem().equals("SingleLineText") || question1.getItem().equals("MultiLineText")) {
                                    sendMsg(update.getMessage().getFrom().getId(), question1.getName());
                                }
                                if (question1.getItem().equals("RadioButtons")){
                                    sendKeyboard(update.getMessage().getFrom().getId(), question1);
                                }
                                if (question1.getItem().equals("Checkboxes")){
                                    sendKeyboard(update.getMessage().getFrom().getId(), question1);
                                }
                            }
                        }
                }
            }
        } else if (update.hasCallbackQuery()) {
            Long id = update.getCallbackQuery().getFrom().getId();
            String str = update.getCallbackQuery().getData();
            String[] strings = str.split("-");
            switch (strings[0]) {
                case "token":
                    try {
                        connection.getToken();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "question":
                    if (answerMap.get(id).size() == 0 && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem().equals("Checkboxes") && String.valueOf(userMap.get(id)).equals(strings[1]) && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers().contains(new Answer(strings[2], strings[3]))){
                        List<Question> questions = new ArrayList<>(answerMap.get(id));
                        Answer answer = new Answer(strings[2], strings[3]);
                        Question question = new Question();
                        question.setName(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getName());
                        question.setItem(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem());
                        question.setPage(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getPage());
                        question.setId(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getId());
                        question.setCompleted(false);
                        List<Answer> answerList = new ArrayList<>();
                        //List<Answer> answerList = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers();
                        answerList.add(answer);
                        question.setAnswers(answerList);
                        questions.add(question);
                        answerMap.put(id, questions);

                        Question question1 = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1);
                        sendKeyboard2(update.getCallbackQuery().getFrom().getId(), question1);
                    } else if (answerMap.get(id).size() != 0 && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getItem().equals("Checkboxes") && String.valueOf(userMap.get(id)).equals(strings[1]) && !answerMap.get(id).get(answerMap.get(id).size()-1).isCompleted() && strings[2].equals("next")){
                        System.out.println("Зашёл");
                        List<Question> questions = new ArrayList<>(answerMap.get(id));
                        Question question = new Question();
                        question.setName(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getName());
                        question.setItem(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getItem());
                        question.setPage(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getPage());
                        question.setId(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getId());
                        question.setCompleted(true);
                        List<Answer> answerList = answerMap.get(id).get(answerMap.get(id).size()-1).getAnswers();
                        //answerList.addAll(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getAnswers());
                        question.setAnswers(answerList);
                        questions.remove(questions.size()-1);
                        questions.add(question);
                        answerMap.put(id, questions);


                        if (surveyMap.get(userMap.get(id)).size() == answerMap.get(id).size()){
                            try {
                                connection.sendResponse(userMap.get(id), answerMap.get(id));
                                sendMsg(update.getCallbackQuery().getFrom().getId(), "Опрос пройден!");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            Question question1 = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size());
                            if (question1.getItem().equals("SingleLineText") || question1.getItem().equals("MultiLineText")) {
                                sendMsg(update.getCallbackQuery().getFrom().getId(), question1.getName());
                            }
                            if (question1.getItem().equals("RadioButtons")) {
                                sendKeyboard(update.getCallbackQuery().getFrom().getId(), question1);
                            }
                            if (question1.getItem().equals("Checkboxes")) {
                                sendKeyboard(update.getCallbackQuery().getFrom().getId(), question1);
                            }
                        }

                    } else if(answerMap.get(id).size() != 0 && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getItem().equals("Checkboxes") && !answerMap.get(id).get(answerMap.get(id).size()-1).isCompleted() && String.valueOf(userMap.get(id)).equals(strings[1]) && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getAnswers().contains(new Answer(strings[2], strings[3])) ){
                        List<Question> questions = new ArrayList<>(answerMap.get(id));
                        Answer answer = new Answer(strings[2], strings[3]);
                        Question question = new Question();
                        question.setName(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getName());
                        question.setItem(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getItem());
                        question.setPage(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getPage());
                        question.setId(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getId());
                        question.setCompleted(false);
                        List<Answer> answerList = answerMap.get(id).get(answerMap.get(id).size()-1).getAnswers();
                        //List<Answer> answerList = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers();
                        answerList.add(answer);
                        question.setAnswers(answerList);
                        questions.remove(questions.size()-1);
                        questions.add(question);
                        answerMap.put(id, questions);
                        Question question1 = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1);
                        sendKeyboard2(update.getCallbackQuery().getFrom().getId(), question1);

                    } else if(answerMap.get(id).size() != 0 && answerMap.get(id).get(answerMap.get(id).size()-1).isCompleted()) {
                       if (answerMap.get(id).size() != 0 && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem().equals("Checkboxes") && String.valueOf(userMap.get(id)).equals(strings[1]) && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers().contains(new Answer(strings[2], strings[3]))){
                            List<Question> questions = new ArrayList<>(answerMap.get(id));
                            Answer answer = new Answer(strings[2], strings[3]);
                            Question question = new Question();
                            question.setName(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getName());
                            question.setItem(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem());
                           question.setPage(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getPage());
                           question.setId(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getId());
                            question.setCompleted(false);
                            List<Answer> answerList = new ArrayList<>();
                            //List<Answer> answerList = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers();
                            answerList.add(answer);
                            question.setAnswers(answerList);
                            questions.add(question);
                            answerMap.put(id, questions);
                           System.out.println(answerMap.get(id));
                            Question question1 = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1);
                            sendKeyboard2(update.getCallbackQuery().getFrom().getId(), question1);
                        } else if (surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem().equals("RadioButtons") && String.valueOf(userMap.get(id)).equals(strings[1]) && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers().contains(new Answer(strings[2], strings[3]))) {
                            List<Question> questions = new ArrayList<>(answerMap.get(id));
                            Answer answer = new Answer(strings[2], strings[3]);
                            Question question = new Question();
                            question.setName(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getName());
                            question.setItem(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem());
                           question.setPage(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getPage());
                           question.setId(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getId());
                            question.setCompleted(true);
                            List<Answer> answerList = new ArrayList<>();
                            //List<Answer> answerList = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers();
                            answerList.add(answer);
                            question.setAnswers(answerList);
                            questions.add(question);
                            answerMap.put(id, questions);

//                        List<Answer> answerList = new ArrayList<>(answerMap.get(id));
//                        Answer answer = new Answer(strings[1], strings[2]);
//                        answerList.add(answer);
//                        System.out.println(answerList);
//                        answerMap.put(id, answerList);
                            if (surveyMap.get(userMap.get(id)).size() == answerMap.get(id).size()) {
                                try {
                                    connection.sendResponse(userMap.get(id), answerMap.get(id));
                                    sendMsg(update.getCallbackQuery().getFrom().getId(), "Опрос пройден!");
                                    System.out.println(answerMap.get(id));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                Question question1 = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size());
                                if (question1.getItem().equals("SingleLineText") || question1.getItem().equals("MultiLineText")) {
                                    sendMsg(update.getCallbackQuery().getFrom().getId(), question1.getName());
                                }
                                if (question1.getItem().equals("RadioButtons")) {
                                    sendKeyboard(update.getCallbackQuery().getFrom().getId(), question1);
                                }
                                if (question1.getItem().equals("Checkboxes")) {
                                    sendKeyboard(update.getCallbackQuery().getFrom().getId(), question1);
                                }
                            }
                        }
                    } else if (surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem().equals("RadioButtons") && String.valueOf(userMap.get(id)).equals(strings[1]) && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers().contains(new Answer(strings[2], strings[3]))){
                        List<Question> questions = new ArrayList<>(answerMap.get(id));
                        Answer answer = new Answer(strings[2], strings[3]);
                        Question question = new Question();
                        question.setName(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getName());
                        question.setItem(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem());
                        question.setPage(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getPage());
                        question.setId(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getId());
                        question.setCompleted(true);
                        List<Answer> answerList = new ArrayList<>();
                        //List<Answer> answerList = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers();
                        answerList.add(answer);
                        question.setAnswers(answerList);
                        questions.add(question);
                        answerMap.put(id, questions);

//                        List<Answer> answerList = new ArrayList<>(answerMap.get(id));
//                        Answer answer = new Answer(strings[1], strings[2]);
//                        answerList.add(answer);
//                        System.out.println(answerList);
//                        answerMap.put(id, answerList);
                        if (surveyMap.get(userMap.get(id)).size() == answerMap.get(id).size()) {
                            try {
                                connection.sendResponse(userMap.get(id), answerMap.get(id));
                                sendMsg(update.getCallbackQuery().getFrom().getId(), "Опрос пройден!");
                                System.out.println(answerMap.get(id));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            Question question1 = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size());
                            if (question1.getItem().equals("SingleLineText") || question1.getItem().equals("MultiLineText")) {
                                sendMsg(update.getCallbackQuery().getFrom().getId(), question1.getName());
                            }
                            if (question1.getItem().equals("RadioButtons")) {
                                sendKeyboard(update.getCallbackQuery().getFrom().getId(), question1);
                            }
                            if (question1.getItem().equals("Checkboxes")) {
                                sendKeyboard(update.getCallbackQuery().getFrom().getId(), question1);
                            }
                        }
                    }
                    break;
                default:
            }
        }
    }


    public void sendKeyboard(Long id, Question question) {
        SendMessage sendMessage = new SendMessage();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (Answer answer: question.getAnswers()) {
            List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(answer.getName());
            inlineKeyboardButton.setCallbackData("question-"+userMap.get(id)+"-"+answer.getName()+"-"+answer.getId());
            keyboardButtons.add(inlineKeyboardButton);
            rowList.add(keyboardButtons);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setChatId(id);
        sendMessage.setText(question.getName());
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendKeyboard2(Long id, Question question) {
        question.getAnswers().removeAll(answerMap.get(id).get(answerMap.get(id).size()-1).getAnswers());
        SendMessage sendMessage = new SendMessage();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (Answer answer: question.getAnswers()) {
            List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(answer.getName());
            inlineKeyboardButton.setCallbackData("question-"+userMap.get(id)+"-"+answer.getName()+"-"+answer.getId());
            keyboardButtons.add(inlineKeyboardButton);
            rowList.add(keyboardButtons);
        }
        List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Следующий вопрос");
        inlineKeyboardButton.setCallbackData("question-"+userMap.get(id)+"-next");
        keyboardButtons.add(inlineKeyboardButton);
        rowList.add(keyboardButtons);
        inlineKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setChatId(id);
        sendMessage.setText(question.getName());
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(Long id, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(id);
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void setButtons(SendMessage sendMessage) {
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
    public String getBotUsername() {
        return "CourseworkBot";
    }

    @Override
    public String getBotToken() {
        return "5596068203:AAGY5IXew9nlQsKmRbdbS56rc5xSCk3PYok";
    }

}
