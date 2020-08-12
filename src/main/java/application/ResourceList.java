package application;

import java.io.File;
import java.net.URL;

/**
 * list resources available from the classpath @ *
 */
public class ResourceList {

	private static File[] getResourceFolderFiles(String folder) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource(folder);
		String path = url.getPath();
		return new File(path).listFiles();
	}

	public static void main(String[] args) {
		for (File f : getResourceFolderFiles("/natives")) {
			System.out.println(f);
		}
	}
}