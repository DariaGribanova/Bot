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
    private String TOKEN = "eyJhbGciOiJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNobWFjLXNoYTI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoiZGF3YU9wZW5BSUBnbWFpbC5jb20iLCJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1laWRlbnRpZmllciI6ImRhd2FPcGVuQUlAZ21haWwuY29tIiwiaHR0cDovL3NjaGVtYXMubWljcm9zb2Z0LmNvbS93cy8yMDA4LzA2L2lkZW50aXR5L2NsYWltcy9yb2xlIjoiU3lzdGVtIEFkbWluaXN0cmF0b3IiLCJodHRwOi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL2lkZW50aXR5L2NsYWltcy90ZW5hbnRpZCI6ImNyczQiLCJuYmYiOjE2ODMyMzIzNTAsImV4cCI6MTY4MzI3NTU1MCwiaXNzIjoiYXBpLmNoZWNrYm94LmNvbSIsImF1ZCI6IkFwaSJ9.X3y08zYZJyYc9lverBs8taj4X6ktKp-Dpm1q4xN1k2M";
    private String NAME = "dawaOpenAI@gmail.com";
    private String PASSWORD = "@cnu@Z2dPTURZaA";
    private String MYURL = "https://api.checkbox.com/v1/crs4/";
    private Map<Long, List<Integer>> pagesMap = new HashMap<Long, List<Integer>>();

    public String getToken() throws IOException {
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
        System.out.println("POST Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
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
        return null;
    }

    public void sendResponse(Long surveyId, List<Question> questions) throws IOException {
        System.out.println(questions);
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
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        int responseCode = myURLConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String res = String.valueOf(response);
            System.out.println(response);
            JSONObject object = new JSONObject(res);
            id = String.valueOf(object.get("id"));
            System.out.println(id);

        }

            for (Integer page : pagesMap.get(surveyId)) {
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
                //String jsonInputString2 = "{\"page_id\":1173,\"action\":\"MoveForward\",\"items\":[{\"item_id\":1894,\"answer\":{\"answer_type\":\"Text\",\"text\":\"1357\",\"is_required\":false,\"is_soft_required\":false}}]}";

                try (OutputStream os = myURLConnection2.getOutputStream()) {
                    byte[] input = str.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }


                int responseCode2 = myURLConnection2.getResponseCode();

                if (responseCode2 == HttpURLConnection.HTTP_OK) { //success
                    BufferedReader in2 = new BufferedReader(new InputStreamReader(myURLConnection2.getInputStream()));
                    String inputLine2;
                    StringBuffer response2 = new StringBuffer();
                    while ((inputLine2 = in2.readLine()) != null) {
                        response2.append(inputLine2);
                    }
                    in2.close();

                    System.out.println(response2);

                } else {
                    System.out.println("POST request did not work.");
                }
            }
            System.out.println("Отправили");
        }

        private String singleLineText(Question question){
            System.out.println(question.getAnswers().get(0).getName());
        String str = "{\n" +
                "\"item_id\":"+question.getId()+",\n" +
                "\"answer\":{\n" +
                "\"answer_type\":\"Text\",\n" +
                "\"text\":\""+question.getAnswers().get(0).getName()+"\",\n" +
                "\"is_required\":false,\n" +
                "\"is_soft_required\":false\n" +
                "}}";
        return str;
        }
    private String radioButtons(Question question){
        System.out.println(question.getAnswers().get(0).getName());
        String str = "{\"item_id\":"+question.getId()+",\n" +
                "\"answer\":{\n" +
                "\"answer_type\":\"SingleChoice\",\n" +
                "\"choice_id\":"+question.getAnswers().get(0).getId()+",\n" +
                "\"text\":null,\n" +
                "\"is_required\":false,\n" +
                "\"is_soft_required\":false\n" +
                "}\n" +
                "}";
        return str;
    }
    private String checkboxes(Question question){
        System.out.println(question.getAnswers().get(0).getName());
        StringBuilder stringBuilder = new StringBuilder("{\n" +
                "\"item_id\":"+question.getId()+",\n" +
                "\"answer\":{\n" +
                "\"answer_type\":\"MultipleChoice\",\n" +
                "\"is_soft_required\":false,\n" +
                "\"choices\":[\n");
        for (Answer answer: question.getAnswers()){
            stringBuilder.append("{\n" +
                    "\"choice_id\":"+answer.getId()+",\n" +
                    "\"text\":\""+answer.getName()+"\"\n" +
                    "},");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.append("]\n" +
                "}\n" +
                "}");
        return String.valueOf(stringBuilder);
    }




//    public List<Survey> getSurveys() throws IOException {
//        URL myURL = new URL(MYURL + "surveys");
//        HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();
//
//        String basicAuth = "Bearer " + new String(TOKEN);
//        myURLConnection.setRequestProperty("Accept", "application/json");
//        myURLConnection.setRequestProperty("Authorization", basicAuth);
//        myURLConnection.setRequestMethod("GET");
//        myURLConnection.setUseCaches(false);
//        myURLConnection.setDoInput(true);
//        myURLConnection.setDoOutput(true);
//
//        List<Survey> surveyList = new ArrayList<>();
//        try (BufferedReader br = new BufferedReader(
//                new InputStreamReader(myURLConnection.getInputStream(), "utf-8"))) {
//            StringBuilder response = new StringBuilder();
//            String responseLine = null;
//            while ((responseLine = br.readLine()) != null) {
//                response.append(responseLine.trim());
//            }
//            System.out.println(response.toString());
//            String res = String.valueOf(response);
//            JSONObject object = new JSONObject(res);
//            JSONArray name = object.getJSONArray("items");
//            Iterator<Object> phonesItr = name.iterator();
//            System.out.println("Опросы: ");
//            while (phonesItr.hasNext()) {
//                JSONObject test = (JSONObject) phonesItr.next();
//                Survey survey = new Survey();
//                survey.setName((String) test.get("name"));
//                survey.setId((Integer) test.get("id"));
//                surveyList.add(survey);
//                System.out.println(survey.getName());
//                System.out.println(survey.getId());
//            }
//
//        }
//        return surveyList;
//    }


    public List<Question> getQuestions(String str) throws IOException {
        URL myURL = new URL(MYURL + "surveys/" + str + "/pages");
        HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();

        String basicAuth = "Bearer " + new String(TOKEN);
        myURLConnection.setRequestProperty("Accept", "application/json");
        myURLConnection.setRequestProperty("Authorization", basicAuth);
        myURLConnection.setRequestMethod("GET");
        myURLConnection.setUseCaches(false);
        myURLConnection.setDoInput(true);
        myURLConnection.setDoOutput(true);
        List<Question> questionList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(myURLConnection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            JSONArray array = new JSONArray(response.toString());
            Iterator<Object> pgItr = array.iterator();
            List<Integer> idList = new ArrayList<>();
            while (pgItr.hasNext()) {
                JSONObject test = (JSONObject) pgItr.next();
                if (test.get("page_type").equals("ContentPage")) {
                    idList.add((Integer) test.get("id"));
                }
            }
            pagesMap.put(Long.valueOf(str), idList);
            for (Integer id : idList) {
                myURL = new URL(MYURL + "surveys/" + str + "/pages/" + id + "/items");
                myURLConnection = (HttpURLConnection) myURL.openConnection();
                basicAuth = "Bearer " + new String(TOKEN);
                myURLConnection.setRequestProperty("Accept", "application/json");
                myURLConnection.setRequestProperty("Authorization", basicAuth);
                myURLConnection.setRequestMethod("GET");
                myURLConnection.setUseCaches(false);
                myURLConnection.setDoInput(true);
                myURLConnection.setDoOutput(true);

                try (BufferedReader br1 = new BufferedReader(
                        new InputStreamReader(myURLConnection.getInputStream(), "utf-8"))) {
                    StringBuilder response1 = new StringBuilder();
                    String responseLine1 = null;
                    while ((responseLine1 = br1.readLine()) != null) {
                        response1.append(responseLine1.trim());
                    }
                    JSONArray array1 = new JSONArray(response1.toString());
                    Iterator<Object> pgItr1 = array1.iterator();
                    while (pgItr1.hasNext()) {
                        JSONObject test = (JSONObject) pgItr1.next();
                        Question question = new Question();
                        String string = (String) test.get("question_text");
                        String substr = string.substring(3, string.length()-4);
                        question.setName(substr);
                        question.setItem((String) test.get("item_type"));
                        question.setCompleted(false);
                        question.setPage(String.valueOf(id));
                        question.setId(String.valueOf(test.get("id")));
                        if (!test.get("item_type").equals("SingleLineText") && !test.get("item_type").equals("MultiLineText") ) {
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
        }
        return questionList;
    }
}