package visualizer.viewer.tab.set.general;

import com.archdia.dv8.cluster.ClusterComponent;
import com.archdia.dv8.cluster.ClusterGroup;
import com.archdia.dv8.cluster.Clustering;
import com.archdia.dv8.matrix.DependencyMatrix;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import visualizer.viewer.ContentTabControl;
import visualizer.viewer.FileChooserManager;
import visualizer.viewer.clustergroup.ClusterGroupSelectTableItem;
import visualizer.viewer.selecttable.SelectTableItem;
import visualizer.viewer.selecttable.SelectTableModel;
import visualizer.viewer.clusteritem.ClusterItemDependencyTypeTable;
import visualizer.viewer.graphics.D3jsControl;
import visualizer.viewer.graphics.GraphicsControl;
import visualizer.viewer.selecttable.SelectTableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static visualizer.dv8.utilities.ClusterUtilities.ItemStream;

public class GeneralSetViewTab extends ContentTabControl {

	private TableView<SelectTableItem<ClusterGroup>> fileClusterTable;
	private SelectTableModel<ClusterGroup> fileClusterTableModel;

	private ObservableList<String> fileClusterContentList;

	private ClusterItemDependencyTypeTable clusterGroupDependencyTypeTable;

	private SetNodeDrawingModel nodeModel;

	private GraphicsControl graphicsControl;

	private SplitPane viewPane;

	private ClusterGroup selectedFileCluster;

	public GeneralSetViewTab(String name, Clustering rootCluster) {
		super(name);

		Collection<ClusterGroupSelectTableItem> items = rootCluster.getChildren().stream()
				.filter(clusterComponent -> clusterComponent instanceof ClusterGroup)
				.map(clusterGroup -> new ClusterGroupSelectTableItem((ClusterGroup) clusterGroup))
				.collect(Collectors.toSet());

		fileClusterTableModel = new SelectTableModel<>(items);

		nodeModel = new SetNodeDrawingModel(fileClusterTableModel.bufferedAddedObjectSet());

		graphicsControl = new D3jsControl(nodeModel);

		selectedFileCluster = null;

		initInnerPanel();
		initInnerListeners();

		fileClusterTable.getItems().addAll(items);

		setContent(viewPane);
	}

	private Control createLeftTablePanel() {
		TabPane inputTablePane = new TabPane();

		fileClusterTable = new SelectTableView<>();

		clusterGroupDependencyTypeTable = new ClusterItemDependencyTypeTable();

		fileClusterContentList = FXCollections.observableList(new ArrayList<>());
		ListView<String> fileClusterContentListView = new ListView<>(fileClusterContentList);

		Tab fileClusterTab = new Tab("File Cluster", fileClusterTable);
		fileClusterTab.closableProperty().setValue(false);
		Tab dependencyTypeTablePane = new Tab("Dependency Types", clusterGroupDependencyTypeTable);
		dependencyTypeTablePane.closableProperty().setValue(false);

		inputTablePane.getTabs().add(fileClusterTab);
		inputTablePane.getTabs().add(dependencyTypeTablePane);

		SplitPane splitPane = new SplitPane();
		splitPane.orientationProperty().set(Orientation.VERTICAL);
		splitPane.getItems().addAll(inputTablePane, fileClusterContentListView);

		return splitPane;
	}

	private void initInnerPanel() {
		viewPane = new SplitPane();

		Control leftPane = createLeftTablePanel();
		BorderPane graphics = new BorderPane();
		graphics.setCenter(graphicsControl.getPanel());

		viewPane.getItems().addAll(leftPane, graphics);
		SplitPane.setResizableWithParent(leftPane, false);
		viewPane.setDividerPositions(0.25f, 0.75f);
	}


	private void initInnerListeners() {
		fileClusterTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				selectedFileCluster = newValue.object.get();
				fileClusterContentList.setAll(
						ItemStream(selectedFileCluster)
								.map(ClusterComponent::getName).collect(Collectors.toList())
				);
			} else {
				fileClusterContentList.clear();
			}
		});
	}

	private void loadDSM() {
		DependencyMatrix<String> matrix = FileChooserManager.loadDv8DepMatrix();
		if (matrix != null) {
			clusterGroupDependencyTypeTable.loadDependencyMatrix(matrix,
					fileClusterTableModel.bufferedAddedObjectSet());
			nodeModel.loadDependencyMatrix(matrix,
					clusterGroupDependencyTypeTable.bufferedDisplayedDependencyTypes());
		}
	}

	@Override
	public List<Menu> getToolBarMenus() {
		ArrayList<Menu> menuList = new ArrayList<>();

		Menu viewOperations = new Menu("View");
		MenuItem addAllItems = new MenuItem("Add all Cluster Groups");
		addAllItems.setOnAction((ActionEvent event) -> {
			fileClusterTableModel.addAllToGraphics();
		});

		MenuItem removeAllItems = new MenuItem("Remove all Cluster Groups");
		removeAllItems.setOnAction((ActionEvent event) -> {
			fileClusterTableModel.removeAllFromGraphics();
		});
		viewOperations.getItems().addAll(addAllItems, removeAllItems);

		Menu dependencyMenu = new Menu("Dependencies");
		MenuItem loadDSMItem = new MenuItem("load DSM...");
		MenuItem addAllDependencyItem = new MenuItem("Add all Dependencies");
		MenuItem removeAllDependencyItem = new MenuItem("Remove all Dependencies");

		loadDSMItem.setOnAction((ActionEvent event) -> {
			loadDSM();
		});

		addAllDependencyItem.setOnAction((ActionEvent event) -> {
			clusterGroupDependencyTypeTable.addAllDependencies();
		});

		removeAllDependencyItem.setOnAction((ActionEvent event) -> {
			clusterGroupDependencyTypeTable.removeAllDependencies();
		});

		dependencyMenu.getItems().addAll(loadDSMItem, addAllDependencyItem, removeAllDependencyItem);

		menuList.add(viewOperations);
		menuList.add(dependencyMenu);

		return menuList;
	}
}
