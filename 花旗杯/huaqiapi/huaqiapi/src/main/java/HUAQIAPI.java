import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author lzb
 * @date 2018/8/29 14:11
 */
public class HUAQIAPI {

    private String accessToken ;
    private String eventId ;
    private String bizToken ;
    private String modulus ;
    private String exponent;




    public HUAQIAPI() {
        try {
            accessToken = getAccessToken();
            Map<String , String> map = getBizTokenEtc(accessToken);
            eventId = map.get("eventId");
            bizToken = map.get("bizToken");
            modulus = map.get("modulus");
            exponent = map.get("exponent");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void transferTo(Account srcAcct, Account destAcct , Double transferamount){

 //       createTransfer(srcAcct, destAcct ,transferamount);
//        confirmTransfer();
    }

    private static String getAccessToken() throws IOException{
        OkHttpClient client = new OkHttpClient();
        String client_id = APIConstant.CLIENT_ID;
        String client_scrent = APIConstant.CLIENT_SCRENT;
        String encode_key = client_id + ":" + client_scrent;
        String authorization = "Basic " + Base64.encodeBase64String(encode_key.getBytes());
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&scope=/api");
        Request request = new Request.Builder()
                .url("https://sandbox.apihub.citi.com/gcb/api/clientCredentials/oauth2/token/hk/gcb")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("authorization", authorization)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();
        Response response = client.newCall(request).execute();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(response.body().string());
        String accessToken = (String) jsonObject.get("access_token");
        System.out.println("step1 access_token:");
        System.out.println("\t" + accessToken);
        return accessToken;
    }

    private static Map getBizTokenEtc(String accessToken) throws IOException{
        Map<String, String> map = new HashMap<>();
        OkHttpClient client = new OkHttpClient();
        String client_id = APIConstant.CLIENT_ID;
        String authorization = "Bearer " + accessToken;
        UUID uuid = UUID.randomUUID();
        Request request = new Request.Builder()
                .url("https://sandbox.apihub.citi.com/gcb/api/security/e2eKey")
                .get()
                .addHeader("authorization", authorization)
                .addHeader("client_id", client_id)
                .addHeader("uuid", uuid.toString())
                .addHeader("content-type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(response.body().string());
        String modulus = null;
        String exponent = null;
        String bizToken = null;
        String eventId = null;
        if (jsonObject != null) {
            modulus = (String) jsonObject.get("modulus");
            exponent = (String) jsonObject.get("exponent");
            Headers headers = response.headers();
            bizToken = headers.get("bizToken");
            eventId = headers.get("eventId");
            map.put("modulus", modulus);
            map.put("exponent", exponent);
            map.put("bizToken", bizToken);
            map.put("eventId", eventId);
        }
        System.out.println("step2 map:");
        for (String s : map.keySet()) {
            System.out.println("\tkey:" + s + "\tvalues:" + map.get(s));
        }
        return map;
    }

    public String getRealAccessToken(String username , String password) throws IOException{
        String client_id = APIConstant.CLIENT_ID;
        String client_scrent = APIConstant.CLIENT_SCRENT;
        System.err.println("bizToken: "+bizToken);
        String encode_key = client_id + ":" + client_scrent;
        String authorization = "Basic " + Base64.encodeBase64String(encode_key.getBytes());
        System.out.println(password);
        UUID uuid = UUID.randomUUID();
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=password&scope=/api&username="+username+"&password="+password);
        Request request = new Request.Builder()
                .url("https://sandbox.apihub.citi.com/gcb/api/password/oauth2/token/hk/gcb")
                .post(body)
                .addHeader("authorization", authorization)
                .addHeader("bizToken", bizToken)
                .addHeader("uuid", uuid.toString())
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("accept", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(response.body().string());
        String realAccessToken = (String) jsonObject.get("access_token");
        System.out.println("step3 real_access_token:");
        System.out.println("\t" + realAccessToken);
        return realAccessToken;
    }
    public  String getAccounts(String username, String password) throws IOException {
        ;if(getRealAccessToken(username,password)==null){
            return null;
        }
        String accounts = step4GetAccounts(username,password);
        if(getRealAccessToken(username,password)==null){
            return null;
        }
        return accounts;
    }

    public String step4GetAccounts(String username, String password) throws IOException{
        String client_id = APIConstant.CLIENT_ID;
        String authorization = "Bearer " + getRealAccessToken(username , password);
        UUID uuid = UUID.randomUUID();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://sandbox.apihub.citi.com/gcb/api/v1/accounts")
                .get()
                .addHeader("authorization", authorization)
                .addHeader("uuid", uuid.toString())
                .addHeader("content-type", "application/json")
                .addHeader("accept", "application/json")
                .addHeader("client_id", client_id)
                .build();
        Response response = client.newCall(request).execute();
        String responseBodyString = response.body().string();
        System.out.println("step4 accounts:");
        System.out.println("\t"+responseBodyString);
        return responseBodyString;
    }
    private  String createTransfer(Account srcAcct, Account destAcct , Double transferamount) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String authorization = "Bearer " + this.getRealAccessToken(srcAcct.getUsername() , srcAcct.getPassword());

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"sourceAccountId\":\"3255613852316f2b4d4d796c344e38756339654972776f663745446e6d4c32486f455a4165374a476858343d\",\"transactionAmount\":1000.25,\"transferCurrencyIndicator\":\"SOURCE_ACCOUNT_CURRENCY\",\"payeeId\":\"C$0003019202$AU$XX$01000540000001\",\"chargeBearer\":\"BENEFICIARY\",\"paymentMethod\":\"GIRO\",\"fxDealReferenceNumber\":\"12345678\",\"remarks\":\"adduruga\",\"transferPurpose\":\"CASH_DISBURSEMENT\"}");
        Request request = new Request.Builder()
                .url("https://sandbox.apihub.citi.com/gcb/api/v1/moneyMovement/personalDomesticTransfers/preprocess")
                .post(body)
                .addHeader("authorization", authorization)
                .addHeader("uuid", UUID.randomUUID().toString())
                .addHeader("accept", "application/json")
                .addHeader("client_id", APIConstant.CLIENT_ID)
                .addHeader("content-type", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(response.body().string());
        String controlFlowId = (String) jsonObject.get("controlFlowId");
        return controlFlowId;
    }

    private void confirmTransfer(Account account , String controlFlowId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String authorization = "Bearer " + this.getRealAccessToken(account.getUsername() , account.getPassword());



        MediaType mediaType = MediaType.parse("application/json");
//        RequestBody body = RequestBody.create(mediaType, "{\"controlFlowId\":\"6e3774334f724a2b7947663653712f52456f524c41797038516a59347a437549564a77755676376e616a733d\"}");
        RequestBody body = RequestBody.create(mediaType, "{\"controlFlowId\":\" "+controlFlowId+"\"}");
        Request request = new Request.Builder()
                .url("https://sandbox.apihub.citi.com/gcb/api/v1/moneyMovement/personalDomesticTransfers")
                .post(body)
                .addHeader("authorization", authorization)
                .addHeader("uuid", UUID.randomUUID().toString())
                .addHeader("accept", "application/json")
                .addHeader("client_id", APIConstant.CLIENT_ID)
                .addHeader("content-type", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(response.body().string());

    }

    public static void main(String args[]) throws IOException {
        HUAQIAPI api = new HUAQIAPI();
        api.getAccounts("SandboxUser1","P@ssUser1$" );
    }

}
