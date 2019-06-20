package visualizer.viewer.tab.set.general;

import com.archdia.dv8.cluster.ClusterGroup;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.io.output.StringBuilderWriter;
import visualizer.viewer.GraphicsJsonGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class SetNodeToGraphicsJson extends GraphicsJsonGenerator {

	public static String serialize(Collection<ClusterGroup> sets, Collection<SetNode> setNodes) throws IOException {
		ArrayList<Link> linkList = new ArrayList<>();
		StringBuilderWriter writer = new StringBuilderWriter();

		JsonGenerator jgen = JF.createGenerator(writer);
		jgen.writeStartObject();

		jgen.writeArrayFieldStart("sets");
		for (ClusterGroup s : sets) {
			writeSetObject(jgen, s);
		}
		jgen.writeEndArray();

		jgen.writeArrayFieldStart("nodes");
		for (SetNode node : setNodes) {
			writeNodeObject(jgen, node);

			node.dependencies().forEach(
					target -> linkList.add(new Link(node, target))
			);
		}
		jgen.writeEndArray();

		jgen.writeArrayFieldStart("links");
		for (Link l : linkList) {
			writeLinkObject(jgen, l);
		}
		jgen.writeEndArray();

		jgen.writeEndObject();
		jgen.flush();
		jgen.close();

		return writer.toString();
	}

	private static void writeSetObject(JsonGenerator jgen, ClusterGroup set) throws IOException {
		jgen.writeStartObject();

		jgen.writeFieldName("id");
		jgen.writeString(setID(set));

		jgen.writeEndObject();
	}

	private static void writeNodeObject(JsonGenerator jgen, SetNode node) throws IOException {
		jgen.writeStartObject();

		jgen.writeFieldName("id");
		jgen.writeString(nodeID(node));

		jgen.writeArrayFieldStart("set");
		for (ClusterGroup parent : node.parents()) {
			jgen.writeString(setID(parent));
		}
		jgen.writeEndArray();

		jgen.writeEndObject();
	}

	private static void writeLinkObject(JsonGenerator jgen, Link link) throws IOException {
		jgen.writeStartObject();
		jgen.writeFieldName("source_id");
		jgen.writeString(link.source_id);
		jgen.writeFieldName("target_id");
		jgen.writeString(link.target_id);
		jgen.writeEndObject();
	}

	private static String setID(ClusterGroup set) {
		return "set_" + set.getName();
	}

	private static String nodeID(SetNode node) {
		return "node_" + node.name;
	}

	private static class Link {
		final String source_id;
		final String target_id;

		Link(SetNode source, SetNode target) {
			this.source_id = nodeID(source);
			this.target_id = nodeID(target);
		}
	}
}
