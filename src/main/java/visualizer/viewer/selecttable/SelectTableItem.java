package visualizer.viewer.selecttable;

import javafx.beans.property.*;

public class SelectTableItem<T> {
	public final ObjectProperty<T> object = new SimpleObjectProperty<>();
	public final ObjectProperty<String> name = new SimpleObjectProperty<>();
	public final IntegerProperty size = new SimpleIntegerProperty();
	public final BooleanProperty isAdded = new SimpleBooleanProperty(false);
}
