package visualizer.dv8.utilities;

import com.archdia.dv8.cluster.ClusterGroup;
import com.archdia.dv8.cluster.ClusterItem;
import com.archdia.dv8.cluster.ClusterParent;

import java.util.Collection;
import java.util.stream.Stream;

public class ClusterUtilities {
	public static Stream<ClusterItem> ItemStream(ClusterGroup c) {
		return c.getChildren().stream()
				.filter(clusterComponent -> clusterComponent instanceof ClusterItem)
				.map(clusterComponent -> (ClusterItem) clusterComponent);
	}

	public static Stream<ClusterGroup> createCombinedClusterGroup(ClusterGroup parent, Collection<ClusterGroup> accepted) {
		if (!accepted.contains(parent)) {
			return Stream.empty();
		}

		return Stream.concat(
				Stream.of(parent),
				ClusterUtilities.collectAllDescendantGroups(parent)
						.filter(descendant -> !accepted.contains(descendant))
		);
	}

	public static ClusterGroup findAcceptedParent(ClusterGroup c, Collection<ClusterGroup> accepted) {
		if (c == null) {
			return null;
		}

		ClusterGroup parent = c.getParent();
		if (accepted.contains(parent)) {
			return parent;
		}
		return findAcceptedParent(parent, accepted);
	}

	public static ClusterGroup findAcceptedSelfOrParent(ClusterGroup c, Collection<ClusterGroup> accepted) {
		if (c == null) {
			return null;
		}

		if (accepted.contains(c)) {
			return c;
		}
		return findAcceptedSelfOrParent(c.getParent(), accepted);
	}

	public static Stream<ClusterGroup> collectAllDescendantGroups(ClusterParent c) {
		return recursivelyCollectDescendantGroups(c);
	}

	private static Stream<ClusterGroup> recursivelyCollectDescendantGroups(ClusterParent c) {
		return c.stream()
				.filter(clusterComponent -> clusterComponent instanceof ClusterGroup)
				.flatMap(clusterComponent ->
						Stream.concat(
								Stream.of((ClusterGroup) clusterComponent),
								recursivelyCollectDescendantGroups((ClusterGroup) clusterComponent)
						)
				);
	}

	public static Stream<ClusterItem> collectAllDescendantItems(ClusterParent c) {
		return recursivelyCollectDescendantItems(c);
	}

	public static Stream<String> collectAllDescendantItemNames(ClusterParent c) {
		return collectAllDescendantItems(c).map(ClusterItem::getName);
	}

	private static Stream<ClusterItem> recursivelyCollectDescendantItems(ClusterParent c) {
		return c.stream()
				.flatMap(clusterComponent -> {
							if (clusterComponent instanceof ClusterParent) {
								return recursivelyCollectDescendantItems((ClusterParent) clusterComponent);
							} else {
								return Stream.of((ClusterItem) clusterComponent);
							}
						}
				);
	}
}
