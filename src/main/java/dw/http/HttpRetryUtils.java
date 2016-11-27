package dw.http;

import java.io.File;
import java.util.Map;

import org.apache.http.entity.ContentType;

public class HttpRetryUtils extends HttpUtils {

	private static final int MAX_TRIES = 4;
	private static final int WAIT_SEC = 4000;

	private static class Holder {
        static final HttpRetryUtils INSTANCE = new HttpRetryUtils();
    }

	public synchronized static final HttpRetryUtils getInstance() {
		return Holder.INSTANCE;
	}

	@Override
	public HttpInputStream getStream(String url, String username, String password, Map<String, ? extends Object> paramsMap, RequestType requestType, String encoding, File file, ContentType fileContentType, String body, String previousRedirectUrl, int redirectCount, Map<String, String> additionalHeaders, ContentType contentType, boolean useProxy) throws Exception {
		int i = 0;

		while (i <= MAX_TRIES) {
			try {
				sleep(WAIT_SEC * i * i); // 0 + 4 + 16 + 36 = 56 sec maximum
				i++;
				return super.getStream(url, username, password, paramsMap, requestType, encoding, file, fileContentType, body, previousRedirectUrl, redirectCount, additionalHeaders, contentType, useProxy);
			} catch (Exception ignore) {
			}
		}

		// try once more to provoke the error
		return super.getStream(url, username, password, paramsMap, requestType, encoding, file, fileContentType, body, previousRedirectUrl, redirectCount, additionalHeaders, contentType, useProxy);
	}

	private static void sleep(long millis) {
		if (millis <= 0)
			return;
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

}
