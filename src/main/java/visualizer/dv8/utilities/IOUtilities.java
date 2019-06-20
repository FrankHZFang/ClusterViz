package visualizer.dv8.utilities;

import com.archdia.dv8.cluster.Clustering;
import com.archdia.dv8.cluster.internal.ClusteringJsonImportService;
import com.archdia.dv8.matrix.DependencyMatrix;
import com.archdia.dv8.matrix.DependencyType;
import com.archdia.dv8.matrix.internal.DependencyMatrixJsonImportService;
import com.archdia.dv8.matrix.internal.DependencyTypeJsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class IOUtilities {
	private static DependencyMatrixJsonImportService jsonMatrixImportService = new DependencyMatrixJsonImportService();
	private static ClusteringJsonImportService clusteringJsonImportService = new ClusteringJsonImportService();
	private static ObjectMapper mapper = new ObjectMapper();
	private static boolean initiated = false;

	private static void checkInitiated() {
		if (!initiated) {
			SimpleModule module = new SimpleModule();
			module.addDeserializer(DependencyType.class, new DependencyTypeJsonDeserializer());
			mapper.registerModule(module);

			initiated = true;
		}
	}

	public static DependencyMatrix<String> loadMatrixFromJSON(File jsonPath, File dependencyType) throws IOException {
		checkInitiated();
		Map<String, DependencyType> dependTypeMap = loadDependencyTypeMap(dependencyType.toPath());
		return jsonMatrixImportService.loadDependencyMatrix(jsonPath.toPath(), dependTypeMap);
	}

	private static Map<String, DependencyType> loadDependencyTypeMap(Path path) throws IOException {
		BufferedReader in = Files.newBufferedReader(path, UTF_8);
		Map<String, DependencyType> map = new HashMap<>();
		JsonNode mapNode = mapper.readTree(in);
		for (JsonNode typeNode : mapNode) {
			DependencyType type = mapper.treeToValue(typeNode, DependencyType.class);
			map.put(type.getName(), type);
		}
		in.close();
		return map;
	}

	public static Clustering loadClusterFromJSON(File clusterFile) throws IOException {
		return clusteringJsonImportService.loadClustering(clusterFile.toPath());
	}
}
