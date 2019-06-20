package visualizer.viewer.clustergroup;

import com.archdia.dv8.cluster.ClusterGroup;
import visualizer.viewer.selecttable.SelectTableItem;

public class ClusterGroupSelectTableItem extends SelectTableItem<ClusterGroup> {
	public ClusterGroupSelectTableItem(ClusterGroup c) {
		object.setValue(c);
		name.setValue(c.getName());
		size.setValue(c.getChildren().size());
	}
}
