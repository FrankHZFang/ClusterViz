package visualizer.viewer.clusteritem;

import com.archdia.dv8.matrix.DependencyType;
import javafx.beans.property.*;

public class ClusterItemDependencyTypeTableItem {
	public final DependencyType type;
	public final StringProperty name;
	public final BooleanProperty displayed;
	public final IntegerProperty totalCount;
	public final IntegerProperty currentExternal;

	public ClusterItemDependencyTypeTableItem(DependencyType type) {
		this.type = type;
		this.name = new SimpleStringProperty(type.getName());
		this.displayed = new SimpleBooleanProperty(false);
		this.totalCount = new SimpleIntegerProperty(0);
		this.currentExternal = new SimpleIntegerProperty(0);
	}
}
