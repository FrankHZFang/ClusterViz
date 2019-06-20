package visualizer.viewer;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.Tab;

import java.util.List;

public abstract class ContentTabControl extends Tab {
	protected Node content;

	public ContentTabControl(String name) {
		super(name);
	}

	public abstract List<Menu> getToolBarMenus();
}
