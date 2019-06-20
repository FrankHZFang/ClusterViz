package visualizer.viewer.tab.cluster.circlepackage;

import com.archdia.dv8.cluster.ClusterGroup;
import com.archdia.dv8.matrix.DependencyMatrix;
import com.archdia.dv8.matrix.DependencyType;
import visualizer.viewer.graphics.AbstractDrawingModel;
import visualizer.data.BufferedObservableSet;
import visualizer.dv8.utilities.ClusterUtilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CirclePackageDrawingModel extends AbstractDrawingModel {

	private static final String FOLDER_NAME = "circle_package";
	private static final String ROOT_NAME = "ROOT";

	private final ClusterGroupNode root = new ClusterGroupNode(ROOT_NAME, 0);

	public ClusterGroupNode getRoot() {
		return root;
	}

	private String graphicsJson = null;

	@Override
	public String getGraphicsJson() {
		return graphicsJson;
	}

	@Override
	public String getFolderName() {
		return FOLDER_NAME;
	}

	private Set<ClusterGroup> acceptedClusterGroup;
	private Set<DependencyType> acceptedDependencyType;

	private Map<ClusterGroup, ClusterGroupNode> clusterGroupVisNodeMap;
	private DependencyMatrix<ClusterGroup> dependencyMatrix;

	public CirclePackageDrawingModel(BufferedObservableSet<ClusterGroup> acceptedClusters) {
		clusterGroupVisNodeMap = new HashMap<>();
		acceptedClusterGroup = acceptedClusters.getBufferedSet();

		acceptedClusters.bufferChanged().addListener(
				(observable, oldValue, newValue) -> constructTree()
		);
	}


	private void constructTree() {
		clearClusters();

		acceptedClusterGroup.forEach(this::createVisNode);
		acceptedClusterGroup.forEach(this::addVisNodeChildren);

		refreshDependencies();

		graphicsJson = mapJson(root);
		updateState();
	}

	private void clearVisNodeDependencies() {
		clusterGroupVisNodeMap.values().forEach(visNode -> visNode.dependencies.clear());
	}

	private void refreshDependencies() {
		if (dependencyMatrix != null) {
			acceptedClusterGroup.forEach(this::refreshAcceptedGroup);
		}
	}

	private void refreshAcceptedGroup(ClusterGroup cg) {
		ClusterUtilities.createCombinedClusterGroup(cg, acceptedClusterGroup)
				.map(source -> dependencyMatrix.getVariables().indexOf(source))
				.flatMap(row -> dependencyMatrix.get(row).stream())
				.filter(location ->
						fromDifferentCombinedGroup(
								dependencyMatrix.getVariable(location.getRow()),
								dependencyMatrix.getVariable(location.getColumn()),
								acceptedClusterGroup
						)
				)
				.flatMap(location -> dependencyMatrix.get(location).stream())
				.filter(dep -> acceptedDependencyType.contains(dep.getType()))
				.forEach(dep -> {
							ClusterGroupDependency cgDep =
									new ClusterGroupDependency(findVisNodeSelfOrParent(dep.getTarget()), dep.getType().getName());
							findVisNodeSelfOrParent(dep.getSource()).dependencies.add(cgDep);
						}
				);
	}

	private boolean fromDifferentCombinedGroup
			(ClusterGroup source, ClusterGroup target, Set<ClusterGroup> acceptedClusterGroup) {
		ClusterGroup top = ClusterUtilities.findAcceptedSelfOrParent(target.getParent(), acceptedClusterGroup);
		return top != null && top != source;
	}

	private void clearClusters() {
		clusterGroupVisNodeMap.clear();
		root.childNodes.clear();
	}

	private void createVisNode(ClusterGroup cluster) {
		ClusterGroupNode clusterGroupNode = new ClusterGroupNode(cluster);
		clusterGroupVisNodeMap.put(cluster, clusterGroupNode);
	}

	private void addVisNodeChildren(ClusterGroup cluster) {
		ClusterGroupNode parentNode = findVisNodeParent(cluster);
		ClusterGroupNode clusterGroupNode = clusterGroupVisNodeMap.get(cluster);
		if (parentNode == null) {
			root.childNodes.add(clusterGroupNode);
		} else {
			parentNode.childNodes.add(clusterGroupNode);
		}
	}

	private ClusterGroupNode findVisNodeParent(ClusterGroup cluster) {
		return clusterGroupVisNodeMap.get(
				ClusterUtilities.findAcceptedParent(cluster, clusterGroupVisNodeMap.keySet()));
	}

	private ClusterGroupNode findVisNodeSelfOrParent(ClusterGroup cluster) {
		return clusterGroupVisNodeMap.get(
				ClusterUtilities.findAcceptedSelfOrParent(cluster, clusterGroupVisNodeMap.keySet()));
	}

	public void loadDependencyMatrix(DependencyMatrix<ClusterGroup> dm, BufferedObservableSet<DependencyType> acceptedTypes) {
		dependencyMatrix = dm;
		acceptedDependencyType = acceptedTypes.getBufferedSet();

		refreshDependencies();
		updateState();

		acceptedTypes.bufferChanged().addListener(
				(observable, oldValue, newValue) -> {
					clearVisNodeDependencies();
					refreshDependencies();
					graphicsJson = mapJson(root);
					updateState();
				}
		);
	}

	private String mapJson(ClusterGroupNode rootNode) {
		try {
			return ClusterGroupNodeToD3JSJson.serialize(rootNode);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.err.println("json process failed");
			return null;
		}
	}
}
