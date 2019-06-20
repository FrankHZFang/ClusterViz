package visualizer.data;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import java.util.ArrayList;

public class BufferedObservableValue<T> extends BufferedObservable {
	private ObjectProperty<T> bufferedValue;
	private BooleanProperty sourceChanged;

	public ObservableValue<T> getBufferedValue() {
		return bufferedValue;
	}

	public BufferedObservableValue(ObservableValue<T> source, int bufferUpdateMilli) {
		this.bufferedValue = new SimpleObjectProperty<>(source.getValue());
		sourceChanged = new SimpleBooleanProperty(false);

		source.addListener((value, oldV, newV) -> sourceChanged.setValue(true));

		Timeline timeline = new Timeline(
				new KeyFrame(Duration.millis(bufferUpdateMilli), ae -> {
					if (sourceChanged.get()) {
						bufferedValue.setValue(source.getValue());
						sourceChanged.setValue(false);
					}
				}));
		timeline.setCycleCount(-1);
		timeline.play();
	}

	public BufferedObservableValue(ObservableValue<T> source) {
		this(source, DEFAULT_UPDATE_INTERVAL_MILLI);
	}
}
