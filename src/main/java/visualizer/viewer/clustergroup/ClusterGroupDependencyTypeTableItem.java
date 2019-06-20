package visualizer.viewer.clustergroup;

import com.archdia.dv8.matrix.DependencyType;
import javafx.beans.property.*;

public class ClusterGroupDependencyTypeTableItem {
	public final DependencyType type;
	public final StringProperty name;
	public final BooleanProperty displayed;
	public final IntegerProperty totalCount;
	public final IntegerProperty currentInternal;
	public final IntegerProperty currentExternal;

	public ClusterGroupDependencyTypeTableItem(DependencyType type) {
		this.type = type;
		this.name = new SimpleStringProperty(type.getName());
		this.displayed = new SimpleBooleanProperty(false);
		this.totalCount = new SimpleIntegerProperty(0);
		this.currentInternal = new SimpleIntegerProperty(0);
		this.currentExternal = new SimpleIntegerProperty(0);
	}
}
