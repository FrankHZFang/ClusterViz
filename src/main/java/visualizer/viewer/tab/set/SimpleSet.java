package visualizer.viewer.tab.set;

import java.util.HashSet;
import java.util.Set;

public class SimpleSet {
	public final String name;
	public final Set<String> files;

	public SimpleSet(String name, Set<String> files) {
		this.name = name;
		this.files = new HashSet<>(files);
	}

	@Override
	public String toString() {
		return name;
	}
}
