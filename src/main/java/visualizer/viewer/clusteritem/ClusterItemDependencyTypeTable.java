package visualizer.viewer.clusteritem;

import com.archdia.dv8.cluster.ClusterComponent;
import com.archdia.dv8.cluster.ClusterGroup;
import com.archdia.dv8.matrix.DependencyMatrix;
import com.archdia.dv8.matrix.DependencyType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import visualizer.data.BufferedObservableSet;
import visualizer.dv8.utilities.ClusterUtilities;
import visualizer.dv8.utilities.DependencyUtilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ClusterItemDependencyTypeTable extends TableView<ClusterItemDependencyTypeTableItem> {
	private ObservableSet<DependencyType> displayedDependencyTypes;

	private BufferedObservableSet<DependencyType> bufferedDisplayedDependencyTypes;

	public BufferedObservableSet<DependencyType> bufferedDisplayedDependencyTypes() {
		return bufferedDisplayedDependencyTypes;
	}

	private HashMap<DependencyType, ClusterItemDependencyTypeTableItem> typeItemMap;

	public ClusterItemDependencyTypeTable() {
		super();

		displayedDependencyTypes = FXCollections.observableSet(new HashSet<>());
		bufferedDisplayedDependencyTypes = new BufferedObservableSet<>(displayedDependencyTypes);

		this.editableProperty().set(true);
		TableColumn<ClusterItemDependencyTypeTableItem, Boolean> displayColumn = new TableColumn<>();
		TableColumn<ClusterItemDependencyTypeTableItem, String> nameColumn = new TableColumn<>("Type");
		TableColumn<ClusterItemDependencyTypeTableItem, Integer> totalCountColumn = new TableColumn<>("Total");
		TableColumn<ClusterItemDependencyTypeTableItem, Integer> currentExternalColumn = new TableColumn<>("External");
		this.getColumns().add(displayColumn);
		this.getColumns().add(nameColumn);
		this.getColumns().add(totalCountColumn);
		this.getColumns().add(currentExternalColumn);

		typeItemMap = new HashMap<>();

		nameColumn.setCellValueFactory((item) -> item.getValue().name);
		nameColumn.editableProperty().set(false);

		displayColumn.setCellValueFactory((item) -> item.getValue().displayed);
		displayColumn.setCellFactory(CheckBoxTableCell.forTableColumn(displayColumn));
		displayColumn.setPrefWidth(24);

		totalCountColumn.setCellValueFactory((item) -> item.getValue().totalCount.asObject());
		totalCountColumn.editableProperty().set(false);
		totalCountColumn.setPrefWidth(50);

		currentExternalColumn.setCellValueFactory((item) -> item.getValue().currentExternal.asObject());
		currentExternalColumn.editableProperty().set(false);
		currentExternalColumn.setPrefWidth(50);
	}

	public void loadDependencyMatrix
			(DependencyMatrix<String> matrix, BufferedObservableSet<ClusterGroup> acceptedClusters) {
		itemsProperty().get().clear();
		typeItemMap.clear();

		matrix.getDependencyTypes().stream()
				.map(ClusterItemDependencyTypeTableItem::new)
				.forEach(item -> {
							item.totalCount.set
									((int) DependencyUtilities.countTotalOccurrences(matrix, item.type));
							item.displayed.addListener((updated, oldValue, newValue) -> {
								if (newValue) {
									displayedDependencyTypes.add(item.type);
								} else {
									displayedDependencyTypes.remove(item.type);
								}
							});
							this.itemsProperty().get().add(item);
							typeItemMap.put(item.type, item);
						}
				);

		acceptedClusters.bufferChanged().addListener(
				(observable, oldValue, newValue) -> {
					refreshCurrentExternalDependencyCount(matrix, acceptedClusters.getBufferedSet());
				}
		);
	}

	private void refreshCurrentExternalDependencyCount(DependencyMatrix<String> matrix,
	                                                   Collection<ClusterGroup> acceptedGroups) {
		itemsProperty().get().forEach(item -> item.currentExternal.setValue(0));

		Set<String> acceptedItemNames = acceptedGroups.stream()
				.flatMap(ClusterUtilities::collectAllDescendantItems)
				.map(ClusterComponent::getName)
				.collect(Collectors.toSet());

		matrix.getAllDependencies().stream()
				.flatMap(Collection::stream)
				.forEach(dependency -> {
					if (acceptedItemNames.contains(dependency.getSource())
							&& acceptedItemNames.contains(dependency.getTarget())) {
						ClusterItemDependencyTypeTableItem item = typeItemMap.get(dependency.getType());
						item.currentExternal.setValue(item.currentExternal.get() + 1);
					}
				});
	}

	public void addAllDependencies() {
		this.itemsProperty().get().stream()
				.map(clusterGroupDependencyTypeTableItem -> clusterGroupDependencyTypeTableItem.displayed)
				.forEach(displayed -> displayed.setValue(true));
	}

	public void removeAllDependencies() {
		this.itemsProperty().get().stream()
				.map(clusterGroupDependencyTypeTableItem -> clusterGroupDependencyTypeTableItem.displayed)
				.forEach(displayed -> displayed.setValue(false));
	}
}
