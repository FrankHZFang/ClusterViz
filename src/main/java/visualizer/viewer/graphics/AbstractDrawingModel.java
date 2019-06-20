package visualizer.viewer.graphics;

import javafx.beans.property.*;
import javafx.beans.value.ObservableLongValue;

public abstract class AbstractDrawingModel {
	private final LongProperty state = new SimpleLongProperty(Long.MIN_VALUE);

	public ObservableLongValue currentState() {
		return state;
	}

	public abstract String getGraphicsJson();

	public abstract String getFolderName();

	protected void updateState() {
		state.set(state.get() + 1);
	}
}
