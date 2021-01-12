package br.com.shellcode.pir.utils;

import java.io.File;

import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.plugins.PluginFolder;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.core.plugins.PluginFolderInterface;

public class PentahoUtils {

	public static final String DSCONFIGPATH = getDSConfigPath() + File.separator + "connections.properties";

	public static String getPDIRunnerBaseDir() {
		final String userDir = System.getProperty("user.home");
		boolean LINUX = false;
		boolean MAC = false;
		switch (System.getProperty("os.name").toLowerCase()) {
		case "mac os x":
			MAC = true;
			break;
		case "linux":
			LINUX = true;
			break;
		default:
		}
		return (LINUX) ? userDir + File.separator + ".pdi_runner"
				: ((MAC) ? userDir + File.separator + "/Library/Application Support/pdi_runner"
						: userDir + File.separator + "AppData" + File.separator + "Local" + File.separator + "PDI_Runner");
	}
	
	private static String getDSConfigPath() {
		return getPDIRunnerBaseDir() + File.separator + "config";
	}

	public static void initEnv(String pluginFolder) {
		try {
			if (System.getProperty("os.name").compareToIgnoreCase("linux") == 0) {
				System.setProperty("java.security.egd", "file:///dev/urandom");
			}
			Locale.setDefault(Locale.US);
			Hashtable<String, String> props = new Hashtable<>();
			props.put("java.naming.factory.initial", "org.osjava.sj.memory.MemoryContextFactory");
			props.put("org.osjava.sj.jndi.shared", "true");
			for (String k : props.keySet()) {
				System.setProperty(k, props.get(k));
			}
			System.out.println("Plugins dir: " + pluginFolder);
//			StepPluginType.getInstance().getPluginFolders().add(new PluginFolder(pluginFolder, false, true));
			List<PluginFolderInterface> pluginFolders = StepPluginType.getInstance().getPluginFolders();
			pluginFolders.add(new PluginFolder(pluginFolder + "/kettle-json-plugin", false, true));
			
			KettleEnvironment.init();
			try {
				InitialContext ic = new InitialContext(props);
				Map<String, Map<String, String>> configMap = DatasourceInitializer.getConfigMap(DSCONFIGPATH);
				Map<String, DataSource> mapDs = DatasourceInitializer.getDsFromMap(configMap);
				System.out.println("Inicializando Datasources:");
				for (String k : mapDs.keySet()) {
					ic.rebind(k, mapDs.get(k));
					System.out.println("> " + k);
					System.out.println("   > URL: " + (String) ((Map<?, ?>) configMap.get(k)).get("url"));
				}

			} catch (NamingException e) {
				e.printStackTrace();
			}
		} catch (KettleException e) {
			System.out.println(e);
		}
	}

	public static void runTransformation(String path, String descr, Properties props)
			throws KettleXMLException, KettleMissingPluginsException, UnknownParamException, KettleException {
		System.out.println("Initializing: " + descr);
		TransMeta transMeta = new TransMeta(path);
		for (Object k : props.keySet()) {
			String key = (String) k;
			String value = props.getProperty(key);
			transMeta.setParameterValue(key, value);
		}
		Trans trans = new Trans(transMeta);
		trans.execute(null);
		trans.waitUntilFinished();
	}
}