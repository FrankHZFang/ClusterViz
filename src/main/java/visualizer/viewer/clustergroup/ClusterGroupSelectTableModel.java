package visualizer.viewer.clustergroup;

import com.archdia.dv8.cluster.ClusterComponent;
import com.archdia.dv8.cluster.ClusterGroup;
import com.archdia.dv8.cluster.Clustering;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import visualizer.viewer.selecttable.SelectTableModel;

import java.util.Collection;

public class ClusterGroupSelectTableModel extends SelectTableModel<ClusterGroup> {
	private Clustering clustering;

	public Clustering getClustering() {
		return clustering;
	}

	private ObjectProperty<TreeItem<ClusterGroupSelectTableItem>> rootContainer;

	public ObservableValue<TreeItem<ClusterGroupSelectTableItem>> rootContainer() {
		return rootContainer;
	}

	public ClusterGroupSelectTableModel() {
		super();
		rootContainer = new SimpleObjectProperty<>(new TreeItem<>(null));
	}

	public void loadClustering(Clustering clustering) {
		this.clustering = clustering;
		rootContainer.set(createTree(clustering.getChildren()));
	}

	private TreeItem<ClusterGroupSelectTableItem> createTree(Collection<ClusterComponent> c) {
		TreeItem<ClusterGroupSelectTableItem> treeItem = new TreeItem<>();
		c.stream()
				.filter(clusterComponent -> clusterComponent instanceof ClusterGroup)
				.forEach(clusterComponent ->
						treeItem.getChildren().add(recursivelyProcessTree((ClusterGroup) clusterComponent))
				);
		return treeItem;
	}

	private TreeItem<ClusterGroupSelectTableItem> recursivelyProcessTree(ClusterGroup c) {
		ClusterGroupSelectTableItem item = new ClusterGroupSelectTableItem(c);
		itemList.add(item);
		initItemListener(item);

		TreeItem<ClusterGroupSelectTableItem> treeItem = new TreeItem<>(item);
		c.getChildren().stream()
				.filter(clusterComponent -> clusterComponent instanceof ClusterGroup)
				.forEach(clusterComponent ->
						treeItem.getChildren().add(recursivelyProcessTree((ClusterGroup) clusterComponent))
				);
		return treeItem;
	}
}
