package server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

public class ResourceLoader {
	private static final Map<String, byte[]> resourceMap = new HashMap<>();
	private static final Map<String, String> stringMap = new HashMap<>();

	private static void loadResource(String resourcePath) {
		InputStream in = ResourceLoader.class.getResourceAsStream(resourcePath);
		if (in == null) {
			return;
		}
		try {
			byte[] bytes = IOUtils.toByteArray(in);
			resourceMap.put(resourcePath, bytes);
			stringMap.put(resourcePath, new String(bytes));
		} catch (IOException e) {
			System.err.printf("Unable to read resource %s \n%s", resourcePath, e.getMessage());
		}
	}

	public static synchronized byte[] getResource(String resourcePath) {
		if (resourceMap.get(resourcePath) == null) {
			loadResource(resourcePath);
		}
		return resourceMap.get(resourcePath);
	}

	public static synchronized String getStringResource(String resourcePath) {
		if (resourceMap.get(resourcePath) == null) {
			loadResource(resourcePath);
		}
		return stringMap.get(resourcePath);
	}

	public static synchronized String getNonNullStringResource(String resourcePath) {
		if (resourceMap.get(resourcePath) == null) {
			loadResource(resourcePath);
			if (resourceMap.get(resourcePath) == null) {
				System.err.println("Nonnull resource violation: " + resourcePath);
				System.exit(-1);
			}
		}
		return stringMap.get(resourcePath);
	}

	public static synchronized void registerResource(String resourcePath, String resource) {
		resourceMap.put(resourcePath, resource.getBytes());
		stringMap.put(resourcePath, resource);
	}
}
