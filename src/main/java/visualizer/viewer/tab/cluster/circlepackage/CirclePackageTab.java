package visualizer.viewer.tab.cluster.circlepackage;

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
import visualizer.dv8.utilities.DependencyUtilities;
import visualizer.viewer.ContentTabControl;
import visualizer.viewer.FileChooserManager;
import visualizer.viewer.clustergroup.*;
import visualizer.viewer.graphics.D3jsControl;
import visualizer.viewer.graphics.GraphicsControl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static visualizer.dv8.utilities.ClusterUtilities.ItemStream;

public class CirclePackageTab extends ContentTabControl {
	private TreeTableView<ClusterGroupSelectTableItem> table;
	private ClusterGroupSelectTableModel tableModel;

	private ListView<String> fileClusterContentListView;
	private ObservableList<String> fileClusterContentList;

	private ListView<String> visNodeContentListView;
	private TreeView<ClusterGroupNode> visNodeTree;

	private ClusterGroupDependencyTypeTable clusterGroupDependencyTypeTable;

	private CirclePackageDrawingModel visTree;

	private GraphicsControl graphicsControl;

	private SplitPane viewPane;

	private ClusterGroup selectedFileCluster;

	public CirclePackageTab(String name, Clustering rootCluster) {
		super(name);
		tableModel = new ClusterGroupSelectTableModel();
		visTree = new CirclePackageDrawingModel(tableModel.bufferedAddedObjectSet());

		graphicsControl = new D3jsControl(visTree);

		selectedFileCluster = null;
		tableModel.loadClustering(rootCluster);

		initInnerPanel();
		initInnerListeners();

		setContent(viewPane);
	}

	private Control createLeftTablePanel() {
		TabPane inputTablePane = new TabPane();

		table = new ClusterGroupTreeTableView();
		table.showRootProperty().setValue(false);
		clusterGroupDependencyTypeTable = new ClusterGroupDependencyTypeTable();

		fileClusterContentList = FXCollections.observableList(new ArrayList<>());
		fileClusterContentListView = new ListView<>(fileClusterContentList);

		Tab fileClusterTab = new Tab("File Cluster", table);
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

	private Control createRightPane() {
		visNodeContentListView = new ListView<>();
		visNodeContentListView.setCellFactory(cell -> new ListCell<String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				setText(item);
				setTooltip(new Tooltip(cell.itemsProperty().get().get(0)));
			}
		});

		Tab visNodeContentListTab = new Tab("Selected node contents", visNodeContentListView);
		visNodeContentListTab.closableProperty().set(false);

		visNodeTree = new TreeView<>();

		Tab nodeTreeTab = new Tab("Selected node list", visNodeTree);
		nodeTreeTab.closableProperty().set(false);

		TabPane tabPane = new TabPane();
		tabPane.getTabs().addAll(nodeTreeTab);

		return tabPane;
	}

	private void initInnerPanel() {

		viewPane = new SplitPane();

		Control leftPane = createLeftTablePanel();
		Control rightPane = createRightPane();
		BorderPane graphics = new BorderPane();
		graphics.setCenter(graphicsControl.getPanel());

		viewPane.getItems().addAll(leftPane, graphics, rightPane);
		SplitPane.setResizableWithParent(leftPane, false);
		SplitPane.setResizableWithParent(rightPane, false);
		viewPane.setDividerPositions(0.25f, 0.75f);
	}

	private TreeItem<ClusterGroupNode> prepareVisNodeTreeRoot() {
		ClusterGroupNode root = visTree.getRoot();
		TreeItem<ClusterGroupNode> rootItem = new TreeItem<>(root);
		prepareVisNodeItem(rootItem);
		return rootItem;
	}

	private void prepareVisNodeItem(TreeItem<ClusterGroupNode> item) {
		ClusterGroupNode root = item.getValue();
		for (ClusterGroupNode child : root.childNodes) {
			TreeItem<ClusterGroupNode> childItem = new TreeItem<>(child);
			item.getChildren().add(childItem);
			prepareVisNodeItem(childItem);
		}
	}

	private void initInnerListeners() {
		table.rootProperty().bind(tableModel.rootContainer());

		visTree.currentState().addListener((updated, oldValue, newValue) -> {
					visNodeTree.setRoot(prepareVisNodeTreeRoot());
				}
		);

		visNodeTree.setCellFactory((treeView) -> new TreeCell<ClusterGroupNode>() {
			@Override
			protected void updateItem(ClusterGroupNode item, boolean empty) {
				super.updateItem(item, empty);
				if (item != null) {
					setText(item.name);
				} else {
					setText(null);
				}
			}
		});

		table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				selectedFileCluster = newValue.getValue().object.get();
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
			DependencyMatrix<ClusterGroup> clusterGroupDependencyMatrix
					= DependencyUtilities.clusterGroupDependencyMatrix(matrix, tableModel.getClustering());

			clusterGroupDependencyTypeTable.loadDependencyMatrix(clusterGroupDependencyMatrix,
					tableModel.bufferedAddedObjectSet());
			visTree.loadDependencyMatrix(clusterGroupDependencyMatrix,
					clusterGroupDependencyTypeTable.bufferedDisplayedDependencyTypes());
		}
	}

	@Override
	public List<Menu> getToolBarMenus() {
		ArrayList<Menu> menuList = new ArrayList<>();

		Menu viewOperations = new Menu("View");
		MenuItem addAllItems = new MenuItem("Add all Cluster Groups");
		addAllItems.setOnAction((ActionEvent event) -> {
			tableModel.addAllToGraphics();
		});

		MenuItem removeAllItems = new MenuItem("Remove all Cluster Groups");
		removeAllItems.setOnAction((ActionEvent event) -> {
			tableModel.removeAllFromGraphics();
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
