package br.com.shellcode.pir.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.postgresql.jdbc3.Jdbc3SimpleDataSource;

import oracle.jdbc.pool.OracleDataSource;

public class DatasourceInitializer {
	private static Map<String, DataSource> dsMap = null;
	private static Map<String, Map<String, String>> propsMap = new HashMap<>();

	public static Map<String, DataSource> getDsFromMap(Map<String, Map<String, String>> propsMap) {
		if (dsMap == null) {
			dsMap = new HashMap<>();
			for (String k : propsMap.keySet()) {
				String driver = (String) propsMap.get(k).get("driver");
				DataSource ds = null;
				if (driver.equals("org.postgresql.Driver")) {
					Jdbc3SimpleDataSource pds = new Jdbc3SimpleDataSource();
					pds.setUrl((String) propsMap.get(k).get("url"));
					pds.setUser((String) propsMap.get(k).get("user"));
					pds.setPassword((String) propsMap.get(k).get("password"));
					ds = pds;
				} else if (driver.equals("oracle.jdbc.driver.OracleDriver")) {
					try {
						OracleDataSource ods = new OracleDataSource();
						ods.setURL((String) propsMap.get(k).get("url"));
						ods.setUser((String) propsMap.get(k).get("user"));
						ods.setPassword((String) propsMap.get(k).get("password"));
						ds = ods;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				dsMap.put(k, ds);
			}
		}
		return dsMap;
	}

	public static Map<String, Map<String, String>> getConfigMap(String configPath) {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(configPath));
			for (Object k : p.keySet()) {
				String splitedKey[] = ((String) k).split("/");
				if (splitedKey.length == 2) {
					String dsKey = splitedKey[0];
					String dsProp = splitedKey[1];
					String value = (String) p.get(k);
					if (propsMap.get(dsKey) == null) {
						propsMap.put(dsKey, new HashMap<>());
					}
					propsMap.get(dsKey).put(dsProp, value);
				} else {
					System.err.println("Wrong key pattern");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return propsMap;
	}
}
