import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;



import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
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

    private  void  retrieveDestacc(Account dest) throws IOException {

        String authorization = "Bearer " + this.getRealAccessToken(dest.getUsername() , dest.getPassword());
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://sandbox.apihub.citi.com/gcb/api/v1/moneyMovement/payees/sourceAccounts?paymentType="+"INTERNAL_DOMESTIC")
                .get()
                .addHeader("authorization", authorization)
                .addHeader("uuid", UUID.randomUUID().toString())
                .addHeader("accept", "application/json")
                .addHeader("client_id", APIConstant.CLIENT_ID)
                .build();

        Response response = client.newCall(request).execute();
        String responseBodyString = response.body().string();
        System.out.println("retrieveDestacc "+responseBodyString);
    }
    public ArrayList<String> getPayeeIds(Account dest) throws IOException {
         ArrayList<String> payeeIds=new ArrayList<String>();
         String responseString=retrievePayeeList(dest);
         net.sf.json.JSONObject outer=net.sf.json.JSONObject.fromObject(responseString);
         if(outer.has("payeeList")){
             net.sf.json.JSONArray payeeList=outer.getJSONArray("payeeList");
             for(int i=0;i<payeeList.size();i++){
                 String s=payeeList.getString(i);
                 net.sf.json.JSONObject payee=net.sf.json.JSONObject.fromObject(s);
                 if(payee.has("payeeId")){
                     String payeeId=payee.getString("payeeId");
                     System.out.println(payeeId);
                     payeeIds.add(payeeId);
                 }
             }
         }
         return payeeIds;
    }
    private  String retrievePayeeList(Account dest) throws IOException {
        String authorization = "Bearer " + this.getRealAccessToken(dest.getUsername() , dest.getPassword());

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://sandbox.apihub.citi.com/gcb/api/v1/moneyMovement/payees")    //后面半部分都被截取了（可选参数）：?paymentType=REPLACE_THIS_VALUE&nextStartIndex=REPLACE_THIS_VALUE
                .get()
                .addHeader("authorization", authorization)
                .addHeader("uuid", UUID.randomUUID().toString())
                .addHeader("accept", "application/json")
                .addHeader("client_id", APIConstant.CLIENT_ID)
                .build();

        Response response = client.newCall(request).execute();
        String responseBodyString = response.body().string();
        System.out.println("retrievePayeelist "+responseBodyString);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"sourceAccountId\":\"3739334c4d3463614356474f6d7650667a737656664652677747796855646c5552745a43346d37423653553d\",\"transactionAmount\":4500.25,\"transferCurrencyIndicator\":\"SOURCE_ACCOUNT_CURRENCY\",\"payeeId\":\"7977557255484c7345546c4e53424766634b6c53756841672b556857626e395253334b70416449676b42673d\",\"chargeBearer\":\"BENEFICIARY\",\"paymentMethod\":\"GIRO\",\"fxDealReferenceNumber\":\"12345678\",\"remarks\":\"muzekeh\",\"transferPurpose\":\"CASH_DISBURSEMENT\"}");

        return responseBodyString;
    }
//    private String createTransfer(Account srcAcct, Double transferamount,String srcAcctId, String destAcctId) throws IOException {
//
//
//        OkHttpClient client = new OkHttpClient();
//        String authorization = "Bearer " + this.getRealAccessToken(srcAcct.getUsername() , srcAcct.getPassword());
//        MediaType mediaType = MediaType.parse("application/json");
//        RequestBody body = RequestBody.create(mediaType, "{\"sourceAccountId\":\"355a515030616a53576b6a65797359506a634175764a734a3238314e4668627349486a676f7449463949453d\",\"transactionAmount\":4500.25,\"transferCurrencyIndicator\":\"SOURCE_ACCOUNT_CURRENCY\",\"payeeId\":\"7977557255484c7345546c4e53424766634b6c53756841672b556857626e395253334b70416449676b42673d\",\"chargeBearer\":\"BENEFICIARY\",\"paymentMethod\":\"GIRO\",\"fxDealReferenceNumber\":\"12345678\",\"remarks\":\"lajeluro\",\"transferPurpose\":\"CASH_DISBURSEMENT\"}");
//        Request request = new Request.Builder()
//                .url("https://sandbox.apihub.citi.com/gcb/api/v1/moneyMovement/internalDomesticTransfers/preprocess")
//                .post(body)
//                .addHeader("authorization", authorization)
//                .addHeader("uuid", UUID.randomUUID().toString())
//                .addHeader("accept", "application/json")
//                .addHeader("client_id", APIConstant.CLIENT_ID)
//                .addHeader("content-type", "application/json")
//                .build();
//
//        Response response = client.newCall(request).execute();
//        JSONObject jsonObject = (JSONObject) JSONValue.parse(response.body().string());
//        System.out.println(jsonObject);
//        return "";
//    }

    private String createTransfer(Account srcAcct, Double transferamount,String srcAcctId, String destAcctId) throws IOException{
        OkHttpClient client = new OkHttpClient();
       String authorization = "Bearer " + this.getRealAccessToken(srcAcct.getUsername() , srcAcct.getPassword());
       MediaType mediaType = MediaType.parse("application/json");
      // RequestBody body = RequestBody.create(mediaType, "{\"sourceAccountId\":\"355a515030616a53576b6a65797359506a634175764a734a3238314e4668627349486a676f7449463949453d\",\"transactionAmount\":4500.25,\"transferCurrencyIndicator\":\"SOURCE_ACCOUNT_CURRENCY\",\"payeeId\":\"7977557255484c7345546c4e53424766634b6c53756841672b556857626e395253334b70416449676b42673d\",\"chargeBearer\":\"BENEFICIARY\",\"paymentMethod\":\"GIRO\",\"fxDealReferenceNumber\":\"12345678\",\"remarks\":\"lajeluro\",\"transferPurpose\":\"CASH_DISBURSEMENT\"}");
        RequestBody body = RequestBody.create(mediaType, "{\"sourceAccountId\":\""+srcAcctId+"\",\"transactionAmount\":4500.25,\"transferCurrencyIndicator\":\"SOURCE_ACCOUNT_CURRENCY\",\"payeeId\":\""+destAcctId+"\",\"chargeBearer\":\"BENEFICIARY\",\"paymentMethod\":\"GIRO\",\"fxDealReferenceNumber\":\"12345678\",\"remarks\":\"lajeluro\",\"transferPurpose\":\"CASH_DISBURSEMENT\"}");
        Request request = new Request.Builder()
                .url("https://sandbox.apihub.citi.com/gcb/api/v1/moneyMovement/internalDomesticTransfers/preprocess")
               .post(body)
                .addHeader("authorization", authorization)
                .addHeader("uuid", UUID.randomUUID().toString())
               .addHeader("accept", "application/json")
                .addHeader("client_id", APIConstant.CLIENT_ID)
               .addHeader("content-type", "application/json")
               .build();

       Response response = client.newCall(request).execute();
       JSONObject jsonObject = (JSONObject) JSONValue.parse(response.body().string());
       System.out.println(jsonObject);
       return  "";
    }

    private void confirmTransfer(Account account , String controlFlowId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String authorization = "Bearer " + this.getRealAccessToken(account.getUsername() , account.getPassword());

        MediaType mediaType = MediaType.parse("application/json");
       RequestBody body = RequestBody.create(mediaType, "{\"controlFlowId\":\"45534b7438634c567a566777354c5861486d59616c4665467a624e61724c73574b4c50494f386664306d6f3d\"}");
      // RequestBody body = RequestBody.create(mediaType, "{\"controlFlowId\":\" "+controlFlowId+"\"}");
        Request request = new Request.Builder()
                .url("https://sandbox.apihub.citi.com/gcb/api/v1/moneyMovement/internalDomesticTransfers")
                .post(body)
                .addHeader("authorization", authorization)
                .addHeader("uuid", UUID.randomUUID().toString())
                .addHeader("accept", "application/json")
                .addHeader("client_id", APIConstant.CLIENT_ID)
                .addHeader("content-type", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(response.body().string());
        System.out.println(jsonObject);
    }

    public static void main(String args[]) throws IOException {

//       GetAccounts accs = new GetAccounts();
//      APIContext context=new APIContext();
//      accs.getBizToken(context);
//      context.setUsername("SandboxUser1");
//      context.setPassword("9188baa47f6a0d176380b195e69979e8f4a324148dd8fd64092c537e5231c02dc2de71d5aebb569aca955548dd8a619c9754a8e24d29a426ee91088ef8172071f095670449464d046de5841433255758cf9eaf1aff08a94dfeb812bb42c5da23cb1bd02e7d489a7873c2780919d34c5e36d0fee2f185b58fdb6c751a5f290f0fa13afb634a4ea7e6683e6c2340571440caa7031c2e91660362b0e3a6c822259cea432c6ae4dc5cc07908532df70a3a474fafb986bf7b0d922be6db7c0051eb73fbdc76732bceb26faaf40a1a5bbaf43e7c671ee4f749a1cf4a3db5a44a217553a9565dea05d7afee46523717a1beac92b897462183bbd9cfc1e323d3f272beb4");
////       context.setUsername("SandboxUser2");
////       context.setPassword("47dd428049b709aae82556f2969bcf4923d7de9be889173e26285a310d7ce3df5b82b89875f59efb70154df01150cc505a067b7103f1828fd6e700e61de19f3455c6e646fc8e7bb6dc78a092f7a28d4f3bb3565faa5c6bc3ecd50b885637fe9e57602fa7aab816a5627d04ea8937b0f76d937e8a4f04c90ee0120af5e8e2895577e0a9a2f9260002b551425c7c61292c54091fefa7492eec5c5415fc09e800149d1aa7ae74878640e662f82146ebcf245f03e04089f5fce042a98ed3609499b5764637207ff2b8b86e5f87a28d9109a7a02bad04d654cc7bce509e1620260b1a7893f2f6c08d332a7266c24b12e4f48f7987483a2d5930808ac71676f1bafa3e");
//      accs.getAccountIds(context.getUsername(), context.getPassword(),context);
////       accs.getAccountIds("SandboxUser1","9188baa47f6a0d176380b195e69979e8f4a324148dd8fd64092c537e5231c02dc2de71d5aebb569aca955548dd8a619c9754a8e24d29a426ee91088ef8172071f095670449464d046de5841433255758cf9eaf1aff08a94dfeb812bb42c5da23cb1bd02e7d489a7873c2780919d34c5e36d0fee2f185b58fdb6c751a5f290f0fa13afb634a4ea7e6683e6c2340571440caa7031c2e91660362b0e3a6c822259cea432c6ae4dc5cc07908532df70a3a474fafb986bf7b0d922be6db7c0051eb73fbdc76732bceb26faaf40a1a5bbaf43e7c671ee4f749a1cf4a3db5a44a217553a9565dea05d7afee46523717a1beac92b897462183bbd9cfc1e323d3f272beb4",context);
////       accs.getAccounts(context.getUsername(), context.getPassword(),context);
////       accs.getAccountDetail("3739334c4d3463614356474f6d7650667a737656664652677747796855646c5552745a43346d37423653553d",context);
////       accs.getTransactions("355a515030616a53576b6a65797359506a634175764a734a3238314e4668627349486a676f7449463949453d",context);

       Account srcAcct=new Account("SandboxUser1","9188baa47f6a0d176380b195e69979e8f4a324148dd8fd64092c537e5231c02dc2de71d5aebb569aca955548dd8a619c9754a8e24d29a426ee91088ef8172071f095670449464d046de5841433255758cf9eaf1aff08a94dfeb812bb42c5da23cb1bd02e7d489a7873c2780919d34c5e36d0fee2f185b58fdb6c751a5f290f0fa13afb634a4ea7e6683e6c2340571440caa7031c2e91660362b0e3a6c822259cea432c6ae4dc5cc07908532df70a3a474fafb986bf7b0d922be6db7c0051eb73fbdc76732bceb26faaf40a1a5bbaf43e7c671ee4f749a1cf4a3db5a44a217553a9565dea05d7afee46523717a1beac92b897462183bbd9cfc1e323d3f272beb4");
       Account destAcct=new Account("SandboxUser2","6f376ba446f36da3e32471eb5b73674a4a49978fac50c3e6e566d4985b29f0d104fba2c1d2b2ccaa2c194326ee3330c9cb2c0b77e75217e827bdc6125dcd7085c9cb01b719a36fccf60584d13d02967d45d1cc6dac68c6fb3b45f8c5219084497189c10f47398db56e6a6e6f2face5f8d653428ea8ef0d653ac15b29fb695ca39f3b0d32a86badf0778eead7d303fafd958a4df3268c9567101617df3a7b42734a20a7fa6a613c60b33bf3d41d9bb5d4378d9ede1f46d021748e43823d721fad8fc2bfc168f81525f1e1091cdb138c94393b606f7de7f0b9608c13546b808846b69bf14c7b58384083ed6abdff60e6dd85f43d13692f8e7ff3d66b62e3c57392");
       HUAQIAPI API=new HUAQIAPI();
//        API.retrievePayeeList(srcAcct);
       API.createTransfer(srcAcct,1000.0,"355a515030616a53576b6a65797359506a634175764a734a3238314e4668627349486a676f7449463949453d","7977557255484c7345546c4e53424766634b6c53756841672b556857626e395253334b70416449676b42673d");
//        API.createTransfer(destAcct,srcAcct,1000.0);

//        Account destAcct=new Account("SandboxUser2","47dd428049b709aae82556f2969bcf4923d7de9be889173e26285a310d7ce3df5b82b89875f59efb70154df01150cc505a067b7103f1828fd6e700e61de19f3455c6e646fc8e7bb6dc78a092f7a28d4f3bb3565faa5c6bc3ecd50b885637fe9e57602fa7aab816a5627d04ea8937b0f76d937e8a4f04c90ee0120af5e8e2895577e0a9a2f9260002b551425c7c61292c54091fefa7492eec5c5415fc09e800149d1aa7ae74878640e662f82146ebcf245f03e04089f5fce042a98ed3609499b5764637207ff2b8b86e5f87a28d9109a7a02bad04d654cc7bce509e1620260b1a7893f2f6c08d332a7266c24b12e4f48f7987483a2d5930808ac71676f1bafa3e");
//        HUAQIAPI api=new HUAQIAPI();

     // API.createTransfer(srcAcct,100.0);
    // API.getPayeeIds(srcAcct);
   // API.retrievePayeeList(srcAcct);

        //API.confirmTransfer(srcAcct,"11");


    }

}
