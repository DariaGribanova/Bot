import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.util.*;

public class Bot extends TelegramLongPollingBot {
    private final Map<Long, List<Question>> surveyMap = new HashMap<>();
    private final Map<Long, List<Question>> answerMap = new HashMap<>();
    private final Map<Long, Long> userMap = new HashMap<>();
    private static final Connection connection = new Connection();
    public static void main(String[] args) throws IOException {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new Bot());
            connection.getToken();
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

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Long id = update.getMessage().getFrom().getId();
            if (update.getMessage().hasText()) {
                String str = update.getMessage().getText();
                String[] strings = str.split(" ");
                switch (strings[0]) {
                    case "/start":
                        if (strings[1]!= null) {
                            try {
                                surveyMap.put(Long.valueOf(strings[1]), connection.getQuestions(strings[1]));
                                answerMap.put(id, new ArrayList<>());
                                userMap.put(id, Long.valueOf(strings[1]));
                                List<Question> questions = surveyMap.get(Long.valueOf(strings[1]));
                                sendNextQuestion(questions.get(answerMap.get(id).size()), id);
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
                        if (surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem().equals("SingleLineText") ||
                                surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem().equals("MultiLineText")){
                            List<Question> questions = new ArrayList<>(answerMap.get(id));
                            Answer answer = new Answer(update.getMessage().getText(), update.getMessage().getText());
                            Question question = createQuestion(id, answerMap.get(id).size(), true);
                            List<Answer> answerList = new ArrayList<>(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers());
                            answerList.add(answer);
                            question.setAnswers(answerList);
                            questions.add(question);
                            answerMap.put(id, questions);
                            nextQuestion(id);
                        }
                }
            }
        } else if (update.hasCallbackQuery()) {
            Long id = update.getCallbackQuery().getFrom().getId();
            String str = update.getCallbackQuery().getData();
            String[] strings = str.split("-");
            switch (strings[0]) {
                case "question":
                    if (answerMap.get(id).size() == 0 && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem().equals("Checkboxes") && String.valueOf(userMap.get(id)).equals(strings[1]) && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers().contains(new Answer(strings[2], strings[3]))){
                        List<Question> questions = new ArrayList<>(answerMap.get(id));
                        Answer answer = new Answer(strings[2], strings[3]);
                        Question question = createQuestion(id, answerMap.get(id).size(), false);
                        List<Answer> answerList = new ArrayList<>();
                        answerList.add(answer);
                        question.setAnswers(answerList);
                        questions.add(question);
                        answerMap.put(id, questions);
                        Question question1 = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1);
                        sendKeyboard2(update.getCallbackQuery().getFrom().getId(), question1);
                    } else if (answerMap.get(id).size() != 0 && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getItem().equals("Checkboxes") && String.valueOf(userMap.get(id)).equals(strings[1]) && !answerMap.get(id).get(answerMap.get(id).size()-1).isCompleted() && strings[2].equals("next")){
                        List<Question> questions = new ArrayList<>(answerMap.get(id));
                        Question question = createQuestion(id, answerMap.get(id).size()-1, true);
                        List<Answer> answerList = answerMap.get(id).get(answerMap.get(id).size()-1).getAnswers();
                        question.setAnswers(answerList);
                        questions.remove(questions.size()-1);
                        questions.add(question);
                        answerMap.put(id, questions);
                        nextQuestion(id);
                    } else if(answerMap.get(id).size() != 0 && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getItem().equals("Checkboxes") && !answerMap.get(id).get(answerMap.get(id).size()-1).isCompleted() && String.valueOf(userMap.get(id)).equals(strings[1]) && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1).getAnswers().contains(new Answer(strings[2], strings[3])) ){
                        List<Question> questions = new ArrayList<>(answerMap.get(id));
                        Answer answer = new Answer(strings[2], strings[3]);
                        Question question = createQuestion(id, answerMap.get(id).size()-1, false);
                        List<Answer> answerList = answerMap.get(id).get(answerMap.get(id).size()-1).getAnswers();
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
                           Question question = createQuestion(id, answerMap.get(id).size(), false);
                            List<Answer> answerList = new ArrayList<>();
                            answerList.add(answer);
                            question.setAnswers(answerList);
                            questions.add(question);
                            answerMap.put(id, questions);
                            Question question1 = surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()-1);
                            sendKeyboard2(update.getCallbackQuery().getFrom().getId(), question1);
                        } else if (surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem().equals("RadioButtons") && String.valueOf(userMap.get(id)).equals(strings[1]) && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers().contains(new Answer(strings[2], strings[3]))) {
                            List<Question> questions = new ArrayList<>(answerMap.get(id));
                            Answer answer = new Answer(strings[2], strings[3]);
                           Question question = createQuestion(id, answerMap.get(id).size(), true);
                            List<Answer> answerList = new ArrayList<>();
                            answerList.add(answer);
                            question.setAnswers(answerList);
                            questions.add(question);
                            answerMap.put(id, questions);
                           nextQuestion(id);
                        }
                    } else if (surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getItem().equals("RadioButtons") && String.valueOf(userMap.get(id)).equals(strings[1]) && surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()).getAnswers().contains(new Answer(strings[2], strings[3]))){
                        List<Question> questions = new ArrayList<>(answerMap.get(id));
                        Answer answer = new Answer(strings[2], strings[3]);
                        Question question = createQuestion(id, answerMap.get(id).size(), true);
                        List<Answer> answerList = new ArrayList<>();
                        answerList.add(answer);
                        question.setAnswers(answerList);
                        questions.add(question);
                        answerMap.put(id, questions);
                        nextQuestion(id);
                    }
                    break;
                default:
            }
        }
    }


    private void sendKeyboard(Long id, Question question) {
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

    private void sendKeyboard2(Long id, Question question) {
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

    private void sendMsg(Long id, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(id);
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void sendNextQuestion(Question question, Long id) {
        if (question.getItem().equals("SingleLineText") || question.getItem().equals("MultiLineText")) {
            sendMsg(id, question.getName());
        }
        if (question.getItem().equals("RadioButtons")) {
            sendKeyboard(id, question);
        }
        if (question.getItem().equals("Checkboxes")) {
            sendKeyboard(id, question);
        }
    }
    private void nextQuestion(Long id) {
        if (surveyMap.get(userMap.get(id)).size() == answerMap.get(id).size()) {
            try {
                connection.sendResponse(userMap.get(id), answerMap.get(id));
                sendMsg(id, "Опрос пройден!");
                userMap.remove(id);
                answerMap.remove(id);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            sendNextQuestion(surveyMap.get(userMap.get(id)).get(answerMap.get(id).size()), id);
        }
    }
    private Question createQuestion(Long id, int size, boolean completed) {
        Question question = new Question();
        question.setName(surveyMap.get(userMap.get(id)).get(size).getName());
        question.setItem(surveyMap.get(userMap.get(id)).get(size).getItem());
        question.setPage(surveyMap.get(userMap.get(id)).get(size).getPage());
        question.setId(surveyMap.get(userMap.get(id)).get(size).getId());
        question.setCompleted(completed);
        return question;
    }

}
