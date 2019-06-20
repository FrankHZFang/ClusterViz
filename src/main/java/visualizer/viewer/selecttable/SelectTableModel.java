package visualizer.viewer.selecttable;

import com.archdia.dv8.cluster.Clustering;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import visualizer.data.BufferedObservableSet;

import java.util.Collection;
import java.util.HashSet;

public class SelectTableModel<T> {
	protected ObservableList<SelectTableItem<T>> itemList;
	public ObservableSet<T> selectedObjectSet;

	private   BufferedObservableSet<T> bufferedAddedObjectSet;
	public BufferedObservableSet<T> bufferedAddedObjectSet() {
		return bufferedAddedObjectSet;
	}

	public SelectTableModel() {
		itemList = FXCollections.observableArrayList();
		selectedObjectSet = FXCollections.observableSet(new HashSet<>());

		bufferedAddedObjectSet = new BufferedObservableSet<>(selectedObjectSet);
	}

	public SelectTableModel(Collection<? extends SelectTableItem<T>> items) {
		this();
		itemList.addAll(items);
		itemList.forEach(this::initItemListener);
	}

	protected void initItemListener(SelectTableItem<T> item) {
		item.isAdded.addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				selectedObjectSet.add(item.object.get());
			} else {
				selectedObjectSet.remove(item.object.get());
			}
		});
	}

	public void addAllToGraphics() {
		itemList.forEach(item -> item.isAdded.set(true));
	}

	public void removeAllFromGraphics() {
		itemList.forEach(item -> item.isAdded.set(false));
	}
}
