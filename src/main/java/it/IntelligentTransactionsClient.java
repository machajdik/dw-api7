package it;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.http.entity.ContentType;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import dw.data.Transaction;
import dw.http.HttpUtils;
import dw.http.HttpUtils.RequestType;

public class IntelligentTransactionsClient {
	
	private static final String URL = "https://intt.cfapps.us10.hana.ondemand.com/analyse";

	private static final Gson gson = new Gson();

	public static Map<String, AnalysisResponse> analyse(List<Transaction> list) {
		
		@SuppressWarnings("serial")
		Type mapType = new TypeToken<Map<String, AnalysisResponse>>() {
		}.getType();
		try {
			return gson.fromJson(HttpUtils.getInstance().getString(URL, null, null, HttpUtils.ENCODING, null, RequestType.POST, gson.toJson(list), null, ContentType.APPLICATION_JSON), mapType);
		} catch (JsonSyntaxException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
	}

}
