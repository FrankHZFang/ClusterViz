package visualizer.data;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.*;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.Set;

public class BufferedObservableSet<T> extends BufferedObservable {
	private ObservableSet<T> bufferedSet;
	private BooleanProperty sourceChanged;

	public Set<T> getBufferedSet() {
		return bufferedSet;
	}

	public BufferedObservableSet(ObservableSet<T> source, int bufferUpdateMilli) {
		this.bufferedSet = FXCollections.observableSet(new HashSet<>(source));
		sourceChanged = new SimpleBooleanProperty(false);

		source.addListener((SetChangeListener<? super T>) c -> sourceChanged.setValue(true));

		Timeline timeline = new Timeline(
				new KeyFrame(Duration.millis(bufferUpdateMilli), ae -> {
					if (sourceChanged.get()) {
						bufferedSet.clear();
						bufferedSet.addAll(source);
						sourceChanged.setValue(false);
						state.setValue(state.get() + 1);
					}
				}));
		timeline.setCycleCount(-1);
		timeline.play();
	}

	public BufferedObservableSet(ObservableSet<T> source) {
		this(source, DEFAULT_UPDATE_INTERVAL_MILLI);
	}
}
