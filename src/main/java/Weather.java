import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class Weather {

    public static String getWeather(String message, Model model) throws IOException {
        URL myURL = new URL("https://api.ckbxeu.com/v1/crs/surveys");
        HttpURLConnection myURLConnection = (HttpURLConnection)myURL.openConnection();

        String basicAuth = "Bearer " + new String("eyJhbGciOiJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNobWFjLXNoYTI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoiZGFzaGljaGdyQGdtYWlsLmNvbSIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL25hbWVpZGVudGlmaWVyIjoiZGFzaGljaGdyQGdtYWlsLmNvbSIsImh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vd3MvMjAwOC8wNi9pZGVudGl0eS9jbGFpbXMvcm9sZSI6IlN5c3RlbSBBZG1pbmlzdHJhdG9yIiwiaHR0cDovL3NjaGVtYXMubWljcm9zb2Z0LmNvbS9pZGVudGl0eS9jbGFpbXMvdGVuYW50aWQiOiJjcnMiLCJuYmYiOjE2NzY5Nzk1MTEsImV4cCI6MTY3NzAyMjcxMSwiaXNzIjoiYXBpLmNoZWNrYm94LmNvbSIsImF1ZCI6IkFwaSJ9.iounwNnhDd2OcpRo2OwA42R5cbHdBM5lr1Yi7Jzj2ug");
        myURLConnection.setRequestProperty("Accept", "application/json");
        myURLConnection.setRequestProperty ("Authorization", basicAuth);
        myURLConnection.setRequestMethod("GET");
   //     myURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//        myURLConnection.setRequestProperty("Content-Length", "" + postData.getBytes().length);
//        myURLConnection.setRequestProperty("Content-Language", "en-US");
        myURLConnection.setUseCaches(false);
        myURLConnection.setDoInput(true);
        myURLConnection.setDoOutput(true);
  //      BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
//        String output;

//        StringBuffer response = new StringBuffer();
//
//
//        while ((output = in.readLine()) != null) {
//            System.out.println("Response:-" + output.toString());
//            ////you will get output in "output.toString()" ,Use it however you like
//        }
//        in.close();
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(myURLConnection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
            String res = String.valueOf(response);
            JSONObject object = new JSONObject(res);
            JSONArray name = object.getJSONArray("items");
            Iterator<Object> phonesItr = name.iterator();
            System.out.println("Опросы: ");
// Выводим в цикле данные массива
            while (phonesItr.hasNext()) {
                JSONObject test = (JSONObject) phonesItr.next();
                System.out.println(test.get("name"));
            }
         //   name.getString("name");
          //  System.out.println(name);
        }

            /************** For getting response from HTTP URL end ***************/

        //URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + message + "&units=metric&appid=8f4f47f0de36155cbb79dd619a280eb3");
//        URL url = new URL("https://api.checkbox.com/v1/csf/licensing/limits&appid=51ae54da-126b-4737-a0f3-991eec0e51fc");
//        Scanner in = new Scanner((InputStream) url.getContent());
//        String result = "";
//        while (in.hasNext()) {
//            result += in.nextLine();
//        }
//
//        JSONObject object = new JSONObject(result);
//        model.setName(object.getString("limit_type"));

//        JSONObject main = object.getJSONObject("main");
//        model.setTemp(main.getDouble("temp"));


        return null;
    }
}