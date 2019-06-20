package visualizer;

import javafx.application.Application;
import server.SimpleServer;
import visualizer.viewer.MainWindowControl;

public class Launcher {
	public static void main(String[] args) {
		SimpleServer.start(14285, false);
		Application.launch(MainWindowControl.class, args);
	}
}