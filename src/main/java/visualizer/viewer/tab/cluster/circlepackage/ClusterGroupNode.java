package visualizer.viewer.tab.cluster.circlepackage;

import com.archdia.dv8.cluster.ClusterGroup;

import java.util.ArrayList;
import java.util.Collection;

public class ClusterGroupNode {
	final String name;
	final Collection<ClusterGroupDependency> dependencies;
	final Collection<ClusterGroupNode> childNodes;
	final int size;

	public ClusterGroupNode(String name, int size) {
		this.name = name;
		this.size = size;
		dependencies = new ArrayList<>();
		childNodes = new ArrayList<>();
	}

	public ClusterGroupNode(ClusterGroup cluster) {
		this(cluster.getName(), cluster.getChildren().size() + 1);
	}

}



