package visualizer.viewer.tab.set;

import visualizer.viewer.selecttable.SelectTableItem;

public class SimpleSetSelectTableItem extends SelectTableItem<SimpleSet> {
	public SimpleSetSelectTableItem(SimpleSet set) {
		this.object.setValue(set);
		this.name.setValue(set.name);
		this.size.setValue(set.files.size());
	}
}
