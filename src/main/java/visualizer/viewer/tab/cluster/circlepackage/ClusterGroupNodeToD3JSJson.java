package visualizer.viewer.tab.cluster.circlepackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.StringBuilderWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import visualizer.viewer.GraphicsJsonGenerator;

public class ClusterGroupNodeToD3JSJson extends GraphicsJsonGenerator {

	public static String serialize(ClusterGroupNode rootNode) throws IOException {
		ArrayList<Link> linkList = new ArrayList<>();
		StringBuilderWriter writer = new StringBuilderWriter();

		JsonGenerator jGen = JF.createGenerator(writer);
		jGen.writeStartObject();

		jGen.writeFieldName("root");
		writeNodeObject(jGen, rootNode, linkList);

		jGen.writeArrayFieldStart("links");
		for (Link link : linkList) {
			writeLinkObject(jGen, link);
		}
		jGen.writeEndArray();

		jGen.writeEndObject();
		jGen.flush();
		jGen.close();

		return writer.toString();
	}

	private static void writeNodeObject(JsonGenerator jGen, ClusterGroupNode node, List<Link> linkList) throws IOException {
		String id = "cl_" + node.name;
		jGen.writeStartObject();
		jGen.writeFieldName("id");
		jGen.writeString(id);
		jGen.writeFieldName("name");
		jGen.writeString(node.name);

		if (node.childNodes.isEmpty()) {
			jGen.writeNumberField("size", node.size);
		} else {
			jGen.writeArrayFieldStart("children");
			for (ClusterGroupNode child : node.childNodes) {
				writeNodeObject(jGen, child, linkList);
			}
			jGen.writeEndArray();
		}
		jGen.writeEndObject();

		for (ClusterGroupDependency dep : node.dependencies) {
			linkList.add(new Link(id, "cl_" + dep.target.name, dep.type));
		}
	}

	private static void writeLinkObject(JsonGenerator jgen, Link link) throws IOException {
		jgen.writeStartObject();
		jgen.writeFieldName("source_id");
		jgen.writeString(link.source_id);
		jgen.writeFieldName("target_id");
		jgen.writeString(link.target_id);
		jgen.writeFieldName("type");
		jgen.writeString(link.type);
		jgen.writeEndObject();
	}

	private static class Link {
		final String source_id;
		final String target_id;
		final String type;

		Link(String source, String target, String type) {
			this.source_id = source;
			this.target_id = target;
			this.type = type;
		}
	}
}
