package bmstu.tp.webserver.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Request extends Packet {
	private Method method;
	private String uri;
	
	@Override
	public String toString() {
		return String.format("%s %s %s", method, uri, version);
	}
	
	public void parseInitialLine(String line) throws RequestFormatException {
		method = Enum.valueOf(Method.class, line.substring(0, line.indexOf(' ')).toUpperCase());
		uri = line.substring(line.indexOf(' ') + 1, line.lastIndexOf(' '));
		if(uri.contains("?")) {
			uri = uri.split("\\?")[0];
		}
		switch(line.substring(line.lastIndexOf(' ') + 1).toUpperCase()) {
			case "HTTP/1.0":
				version = Version.HTTP10;
				break;
			case "HTTP/1.1":
				version = Version.HTTP11;
				break;
			default:
				throw new RequestFormatException();
		}
	}
	
	public void parseHeaderLine(String line) {
		String[] params = line.split(": ");
		headers.put(params[0], params[1]);
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public String getUri() {
		try {
			return URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
