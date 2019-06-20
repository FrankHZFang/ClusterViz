package visualizer.viewer.tab.set.general;

import com.archdia.dv8.cluster.ClusterComponent;
import com.archdia.dv8.cluster.ClusterGroup;
import com.archdia.dv8.matrix.DependencyMatrix;
import com.archdia.dv8.matrix.DependencyType;
import visualizer.viewer.graphics.AbstractDrawingModel;
import visualizer.data.BufferedObservableSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SetNodeDrawingModel extends AbstractDrawingModel {
	private static final String FOLDER_NAME = "general_set_view";

	private Set<ClusterGroup> acceptedClusterGroup;
	private Set<DependencyType> acceptedDependencyType;

	private String graphicsJson = null;

	@Override
	public String getGraphicsJson() {
		return graphicsJson;
	}

	@Override
	public String getFolderName() {
		return FOLDER_NAME;
	}

	private Map<ClusterComponent, SetNode> clusterItemSetNodeMap;
	private DependencyMatrix<String> dependencyMatrix;

	public SetNodeDrawingModel(BufferedObservableSet<ClusterGroup> acceptedClusters) {
		clusterItemSetNodeMap = new HashMap<>();
		acceptedClusterGroup = acceptedClusters.getBufferedSet();

		acceptedClusters.bufferChanged().addListener(
				(observable, oldValue, newValue) -> constructTree()
		);
	}

	private void constructTree() {
		clearClusters();
		acceptedClusterGroup.forEach(this::createSetNode);
		refreshDependencies();
		mapJson();
	}


	private void createSetNode(ClusterGroup cluster) {
		cluster.getChildren().forEach((child) -> {
			SetNode node = clusterItemSetNodeMap.get(child);
			if (node == null) {
				node = new SetNode(child.getName());
				clusterItemSetNodeMap.put(child, node);
			}
			node.addParent(cluster);
		});
	}

	public void loadDependencyMatrix(DependencyMatrix<String> dm, BufferedObservableSet<DependencyType> acceptedTypes) {
		dependencyMatrix = dm;
		acceptedDependencyType = acceptedTypes.getBufferedSet();

		refreshDependencies();


		acceptedTypes.bufferChanged().addListener(
				(observable, oldValue, newValue) -> {
					clearVisNodeDependencies();
					refreshDependencies();
					mapJson();
				}
		);
	}

	private void clearVisNodeDependencies() {
		clusterItemSetNodeMap.values().forEach(node -> node.dependencies().clear());
	}

	private void refreshDependencies() {
		if (dependencyMatrix != null) {
			clusterItemSetNodeMap.keySet().forEach(source -> {
				int sourceIndex = dependencyMatrix.getVariables().indexOf(source.getName());
				clusterItemSetNodeMap.keySet().stream()
						.filter(target -> !(target == source))
						.forEach(target -> {
							int targetIndex = dependencyMatrix.getVariables().indexOf(target.getName());
							if (dependencyMatrix.get(sourceIndex, targetIndex).stream()
									.anyMatch(type ->
											acceptedDependencyType.contains(type.getType())
									)) {
								clusterItemSetNodeMap.get(source).addDependency(clusterItemSetNodeMap.get(target));
							}
						});
			});
		}
	}

	private void clearClusters() {
		clusterItemSetNodeMap.clear();
	}

	private void mapJson() {
		try {
			graphicsJson = SetNodeToGraphicsJson.serialize(acceptedClusterGroup, clusterItemSetNodeMap.values());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.err.println("json process failed");
			graphicsJson = null;
		}
		updateState();
	}
}
