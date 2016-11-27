package dw.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLProtocolException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultHttpClientConnectionOperator;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import dw.utils.Utils;


public class HttpUtils {

	public enum RequestType {
		GET, POST, PUT
	}
	private static class Holder {
        static final HttpUtils INSTANCE = new HttpUtils();
    }

	public synchronized static HttpUtils getInstance() {
		return Holder.INSTANCE;
	}

	private static final int HTTP_TIMEOUT = 60000;
	private static final int HTTP_TIMEOUT_TEST = 600000;
	private static final int HTTP_429_STANDARD_RETRY_TIME = 60*5; // 5 minutes
	
	public static final String ENCODING = "UTF-8";

	// protected ClientConnectionManager createSSLConnManager() {
	// SSLSocketFactory easySSLSocketFactory = createEasySSLSocketFactory();
	// easySSLSocketFactory.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	// Scheme httpsScheme = new Scheme("https", 443, easySSLSocketFactory);
	// SchemeRegistry schemeRegistry = new SchemeRegistry();
	// schemeRegistry.register(httpsScheme);
	// return new BasicClientConnectionManager(schemeRegistry);
	//
	// }

	public String getString(String url) throws Exception {
		return getString(url, "UTF-8");
	}

	public String getString(String url, String encoding) throws Exception {
		return getString(url, null, null, encoding);
	}

	public String getString(String url, String username, String password, String encoding) throws Exception {
		return getString(url, username, password, encoding, null);
	}

	public String getString(String url, String username, String password, String encoding, Map<String, ? extends Object> paramsMap) throws Exception {
		return getString(url, username, password, encoding, paramsMap, RequestType.GET);
	}

	public String getString(String url, String username, String password, String encoding, Map<String, ? extends Object> paramsMap, RequestType requestType) throws Exception {
		return getString(url, username, password, encoding, paramsMap, requestType, null);
	}

	public String getString(String url, String username, String password, String encoding, Map<String, ? extends Object> paramsMap, RequestType requestType, String body) throws Exception {
		return getString(url, username, password, encoding, paramsMap, requestType, body, null);
	}

	public String getString(String url, String username, String password, String encoding, Map<String, ? extends Object> paramsMap, RequestType requestType, String body, Map<String, String> additionalHeaders) throws Exception {
		return getString(url, username, password, encoding, paramsMap, requestType, body, additionalHeaders, null);
	}

	public String getString(String url, String username, String password, String encoding, Map<String, ? extends Object> paramsMap, RequestType requestType, String body, Map<String, String> additionalHeaders, ContentType contentType) throws Exception {
		return getString(url, username, password, encoding, paramsMap, requestType, body, additionalHeaders, contentType, null, null);
	}

	public String getString(String url, String username, String password, String encoding, Map<String, ? extends Object> paramsMap, RequestType requestType, String body, Map<String, String> additionalHeaders, ContentType contentType, File file, ContentType fileContentType) throws Exception {
		return getString(url, username, password, encoding, paramsMap, requestType, body, additionalHeaders, contentType, file, fileContentType, false);
	}


	public String getString(String url, String username, String password, String encoding, Map<String, ? extends Object> paramsMap, RequestType requestType, String body, Map<String, String> additionalHeaders, ContentType contentType, File file, ContentType fileContentType, boolean useProxy) throws Exception {
		StringWriter writer = new StringWriter();
		InputStream stream = getStream(url, username, password, paramsMap, requestType, encoding, file, fileContentType, body, null, 0, additionalHeaders, contentType, useProxy);
		IOUtils.copy(stream, writer, encoding);
		stream.close();
		return writer.toString();
	}

	/**
	 * IMPORTANT: close the returned InputStream! (use try-finally-Block.)
	 *
	 * @throws Exception
	 */
	public HttpInputStream getStream(String url, String username, String password) throws Exception {
		return getStream(url, username, password, null, RequestType.GET);
	}

	/**
	 * IMPORTANT: close the returned InputStream! (use try-finally-Block.)
	 *
	 * @throws Exception
	 */
	public HttpInputStream getStream(String url, String username, String password, Map<String, Object> paramsMap) throws Exception {
		return getStream(url, username, password, paramsMap, RequestType.GET);
	}

	/**
	 * IMPORTANT: close the returned InputStream! (use try-finally-Block.)
	 *
	 * @throws Exception
	 */
	public HttpInputStream getStream(String url, String username, String password, File file, ContentType fileContentType) throws Exception {
		return getStream(url, username, password, null, RequestType.POST, "UTF-8", file, fileContentType, null, null, 0, null, null, false);
	}

	/**
	 * IMPORTANT: close the returned InputStream! (use try-finally-Block.)
	 *
	 * @throws Exception
	 */
	public HttpInputStream getStream(String url, String username, String password, Map<String, ? extends Object> paramsMap, RequestType requestType) throws Exception {
		return getStream(url, username, password, paramsMap, requestType, "UTF-8");
	}

	/**
	 * IMPORTANT: close the returned InputStream! (use try-finally-Block.)
	 *
	 * @throws Exception
	 */
	public HttpInputStream getStream(String url, String username, String password, Map<String, ? extends Object> paramsMap, RequestType requestType, String encoding) throws Exception {
		return getStream(url, username, password, paramsMap, requestType, encoding, null, null, null, null, 0, null, null, false);
	}

	/**
	 * IMPORTANT: close the returned InputStream! (use try-finally-Block.)
	 *
	 * @throws Exception
	 */
	public HttpInputStream getStream(String url, String username, String password, Map<String, ? extends Object> paramsMap, RequestType requestType, String encoding, String body) throws Exception {
		return getStream(url, username, password, paramsMap, requestType, encoding, null, null, null, null, 0, null, null, false);
	}

	/**
	 * IMPORTANT: close the returned InputStream! (use try-finally-Block.)
	 *
	 * @throws Exception
	 */
	public HttpInputStream getStream(String url, String username, String password, Map<String, ? extends Object> paramsMap, RequestType requestType, String encoding, File file, ContentType fileContentType, String body, String previousRedirectUrl, int redirectCount, Map<String, String> additionalHeaders, ContentType contentType, boolean useProxy) throws Exception {
		if (requestType != RequestType.POST && !"UTF-8".equals(encoding)) {
			throw new RuntimeException("For request types other than POST only UTF-8 encoding is supported.");
		}
		if (requestType != RequestType.POST && file != null) {
			throw new RuntimeException("For posting a file you have to use request type POST.");
		}
		if (file != null && fileContentType == null) {
			throw new RuntimeException("For posting a file you have to specify a fileContentType.");
		}
		if (paramsMap == null && file != null) {
			throw new RuntimeException("For posting a file you also need a paramsMap.");
		}
		if (requestType != RequestType.PUT && requestType != RequestType.POST && body != null) {
			throw new RuntimeException("Body is only allowed for PUT and POST.");
		}
		if (requestType == RequestType.PUT && body != null && paramsMap != null && !paramsMap.isEmpty()) {
			throw new RuntimeException("Only body OR parameters is allowed for PUT.");
		}
		if (contentType != null && StringUtils.isEmpty(body)) {
			throw new RuntimeException("Specify contentType only with body");
		}

		HttpResponse response = null;

		try {
			Proxy proxy = useProxy ? getProxy() : null;

			Builder configBuilder = RequestConfig.custom();
				configBuilder.setSocketTimeout(HTTP_TIMEOUT).setConnectTimeout(HTTP_TIMEOUT);
			if (useProxy)
				configBuilder.setProxy(new HttpHost(proxy.getHost(), proxy.getPort()));
			HttpClientBuilder httpClientBuilder = HttpClients.custom();
			httpClientBuilder.setDefaultRequestConfig(configBuilder.build());

			SSLContextBuilder sslBuilder = new SSLContextBuilder();
			sslBuilder.loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			});

			SSLContext sslContext = sslBuilder.build();
			SSLConnectionSocketFactory sslsf = new SniSSLSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
			// new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
			Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", new PlainConnectionSocketFactory()).register("https", sslsf).build();
			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(new SniHttpClientConnectionOperator(registry), null, -1, TimeUnit.MILLISECONDS);

			cm.setMaxTotal(2000);// max connection
			httpClientBuilder.setSSLSocketFactory(sslsf);
			httpClientBuilder.setConnectionManager(cm);

			// DefaultHttpClient httpClient = null;
			// HttpParams httpParams = new BasicHttpParams();
			// HttpConnectionParams.setConnectionTimeout(httpParams, HTTP_TIMEOUT);
			// HttpConnectionParams.setSoTimeout(httpParams, HTTP_TIMEOUT);
			// if (url.toLowerCase().startsWith("https://")) {
			// builder.setSSLSocketFactory(sslSocketFactory)
			// httpClient = new DefaultHttpClient(createSSLConnManager(), httpParams);
			// } else {
			// httpClient = new DefaultHttpClient(httpParams);
			// }

			// CloseableHttpClient httpClient = builder.build();

			if (username != null) {
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(getAuthScopeFromUrl(url), new UsernamePasswordCredentials(username, password));
				if (useProxy)
					credsProvider.setCredentials(new AuthScope(proxy.getHost(), proxy.getPort()), new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));
				httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
			}

			httpClientBuilder.addInterceptorLast(new HttpResponseInterceptor() {
				@Override
				public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
					// manchmal encoding nicht als HTTP.CONTENT_ENCODING Header sondern nur im HTTP.CONTENT_TYPE
					if (response.getFirstHeader(HTTP.CONTENT_ENCODING) == null) {
						Header ct = response.getFirstHeader(HTTP.CONTENT_TYPE);
						if (ct != null) {
							for (HeaderElement he : ct.getElements()) {
								NameValuePair charset = he.getParameterByName("charset");
								if (charset != null) {
									response.setHeader(new BasicHeader(HTTP.CONTENT_ENCODING, charset.getValue()));
									break;
								}
							}
						}
					}
				}
			});

			CloseableHttpClient httpClient = httpClientBuilder.build();

			URIBuilder builder = new URIBuilder(url);

			if (requestType == RequestType.GET && paramsMap != null && !paramsMap.isEmpty()) {
				for (String key : paramsMap.keySet()) {
					builder.setParameter(key, paramsMap.get(key).toString());
				}
			}
			URI uri = builder.build();
			HttpRequestBase request = constructRequest(requestType, uri);
			if ((requestType == RequestType.POST || requestType == RequestType.PUT) && paramsMap != null && !paramsMap.isEmpty()) {
				if (file == null) {
					((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(toPairs(paramsMap), encoding));
				} else {
					MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
					FileBody fileBody = new FileBody(file);
					entityBuilder.addPart("file", fileBody);
					for (String key : paramsMap.keySet()) {
						Object o = paramsMap.get(key);
						if (o != null) {
							entityBuilder.addTextBody(key, o.toString());
						}
					}
					((HttpEntityEnclosingRequestBase) request).setEntity(entityBuilder.build());
				}
			}
			if (body != null) {
				if (contentType != null) {
					((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(body, contentType));
				} else {
					((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(body));
				}
			}

			if (additionalHeaders != null) {
				for (String header : additionalHeaders.keySet()) {
					request.addHeader(header, additionalHeaders.get(header));
				}
			}

			response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			HttpInputStream stream = new HttpInputStream(entity);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 300 || statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 305 || statusCode == 307 || statusCode == 308) {
				Header[] location = response.getHeaders("Location");
				String newUrl = (location != null && location.length > 0) ? location[0].getValue() : null;
				if (newUrl != null) {
					// wenn redirect von https auf http -> SSLException schmeißen (wichtig für ShopInterfaceLogic#init())
					if (url.toLowerCase().startsWith("https://") && newUrl.toLowerCase().startsWith("http://")) {
						throw new SSLException("Redirecting to non-SSL URL: " + newUrl);
					}
					if (!newUrl.contains("://")) {
						if (!newUrl.startsWith("/") && !url.endsWith("/")) {
							newUrl = "/" + newUrl;
						}
						newUrl = url + newUrl;
					}

					// looping verhindern
					if (newUrl.equals(url)) {
						StringWriter writer = new StringWriter();
						IOUtils.copy(stream, writer, encoding);
						stream.close();
						String ws = writer.toString();
						throw new CircularRedirectException("Circular " + response.getStatusLine() + " redirect to " + newUrl + "." + (StringUtils.isEmpty(ws) ? "" : (" Content: " + ws)));
					}
					if (redirectCount > 10) {
						throw new CircularRedirectException("Redirected more than 10 times. Current redirect: " + url + " -> " + newUrl);
					}
					return getStream(newUrl, username, password, paramsMap, requestType, encoding, file, fileContentType, body, url, redirectCount + 1, additionalHeaders, contentType, useProxy);
				} else {
					StringWriter writer = new StringWriter();
					IOUtils.copy(stream, writer, encoding);
					stream.close();
					throw new RedirectException("Redirecting to empty location: " + writer.toString());
				}
			} else if (statusCode == 429 && redirectCount < 10 && waitForRateLimitReset(response)) {
				return getStream(url, username, password, paramsMap, requestType, encoding, file, fileContentType, body, previousRedirectUrl, 10, additionalHeaders, contentType, useProxy);
			} else if (statusCode > 300) { // unbekannte/unbehandelte 3xx redirects, 4xx + 5xx fehler
				StringWriter writer = new StringWriter();
				IOUtils.copy(stream, writer, encoding);
				stream.close();
				throw new dw.http.HttpException(url, response.getStatusLine(), writer.toString());
			}
			return stream;

		} catch (Exception e) {
			try {
				if (response != null)
					EntityUtils.consume(response.getEntity());
			} catch (IOException ioe) {
				// ignore
			}
			throw e;
		}

	}

	private boolean waitForRateLimitReset(HttpResponse response) {
		Header[] h = response.getHeaders("X-RateLimit-Reset");
		long reset = 0;
		if (h == null || h.length == 0 || !org.apache.commons.lang3.StringUtils.isNumeric(h[0].getValue())) {
			reset = HTTP_429_STANDARD_RETRY_TIME;
		} else {
			reset = Long.parseLong(h[0].getValue());
		}

		if (reset < 15*60) {
			Utils.sleep(reset*1000 + 5000);
			return true;
		} else {
			return false;
		}
	}

	protected List<NameValuePair> toPairs(Map<String, ? extends Object> paramsMap) {
		List<NameValuePair> ret = new ArrayList<NameValuePair>();
		if (paramsMap == null)
			return ret;

		for (String key : paramsMap.keySet()) {
			Object o = paramsMap.get(key);
			ret.add(new BasicNameValuePair(key, o == null ? null : o.toString()));
		}

		return ret;
	}

	protected HttpRequestBase constructRequest(RequestType type, URI uri) {
		switch (type) {
		case GET:
			return new HttpGet(uri);
		case POST:
			return new HttpPost(uri);
		case PUT:
			return new HttpPut(uri);
		default:
			throw new RuntimeException("Unknown request type: " + type);
		}
	}

	protected AuthScope getAuthScopeFromUrl(String urlString) throws MalformedURLException {
		URL url = new URL(urlString);

		AuthScope auth = new AuthScope(url.getHost(), url.getPort());

		return auth;
	}

	// protected SSLSocketFactory createEasySSLSocketFactory() {
	// try {
	// return new SSLSocketFactory(new TrustStrategy() {
	//
	// @Override
	// public boolean isTrusted(final X509Certificate[] chain, String authType) throws CertificateException {
	// // Oh, I am easy...
	// return true;
	// }
	//
	// });
	// } catch (Exception e) {
	// throw new RuntimeException(e);
	// }
	// }

	/** Decodes a application/x-www-form-urlencoded string using the encoding from ShopInterfaceLogic.ENCODING. Additionally handles '+' correctly. */
	public static String decode(String encoded) {
		if (encoded == null)
			return null;
		String s = encoded; // entfernt - mal schaun, obs schadet .replace("+", "%2B");
		try {
			return URLDecoder.decode(s, HttpUtils.ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String encode(String s) {
		if (s == null)
			return null;
		try {
			return URLEncoder.encode(s, ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Splits the params of a application/x-www-form-urlencoded string into a key-value Map, using the encoding from ShopInterfaceLogic.ENCODING.
	 * Additionally handles '+' correctly. Also supports arrays.
	 */
	public static Map<String, String> getParamsFromUrlString(String encodedUrlString) {
		List<NameValuePair> list = URLEncodedUtils.parse(encodedUrlString, Charset.forName(ENCODING));

		Map<String, String> ret = new HashMap<String, String>();
		for (NameValuePair nvp : list) {
			ret.put(nvp.getName(), nvp.getValue());
		}

		return ret;
	}

	public static class SniHttpClientConnectionOperator extends DefaultHttpClientConnectionOperator {

		public SniHttpClientConnectionOperator(Lookup<ConnectionSocketFactory> socketFactoryRegistry) {
			super(socketFactoryRegistry, null, null);
		}

		@Override
		public void connect(final ManagedHttpClientConnection conn, final HttpHost host, final InetSocketAddress localAddress, final int connectTimeout, final SocketConfig socketConfig, final HttpContext context) throws IOException {
			try {
				super.connect(conn, host, localAddress, connectTimeout, socketConfig, context);
			} catch (SSLProtocolException e) {
				Boolean enableSniValue = (Boolean) context.getAttribute(SniSSLSocketFactory.ENABLE_SNI);
				boolean enableSni = enableSniValue == null || enableSniValue;
				if (enableSni && e.getMessage() != null && e.getMessage().equals("handshake alert:  unrecognized_name")) {
					// Server received saw wrong SNI host, retrying without SNI
					context.setAttribute(SniSSLSocketFactory.ENABLE_SNI, false);
					super.connect(conn, host, localAddress, connectTimeout, socketConfig, context);
				} else {
					throw e;
				}
			}
		}
	}

	public static class SniSSLSocketFactory extends SSLConnectionSocketFactory {

		public static final String ENABLE_SNI = "__enable_sni__";

		/*
		 * Implement any constructor you need for your particular application - SSLConnectionSocketFactory has many variants
		 */
		public SniSSLSocketFactory(final SSLContext sslContext, final HostnameVerifier verifier) {
			super(sslContext, verifier);
		}

		@Override
		public Socket createLayeredSocket(final Socket socket, final String target, final int port, final HttpContext context) throws IOException {
			Boolean enableSniValue = (Boolean) context.getAttribute(ENABLE_SNI);
			boolean enableSni = enableSniValue == null || enableSniValue;
			return super.createLayeredSocket(socket, enableSni ? target : "", port, context);
		}
	}

	private Proxy getProxy() {
		Proxy r = new Proxy();
		r.setHost("ec2-52-29-29-170.eu-central-1.compute.amazonaws.com");
		r.setPort(3128);
		r.setUsername("gd37dtgdh");
		r.setPassword("bampe0267dghsmqw9153");
		return r;
	}

}
