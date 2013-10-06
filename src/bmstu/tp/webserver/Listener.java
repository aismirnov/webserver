package bmstu.tp.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Listener {
	private static final Logger LOG = LoggerFactory.getLogger(Listener.class);
	private static final ExecutorService ES = Executors.newCachedThreadPool();
	
	public static void main(String[] args) {
		int port = 4080;
		try(ServerSocket serverSocket = new ServerSocket(port)) {
			while(true) {
				try {
					Socket clientSocket = serverSocket.accept();
					LOG.debug("Connetion accepted, starting new thread");
					ES.submit(new Worker(clientSocket));
				} catch (IOException e) {
				    LOG.error("Accept failed");
				}
			}
		} catch (IOException e) {
		    LOG.error("Could not listen on port: {}", port);
		    System.exit(-1);
		}
	}
}
