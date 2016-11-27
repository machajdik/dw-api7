package dw.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

public class HttpInputStream extends InputStream {

	private InputStream is;
	private long contentLength;
	private String charset;
	
	public HttpInputStream(HttpEntity entity) throws IllegalStateException, IOException {
		
		this.is = entity.getContent();
		
		// Header encoding = entity.getContentEncoding(); // bei SEM nicht vorhanden
		Header ct = entity.getContentType();
		if (ct != null) {
			for (HeaderElement he : ct.getElements()) {
				NameValuePair charset = he.getParameterByName("charset");
				if (charset != null) {
					this.charset = charset.getValue();
				}
				break;
			}
		}
		
		this.contentLength = entity.getContentLength();
	}
	
	public HttpInputStream(InputStream is, long contentLength, String charset) {
		this.is = is;
		this.contentLength = contentLength;
		this.charset = charset;
	}

	@Override
	public int read() throws IOException {
		return is.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return is.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return is.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return is.skip(n);
	}

	@Override
	public int available() throws IOException {
		return is.available();
	}

	@Override
	public void close() throws IOException {
		is.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		is.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		is.reset();
	}

	@Override
	public boolean markSupported() {
		return is.markSupported();
	}

	public long getContentLength() {
		return contentLength;
	}

	public String getCharset() {
		return charset;
	}

}
