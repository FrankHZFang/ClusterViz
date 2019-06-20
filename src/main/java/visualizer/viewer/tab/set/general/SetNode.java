package visualizer.viewer.tab.set.general;

import com.archdia.dv8.cluster.ClusterGroup;

import java.util.Collection;
import java.util.HashSet;

public class SetNode {
	public final String name;
	private HashSet<ClusterGroup> parents;
	public Collection<ClusterGroup> parents() {
		return parents;
	}

	private HashSet<SetNode> dependencies;

	public Collection<SetNode> dependencies() {
		return dependencies;
	}

	public SetNode(String name) {
		this.name = name;
		parents = new HashSet<>();
		dependencies = new HashSet<>();
	}

	public void addParent(ClusterGroup parent) {
		parents.add(parent);
	}

	public void addDependency(SetNode node) {
		dependencies.add(node);
	}
}
