package visualizer.viewer.clustergroup;

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

import java.util.*;
import java.util.stream.Collectors;

public class ClusterGroupDependencyTypeTable extends TableView<ClusterGroupDependencyTypeTableItem> {
	private ObservableSet<DependencyType> displayedDependencyTypes;

	public ObservableSet<DependencyType> displayedTypes() {
		return displayedDependencyTypes;
	}

	private BufferedObservableSet<DependencyType> bufferedDisplayedDependencyTypes;

	public BufferedObservableSet<DependencyType> bufferedDisplayedDependencyTypes() {
		return bufferedDisplayedDependencyTypes;
	}

	public ClusterGroupDependencyTypeTable() {
		super();

		displayedDependencyTypes = FXCollections.observableSet(new HashSet<>());
		bufferedDisplayedDependencyTypes = new BufferedObservableSet<>(displayedDependencyTypes);

		this.editableProperty().set(true);
		TableColumn<ClusterGroupDependencyTypeTableItem, Boolean> displayColumn = new TableColumn<>();
		TableColumn<ClusterGroupDependencyTypeTableItem, String> nameColumn = new TableColumn<>("Type");
		TableColumn<ClusterGroupDependencyTypeTableItem, Integer> totalCountColumn = new TableColumn<>("Total");
		TableColumn<ClusterGroupDependencyTypeTableItem, Integer> currentInternalColumn = new TableColumn<>("Internal");
		TableColumn<ClusterGroupDependencyTypeTableItem, Integer> currentExternalColumn = new TableColumn<>("External");
		this.getColumns().add(displayColumn);
		this.getColumns().add(nameColumn);
		this.getColumns().add(totalCountColumn);
		this.getColumns().add(currentInternalColumn);
		this.getColumns().add(currentExternalColumn);

		nameColumn.setCellValueFactory((item) -> item.getValue().name);
		nameColumn.editableProperty().set(false);

		displayColumn.setCellValueFactory((item) -> item.getValue().displayed);
		displayColumn.setCellFactory(CheckBoxTableCell.forTableColumn(displayColumn));
		displayColumn.setPrefWidth(24);

		totalCountColumn.setCellValueFactory((item) -> item.getValue().totalCount.asObject());
		totalCountColumn.editableProperty().set(false);
		totalCountColumn.setPrefWidth(50);

		currentInternalColumn.setCellValueFactory((item) -> item.getValue().currentInternal.asObject());
		currentInternalColumn.editableProperty().set(false);
		currentInternalColumn.setPrefWidth(50);

		currentExternalColumn.setCellValueFactory((item) -> item.getValue().currentExternal.asObject());
		currentExternalColumn.editableProperty().set(false);
		currentExternalColumn.setPrefWidth(50);
	}

	public void loadDependencyMatrix
			(DependencyMatrix<ClusterGroup> matrix, BufferedObservableSet<ClusterGroup> acceptedClusters) {
		this.itemsProperty().get().clear();
		matrix.getDependencyTypes().stream()
				.map(ClusterGroupDependencyTypeTableItem::new)
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
						}
				);

		refreshCurrentInternalDependencyCount(matrix, acceptedClusters.getBufferedSet());
		acceptedClusters.bufferChanged().addListener(
				(observable, oldValue, newValue) -> {
					refreshCurrentInternalDependencyCount(matrix, acceptedClusters.getBufferedSet());
					refreshCurrentExternalDependencyCount(matrix, acceptedClusters.getBufferedSet());
				}
		);
	}

	private void refreshCurrentInternalDependencyCount(DependencyMatrix<ClusterGroup> matrix, Collection<ClusterGroup> acceptedGroups) {
		itemsProperty().get().forEach(item ->
				item.currentInternal.setValue(acceptedGroups.stream()
						.map(cg -> ClusterUtilities.createCombinedClusterGroup(cg, acceptedGroups).collect(Collectors.toSet()))
						.mapToInt(combined -> (int) DependencyUtilities.countInternalOccurrences(matrix, combined, item.type))
						.sum()
				)
		);
	}

	private void refreshCurrentExternalDependencyCount(DependencyMatrix<ClusterGroup> matrix, Collection<ClusterGroup> acceptedGroups) {
		itemsProperty().get().forEach(item ->
				item.currentExternal.setValue(acceptedGroups.stream()
						.map(cg -> ClusterUtilities.createCombinedClusterGroup(cg, acceptedGroups).collect(Collectors.toSet()))
						.mapToInt(combined -> (int) DependencyUtilities.countInterClusterGroupOccurrences(matrix, combined, acceptedGroups, item.type))
						.sum()
				)
		);
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
