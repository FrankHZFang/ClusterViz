package server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.*;

public class SimpleServer {
	private static HttpServer server;

	public static void start(int port, boolean debugMode) {
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
			if (debugMode) {
				System.out.println("server started at " + port);
			}
			server.createContext("/", new RootHandler(debugMode));
			server.setExecutor(null);
			server.start();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		SimpleServer.start(80, true);
	}
}
