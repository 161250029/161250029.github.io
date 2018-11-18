
import net.sf.json.JSONException;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;

import org.json.simple.JSONValue;

import net.sf.json.JSONObject;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;

import java.io.IOException;
import java.util.*;

public class GetAccounts {
	
	public static Map getBizToken(APIContext context) throws IOException{
		step1GetAccessToken(context);
		if(context.getAccessToken()==null){
			return null;
		}
		Map map = step2GetBizToken(context);
		if(context.getEventId()==null){
			return null;
		}
		return map;
	}
	public static ArrayList<String> getAccountIds(String username, String password, APIContext context) throws IOException {
	    ArrayList<String> accountIds = new ArrayList<String>();
	    String accounts=getAccounts(username,password,context);
        JSONObject object= JSONObject.fromObject(accounts);            //使用net.sf.json包
		if(object.has("accountGroupSummary")){                 //外层嵌套
			  JSONArray outer=object.getJSONArray("accountGroupSummary");      //获得accountGroupSummary对象

			  for(int i=0;i< outer.size();i++){
			  	   String t=outer.getString(i);
			  	   JSONObject temp=JSONObject.fromObject(t);       //每一个实体中都有一个account属性
			  	   if(temp.has("accounts")){
			  	       JSONArray listAccounts=temp.getJSONArray("accounts");
			  	       for(int j=0;j<listAccounts.size();j++){
			  	           String h=listAccounts.getString(j);
			  	           JSONObject second = JSONObject.fromObject(h);
                           try {
                               Iterator it=second.keys();
                               while (it.hasNext()){
                                   JSONObject summary=(JSONObject) second.get(it.next());             //强类型转换，得到的值是object类型。
                                   accountIds.add(summary.get("accountId").toString());
                                   System.out.println(summary.get("accountId"));
                               }
                           } catch (JSONException e) {
                               e.printStackTrace();
                           }
                       }
                   }
			  }
		}
		System.out.println(accountIds);
        return accountIds;
    }
	public static String getAccounts(String username, String password, APIContext context) throws IOException {
		context.setUsername(username);
		context.setPassword(password);
		step3GetRealAccessToken(context);
		if(context.getRealAccessToken()==null){
			return null;
		}
		String accounts = step4GetAccounts(context);
		if(context.getAccounts()==null){
			return null;
		}
		return accounts;
	}
	
	public static String getAccountDetail(String accountId, APIContext context) throws IOException {
		context.setAccountId(accountId);
		String accountDetail = step5GetAccountDetails(context);
		if(accountDetail == null) {
			return null;
		}
		return accountDetail;
	}
	
	public static String getTransactions(String accountId, APIContext context) throws IOException {
		context.setAccountId(accountId);
		String transaction = step6GetTransaction(context);
		if(transaction == null) {
			return null;
		}
		return transaction;
	}
	
	public static String step1GetAccessToken(APIContext context) throws IOException {
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
		org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) JSONValue.parse(response.body().string());
		String accessToken = (String) jsonObject.get("access_token");
		context.setAccessToken(accessToken);
		System.out.println("step1 access_token:");
		System.out.println("\t" + accessToken);
		return accessToken;
	}

	public static Map step2GetBizToken(APIContext context) throws IOException {
		Map<String, String> map = new HashMap<>();
		OkHttpClient client = new OkHttpClient();
		String client_id = APIConstant.CLIENT_ID;
		String accessToken = context.getAccessToken();
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
		org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) JSONValue.parse(response.body().string());
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
			context.setEventId(eventId);
			context.setBizToken(bizToken);
		}
		System.out.println("step2 map:");
		for (String s : map.keySet()) {
			System.out.println("\tkey:" + s + "\tvalues:" + map.get(s));
		}
		return map;
	}
	
	public static String step3GetRealAccessToken(APIContext context) throws IOException{
		String client_id = APIConstant.CLIENT_ID;
		String client_scrent = APIConstant.CLIENT_SCRENT;
		String bizToken = context.getBizToken();
		System.err.println("bizToken: "+bizToken);
		String encode_key = client_id + ":" + client_scrent;
		String authorization = "Basic " + Base64.encodeBase64String(encode_key.getBytes());
		String username = context.getUsername();
		String password = context.getPassword();
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
		org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) JSONValue.parse(response.body().string());
		String realAccessToken = (String) jsonObject.get("access_token");
		context.setRealAccessToken(realAccessToken);
		System.out.println("step3 real_access_token:");
		System.out.println("\t" + realAccessToken);
		return realAccessToken;
	}
	
	public static String step4GetAccounts(APIContext context) throws IOException{
		String client_id = APIConstant.CLIENT_ID;
		String authorization = "Bearer " + context.getRealAccessToken();
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
		context.setAccounts(responseBodyString);
		System.out.println("step4 accounts:");

		System.out.println("\t"+responseBodyString);
		return responseBodyString;
	}
	
	public static String step5GetAccountDetails(APIContext context) throws IOException{
		String client_id = APIConstant.CLIENT_ID;
		String authorization = "Bearer " + context.getRealAccessToken();
		UUID uuid = UUID.randomUUID();
		String accountId = context.getAccountId();
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url("https://sandbox.apihub.citi.com/gcb/api/v1/accounts/"+accountId)
				.get()
				.addHeader("authorization", authorization)
				.addHeader("uuid", uuid.toString())
				.addHeader("content-type", "application/json")
				.addHeader("accept", "application/json")
				.addHeader("client_id", client_id)
				.build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		context.setAccounts(responseBodyString);
		System.out.println("step5 account details:");
		System.out.println("\t"+responseBodyString);
		return responseBodyString;
	}
	
	public static String step6GetTransaction(APIContext context) throws IOException{
		String client_id = APIConstant.CLIENT_ID;
		String authorization = "Bearer " + context.getRealAccessToken();
		UUID uuid = UUID.randomUUID();
		String accountId = context.getAccountId();
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url("https://sandbox.apihub.citi.com/gcb/api/v1/accounts/"+accountId+"/transactions")
				.get()
				.addHeader("authorization", authorization)
				.addHeader("uuid", uuid.toString())
				.addHeader("content-type", "application/json")
				.addHeader("accept", "application/json")
				.addHeader("client_id", client_id)
				.build();
		Response response = client.newCall(request).execute();
		String responseBodyString = response.body().string();
		context.setAccounts(responseBodyString);
		System.out.println("step6 transaction details:");
		System.out.println("\t"+responseBodyString);
		return responseBodyString;
	}
}
