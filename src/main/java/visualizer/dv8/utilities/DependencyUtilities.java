package visualizer.dv8.utilities;

import com.archdia.dv8.cluster.ClusterComponent;
import com.archdia.dv8.cluster.ClusterGroup;
import com.archdia.dv8.cluster.ClusterItem;
import com.archdia.dv8.cluster.Clustering;
import com.archdia.dv8.matrix.DependencyMatrix;
import com.archdia.dv8.matrix.DependencyType;
import com.archdia.dv8.matrix.internal.SimpleDependencyMatrix;
import com.archdia.dv8.matrix.internal.UnorderedDependencyMatrixBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DependencyUtilities {
	public static long countInternalOccurrences(
			DependencyMatrix<ClusterGroup> matrix,
			Collection<ClusterGroup> combined,
			DependencyType type) {
		return combined.stream()
				.filter(name -> matrix.getVariables().indexOf(name) != -1)
				.map(name -> matrix.getVariables().indexOf(name))
				.flatMap(index -> matrix.get(index).stream())
				.filter(location -> combined.contains(matrix.getVariables().get(location.getColumn())))
				.flatMap(location -> matrix.get(location).stream())
				.filter(stringDependency -> stringDependency.getType() == type)
				.count();
	}

	public static long countInterClusterGroupOccurrences(
			DependencyMatrix<ClusterGroup> matrix,
			Collection<ClusterGroup> combinedClusterGroups,
			Collection<ClusterGroup> acceptedClusterGroups,
			DependencyType type) {
		return combinedClusterGroups.stream()
				.filter(name -> matrix.getVariables().indexOf(name) != -1)
				.map(name -> matrix.getVariables().indexOf(name))
				.flatMap(index -> matrix.get(index).stream())
				.filter(location -> {
							ClusterGroup target = matrix.getVariables().get(location.getColumn());
							ClusterGroup acceptedParent = ClusterUtilities.findAcceptedSelfOrParent(target, acceptedClusterGroups);
							return acceptedParent != null && !combinedClusterGroups.contains(target);
						}
				)
				.flatMap(location -> matrix.get(location).stream())
				.filter(stringDependency -> stringDependency.getType() == type)
				.count();
	}

	public static long countTotalOccurrences(DependencyMatrix<?> matrix, DependencyType type) {
		return matrix.getAllDependencies().stream()
				.flatMap(Collection::stream)
				.filter(stringDependency -> stringDependency.getType() == type)
				.distinct().count();
	}

	public static SimpleDependencyMatrix<ClusterGroup> clusterGroupDependencyMatrix
			(DependencyMatrix<String> matrix, Clustering clustering) {
		List<ClusterGroup> groupArrayList =
				ClusterUtilities.collectAllDescendantGroups(clustering)
						.collect(Collectors.toList());

		Map<String, ClusterItem> nameToItemMap =
				ClusterUtilities.collectAllDescendantItems(clustering)
						.collect(Collectors.toMap(ClusterComponent::getName, item -> item));

		UnorderedDependencyMatrixBuilder<ClusterGroup> builder = new UnorderedDependencyMatrixBuilder<>(groupArrayList.size());
		builder.addAllVariables(groupArrayList);
		builder.addAllDependencyTypes(matrix.getDependencyTypes());

		IntStream.range(0, groupArrayList.size()).forEach(index ->
				ClusterUtilities.ItemStream(groupArrayList.get(index))
						.map(ClusterComponent::getName)
						.flatMap(name -> matrix.get(matrix.getVariables().indexOf(name)).stream())
						.flatMap(location -> matrix.get(location).stream())
						.forEach(stringDependency -> {
							ClusterGroup targetParent = nameToItemMap.get(stringDependency.getTarget()).getParent();
							builder.addDependency(
									index,
									groupArrayList.indexOf(targetParent),
									stringDependency.getType(),
									stringDependency.getWeight()
							);
						})
		);

		return builder.build();
	}
}
