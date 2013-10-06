package bmstu.tp.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bmstu.tp.webserver.http.Method;
import bmstu.tp.webserver.http.Request;
import bmstu.tp.webserver.http.RequestFormatException;
import bmstu.tp.webserver.http.Response;


public class Worker implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(Worker.class);
	private Socket socket;
	private Request request = new Request();
	public Response response = new Response();
	
	public Worker(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try(BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				OutputStream socketWriter = socket.getOutputStream()) {
				readRequest(socketReader);
				try {
					if(request.getMethod() == Method.POST) {
						response.setStatusCode(400);
						sendResponse(socketWriter, "POST method not supported");
						return;
					}
					if(request.getUri().contains("..")) {
						response.setStatusCode(400);
						sendResponse(socketWriter, "Nope!");
						return;
					}
					boolean headersOnly = request.getMethod() == Method.HEAD;
					File file = FileServer.getFile(request.getUri(), headersOnly);
					sendResponse(socketWriter, file);
				} catch(FileNotFoundException e) {
					response.setStatusCode(404);
					sendResponse(socketWriter, "File not found");
				} catch(ForbiddenException e) {
					response.setStatusCode(403);
					sendResponse(socketWriter, "Forbidden");
				}
			} catch (Throwable e) {
				LOG.warn("Exception:", e);
			}
		LOG.debug("Closing socket, terminating thread");
	}
	
	private void sendResponse(OutputStream socketWriter, File file) {
		byte[] body = file.getContents();
		int length = file.getLength();
		response.setBody(body);
		response.setHeader("Server", "My cool java web server");
		response.setHeader("Date", getServerTime());
		response.setHeader("Connection", "closed");
		response.setHeader("Content-Type", file.getContentType());
		response.setHeader("Content-Length", String.valueOf(length));
		LOG.debug("Content-Length for {}: {}", request.getUri(), length);
		byte[] headers = response.getHeaders().getBytes();
		try {
			socketWriter.write(headers, 0, headers.length);
			socketWriter.write(body, 0, body.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendResponse(OutputStream socketWriter, String msg) {
		try {
			byte body[] = msg.getBytes("UTF-8");
			sendResponse(socketWriter, new File(body, body.length, "Text"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	private void readRequest(BufferedReader socketReader) throws IOException, RequestFormatException {
		readHeaders(socketReader);
		String contentLength = request.getHeader("Content-Length");
		if(contentLength != null) {
			readBody(socketReader, Integer.parseInt(contentLength));
		}
	}
	
	private void readHeaders(BufferedReader socketReader) throws IOException, RequestFormatException {
		LOG.debug("Reading request headers");
		String inputLine = socketReader.readLine();
		LOG.debug(inputLine);
		request.parseInitialLine(inputLine);
		while ((inputLine = socketReader.readLine()) != null && !inputLine.isEmpty()) {
			request.parseHeaderLine(inputLine);
		}
	}
	
	private void readBody(BufferedReader socketReader, int length) throws IOException {
		LOG.debug("Reading request body");
		char[] body = new char[length];
		socketReader.read(body, 0, length);
		request.setBody(String.valueOf(body).getBytes());
	}
	
	private String getServerTime() {
	    Calendar calendar = Calendar.getInstance();
	    SimpleDateFormat dateFormat = new SimpleDateFormat(
	        "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return dateFormat.format(calendar.getTime());
	}
}
