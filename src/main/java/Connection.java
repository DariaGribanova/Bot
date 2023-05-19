import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.UUID;

public class Connection {
    private String TOKEN = "";
    private final String NAME = "dawagrib5@gmail.com";
    private final String PASSWORD = "xfnz@6QVMGM9YMY";
    private final String ACCNAME = "crs5";
    private final String MYURL = "https://api.checkbox.com/v1/"+ACCNAME+"/";

    public void getToken() throws IOException {
        URL myURL = new URL(MYURL+ "oauth2/token");
        HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();
        myURLConnection.setRequestMethod("POST");
        myURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String s1 = "username=" + NAME + "&password=" + PASSWORD + "&grant_type=password";
        myURLConnection.setDoOutput(true);
        OutputStream os = myURLConnection.getOutputStream();
        os.write(s1.getBytes());
        os.flush();
        os.close();
        int responseCode = myURLConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String res = String.valueOf(response);
            JSONObject object = new JSONObject(res);
            this.TOKEN = object.getString("access_token");
            System.out.println(object.getString("access_token"));
        } else {
            System.out.println("POST request did not work.");
        }
    }

    public void sendResponse(Long surveyId, List<Question> questions) throws IOException {
        UUID uniqueKey = UUID.randomUUID();
        String id = "";
        URL myURL = new URL(MYURL + "surveys/"+surveyId+"/responses");
        HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();
        myURLConnection.setRequestMethod("POST");
        myURLConnection.setRequestProperty("Content-Type", "application/json");
        myURLConnection.setRequestProperty("Accept", "application/json");
        myURLConnection.setDoOutput(true);
        String jsonInputString = "{\"language\":\"en-US\",\"is_test\":false,\"hidden_items\":[],\n" +
                "\"anonymous_respondent_id\":\""+uniqueKey+"\"}";
        try (OutputStream os = myURLConnection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = myURLConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String res = String.valueOf(response);
            JSONObject object = new JSONObject(res);
            id = String.valueOf(object.get("id"));
        }
            List<Integer> pageList = getPages(String.valueOf(surveyId));
            for (Integer page : pageList) {
                URL myURL2 = new URL(MYURL + "surveys/" + surveyId + "/responses/" + id + "/current-page");
                HttpURLConnection myURLConnection2 = (HttpURLConnection) myURL2.openConnection();
                myURLConnection2.setRequestMethod("POST");
                myURLConnection2.setRequestProperty("Content-Type", "application/json");
                myURLConnection2.setRequestProperty("Accept", "application/json");
                myURLConnection2.setDoOutput(true);
                StringBuilder jsonInputString2 = new StringBuilder("{\"page_id\":"+page+",\"action\":\"MoveForward\",\"items\":[");
                    for (Question question: questions){
                        if(question.getPage().equals(String.valueOf(page))){
                                if(question.getItem().equals("SingleLineText") || question.getItem().equals("MultiLineText")){
                                    jsonInputString2.append(singleLineText(question));
                                }
                            if(question.getItem().equals("RadioButtons")){
                                jsonInputString2.append(radioButtons(question));
                            }
                            if(question.getItem().equals("Checkboxes")){
                                jsonInputString2.append(checkboxes(question));
                            }
                            jsonInputString2.append(",");
                        }
                    }
                    jsonInputString2.deleteCharAt(jsonInputString2.length()-1);
                jsonInputString2.append("]}");
                String str = String.valueOf(jsonInputString2);
                try (OutputStream os = myURLConnection2.getOutputStream()) {
                    byte[] input = str.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                myURLConnection2.getResponseCode();
            }
        }

        private String singleLineText(Question question){
            return "{\n" +
                    "\"item_id\":"+question.getId()+",\n" +
                    "\"answer\":{\n" +
                    "\"answer_type\":\"Text\",\n" +
                    "\"text\":\""+question.getAnswers().get(0).getName()+"\",\n" +
                    "\"is_required\":false,\n" +
                    "\"is_soft_required\":false\n" +
                    "}}";
        }
    private String radioButtons(Question question){
        return "{\"item_id\":"+question.getId()+",\n" +
                "\"answer\":{\n" +
                "\"answer_type\":\"SingleChoice\",\n" +
                "\"choice_id\":"+question.getAnswers().get(0).getId()+",\n" +
                "\"text\":null,\n" +
                "\"is_required\":false,\n" +
                "\"is_soft_required\":false\n" +
                "}\n" +
                "}";
    }
    private String checkboxes(Question question){
        StringBuilder stringBuilder = new StringBuilder("{\n" +
                "\"item_id\":"+question.getId()+",\n" +
                "\"answer\":{\n" +
                "\"answer_type\":\"MultipleChoice\",\n" +
                "\"is_soft_required\":false,\n" +
                "\"choices\":[\n");
        for (Answer answer: question.getAnswers()){
            stringBuilder.append("{\n" + "\"choice_id\":").append(answer.getId()).append(",\n")
                    .append("\"text\":\"").append(answer.getName()).append("\"\n").append("},");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.append("""
                ]
                }
                }""");
        return String.valueOf(stringBuilder);
    }


    public List<Question> getQuestions(String str) throws IOException {
            List<Question> questionList = new ArrayList<>();
            List<Integer> pageList = getPages(String.valueOf(str));
            for (Integer id : pageList) {
                URL myURL = new URL(MYURL + "surveys/" + str + "/pages/" + id + "/items");
                HttpURLConnection myURLConnection = createHttpURLConnection(myURL);
                try (BufferedReader br1 = new BufferedReader(
                        new InputStreamReader(myURLConnection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response1 = new StringBuilder();
                    String responseLine1;
                    while ((responseLine1 = br1.readLine()) != null) {
                        response1.append(responseLine1.trim());
                    }
                    JSONArray array1 = new JSONArray(response1.toString());
                    for (Object o : array1) {
                        JSONObject test = (JSONObject) o;
                        Question question = new Question();
                        String string = (String) test.get("question_text");
                        String substr = string.substring(3, string.length() - 4);
                        question.setName(substr);
                        question.setItem((String) test.get("item_type"));
                        question.setCompleted(false);
                        question.setPage(String.valueOf(id));
                        question.setId(String.valueOf(test.get("id")));
                        if (!test.get("item_type").equals("SingleLineText") && !test.get("item_type").equals("MultiLineText")) {
                            JSONArray choices = test.getJSONArray("choices");
                            Iterator<Object> chItr = choices.iterator();
                            List<Answer> answerList = new ArrayList<>();
                            while (chItr.hasNext()) {
                                JSONObject test1 = (JSONObject) chItr.next();
                                Answer answer = new Answer();
                                answer.setId(String.valueOf(test1.get("id")));
                                answer.setName((String) test1.get("text"));
                                answerList.add(answer);
                            }
                            question.setAnswers(answerList);
                        }
                        questionList.add(question);
                    }
                }
            }
        return questionList;
    }
    public List<Integer> getPages(String str) throws IOException {
        URL myURL = new URL(MYURL + "surveys/" + str + "/pages");
        HttpURLConnection myURLConnection = createHttpURLConnection(myURL);
        List<Integer> pagesList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(myURLConnection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            JSONArray array = new JSONArray(response.toString());
            for (Object o : array) {
                JSONObject test = (JSONObject) o;
                if (test.get("page_type").equals("ContentPage")) {
                    pagesList.add((Integer) test.get("id"));
                }
            }
        }
        return pagesList;
    }
    private HttpURLConnection createHttpURLConnection (URL myURL) throws IOException {
        HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();
        String basicAuth = "Bearer " + TOKEN;
        myURLConnection.setRequestProperty("Accept", "application/json");
        myURLConnection.setRequestProperty("Authorization", basicAuth);
        myURLConnection.setRequestMethod("GET");
        myURLConnection.setUseCaches(false);
        myURLConnection.setDoInput(true);
        myURLConnection.setDoOutput(true);
        return myURLConnection;
    }
}