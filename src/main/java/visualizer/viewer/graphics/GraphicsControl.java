package visualizer.viewer.graphics;

import javafx.scene.Node;

public interface GraphicsControl {
	Node getPanel();

	void updateDrawing(String graphicsJson);
}
