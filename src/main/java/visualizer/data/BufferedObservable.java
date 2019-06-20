package visualizer.data;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ObservableLongValue;

public class BufferedObservable<T> {
	protected static final int DEFAULT_UPDATE_INTERVAL_MILLI = 1000;
	protected LongProperty state = new SimpleLongProperty();

	public ObservableLongValue bufferChanged() {
		return state;
	}
}
