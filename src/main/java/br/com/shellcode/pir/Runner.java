package br.com.shellcode.pir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import br.com.shellcode.pir.utils.PentahoUtils;

//import java.sql.SQLException;

//import java.util.Hashtable;
//import java.util.Locale;
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
//import javax.sql.DataSource;
//import oracle.jdbc.pool.OracleDataSource;
//import org.pentaho.di.core.KettleEnvironment;
//import org.pentaho.di.core.exception.KettleException;
//import org.pentaho.di.core.exception.KettleMissingPluginsException;
//import org.pentaho.di.core.exception.KettleXMLException;
//import org.pentaho.di.core.parameters.UnknownParamException;
//import org.pentaho.di.core.plugins.PluginFolder;
//import org.pentaho.di.core.plugins.StepPluginType;
//import org.pentaho.di.trans.Trans;
//import org.pentaho.di.trans.TransMeta;
//import org.postgresql.jdbc3.Jdbc3SimpleDataSource;

public class Runner {

	public static void main(String[] args) {
		/**
		 * Download plugins to cache dir
		 */
		downloadPlugins("9.1.0.0-324");

		/**
		 * Download libraries to cache dir
		 */
//		downloadLibs("9.1.0.0-324");

		/**
		 * initialize environment
		 */
//		System.out.println(getPDIRunnerBaseDir());
		PentahoUtils.initEnv(getPluginsDir() + File.separator + "9.1.0.0-324");
//		PentahoUtils.initEnv("D:/pentaho_pdi_plugins/data-integration/plugins/");

		if (args.length > 0) {

		} else {
			scanKtr();
		}
	}

	public static void downloadPlugins(String pdiVersion) {
		String plugins[] = { "kettle-dummy-plugin", "kettle-json-plugin", "pdi-core-plugins", "pdi-xml-plugin",
				"platform-utils-plugin" };
		for (int i = 0; i < plugins.length; i++) {
			downloadPlugin(pdiVersion, plugins[i]);
		}
	}

	private static String getPluginsDir() {
		return PentahoUtils.getPDIRunnerBaseDir() + File.separator + "plugins";
	}

	public static void downloadPlugin(String pdiVersion, String plugin) {
		URL obj;
		final int BUFFER_SIZE = 4096;
		String saveDirPath = getPluginsDir() + File.separator + pdiVersion + File.separator + plugin;
		String saveFilePath = saveDirPath + ".zip";
		try {
			File destinationDir = new File(saveDirPath);
			File destinationFile = new File(saveFilePath);
			// nao existe, baixar
			if (!destinationFile.exists()) {
				String downloadPlugins = "https://github.com/andersonbr-oss/pentaho_pdi_plugins/raw/master/plugins/"
						+ pdiVersion + "/" + plugin + ".zip";
				obj = new URL(downloadPlugins);
				HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
				conn.setInstanceFollowRedirects(true);
				HttpURLConnection.setFollowRedirects(true);
				InputStream inputStream = conn.getInputStream();
				File directory = new File(saveDirPath);
				if (!directory.exists()) {
					directory.mkdirs();
				}

				// opens an output stream to save into file
				FileOutputStream outputStream = new FileOutputStream(saveFilePath);

				int bytesRead = -1;
				byte[] buffer = new byte[BUFFER_SIZE];
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}

				outputStream.close();
				inputStream.close();

			} else {
			}
			if (!destinationDir.exists()) {
				// UNZIP
				unzip(saveFilePath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getLibsDir() {
		return PentahoUtils.getPDIRunnerBaseDir() + File.separator + "lib";
	}

	public static void downloadLibs(String pdiVersion) {
		URL obj;
		final int BUFFER_SIZE = 4096;
		String saveDirPath = getLibsDir() + File.separator + pdiVersion;
		String saveFilePath = saveDirPath + ".zip";
		try {
			File destinationDir = new File(saveDirPath);
			File destinationFile = new File(saveFilePath);
			// nao existe, baixar
			if (!destinationFile.exists()) {
				String downloadPlugins = "https://github.com/andersonbr-oss/pentaho_pdi_plugins/raw/master/lib/"
						+ pdiVersion + ".zip";
				obj = new URL(downloadPlugins);
				HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
				conn.setInstanceFollowRedirects(true);
				HttpURLConnection.setFollowRedirects(true);
				InputStream inputStream = conn.getInputStream();
				File directory = new File(saveDirPath);
				if (!directory.exists()) {
					directory.mkdirs();
				}

				// opens an output stream to save into file
				FileOutputStream outputStream = new FileOutputStream(saveFilePath);

				int bytesRead = -1;
				byte[] buffer = new byte[BUFFER_SIZE];
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}

				outputStream.close();
				inputStream.close();

			} else {
			}
			if (!destinationDir.exists()) {
				// UNZIP
				unzip(saveFilePath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void scanKtr() {
		final int minDepth = 0;
		final int maxDepth = 4;
		final String PATTERN = ".*\\.ktr";
		final Path rootPath = Paths.get("");
		final int rootPathDepth = rootPath.getNameCount();
		try {
			List<Path> paths = Files.walk(rootPath, maxDepth)
					.filter(e -> e.toFile().isFile() && e.toFile().getName().matches(PATTERN))
					.filter(e -> e.getNameCount() - rootPathDepth >= minDepth).collect(Collectors.toList());
			for (Path p : paths) {
				System.out.println(p);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void unzip(String file) {
		File destDir = new File(new File(file).getParent());
		byte[] buffer = new byte[1024];
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				File newFile = newFile(destDir, zipEntry);
				if (zipEntry.isDirectory()) {
					if (!newFile.isDirectory() && !newFile.mkdirs()) {
						throw new IOException("Failed to create directory " + newFile);
					}
				} else {
					File parent = newFile.getParentFile();
					if (!parent.isDirectory() && !parent.mkdirs()) {
						throw new IOException("Failed to create directory " + parent);
					}
					FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
				}
				zipEntry = zis.getNextEntry();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());
		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();
		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}
		return destFile;
	}
}
