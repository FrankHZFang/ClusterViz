package visualizer.viewer.tab.cluster.circlepackage;

class ClusterGroupDependency {
	ClusterGroupNode target;
	String type;

	public ClusterGroupDependency(ClusterGroupNode target, String type) {
		this.target = target;
		this.type = type;
	}
}
