package dw.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.StatusLine;

public class HttpException extends RuntimeException {

	private static final long serialVersionUID = 4697260323639460520L;

	private String url;
	private StatusLine status;
	private String content;

	public HttpException(String url, StatusLine status, String content) {
		this.status = status;
		this.url = shortenUrl(url);
		this.content = content;
	}

	protected String shortenUrl(String url) {
		if (url == null || StringUtils.isEmpty(url))
			return url;

		int i = url.lastIndexOf("/");
		if (i == -1)
			i = url.lastIndexOf("?");

		return i == -1 ? url : url.substring(0, i);
	}

	@Override
	public String getMessage() {
		return "Connection to " + this.url + " not successful: " + this.status.toString() + "." + (StringUtils.isEmpty(this.content) ? "" : ("\nContent:\n" + this.content + "\n"));
	}


	public int getStatusCode() {
		return this.status == null ? 0 : this.status.getStatusCode();
	}

	public String getContent() {
		return this.content;
	}




}
