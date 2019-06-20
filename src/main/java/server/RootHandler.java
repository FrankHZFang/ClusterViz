package server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RootHandler implements HttpHandler {
	private final String HTML_PATH = "/html/";
	private boolean debugMode;

	public RootHandler(boolean debugMode) {
		this.debugMode = debugMode;
	}

	@Override
	public void handle(HttpExchange he) throws IOException {
		URI uri = he.getRequestURI();
		if (debugMode) {
			System.out.println(uri);
		}
		Path p;
		if (uri.getPath().equals("/")) {
			p = Paths.get(HTML_PATH, "/index.html");
		} else {
			p = Paths.get(HTML_PATH, uri.getPath());
		}

		String path = p.toString().replace('\\', '/');
		
		byte[] response = ResourceLoader.getResource(path);
		if (response == null) {
			he.sendResponseHeaders(404, -1);
		} else {
			he.sendResponseHeaders(200, 0);
			OutputStream os = he.getResponseBody();
			os.write(response);
			os.flush();
			os.close();
		}
	}
}