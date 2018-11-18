import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;

public class Authorize {
    // OkHttpClient from http://square.github.io/okhttp/
    public void run() throws IOException {
        OkHttpClient client = new OkHttpClient();

//        Request request = new Request.Builder()
//                .url("https://sandbox.apihub.citi.com/gcb/api/authCode/oauth2/authorize?response_type=" + "code" + "&client_id=" + "2f3c5af8-34c5-4751-b1dd-9be322aee7c9" + "&scope=" + "accounts_details_transactions" + "&countryCode= " + "US" + "&businessCode=" + "GCB" + "&locale=" + "en_US" + "&state=" + "12093" + "&redirect_uri=" + "https://www.baidu.com/")
//                .get()
//                .addHeader("accept", "application/json")
//                .build();

        Request request = new Request.Builder()
                .url("https://sandbox.apihub.citi.com/gcb/api/authCode/oauth2/authorize?response_type=" + "code" + "&client_id=" + "308a6cf6-22df-4d11-94c5-43eea3dc524a" + "&scope=" + "accounts_details_transactions" + "&countryCode= " + "US" + "&businessCode=" + "GCB" + "&locale=" + "en_US" + "&state=" + "12093" + "&redirect_uri=" + "https://www.duba.com/?f=dbsem&hid=10_166_17_20839_&ty4=0&tryno=1335&pru=1")
                .get()
                .addHeader("accept", "application/json")
                .build();

        Response response = client.newCall(request).execute();

        JSONObject jsonObject = (JSONObject) JSONValue.parse(response.body().string());
        System.out.println(jsonObject);



    }

  public static void main(String args[]) throws IOException {
        Authorize api = new Authorize();
        api.run();
  }
}
