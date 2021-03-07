package com.konantech.forensic.common;

import java.io.FileInputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyConfig {
	final static Logger log = LoggerFactory.getLogger(PropertyConfig.class);

	public static Properties getProperties() {

		Properties props = new Properties();
		FileInputStream istream = null;
		String fileFPath = System.getProperty("konan.configuration");
		try {
		if (fileFPath == null)
			throw new Exception("run konan.configuration is null");
			System.out.println("konan.configuration : " + fileFPath);
			istream = new FileInputStream(fileFPath);
			props.load(istream);
			istream.close();
		} catch (Exception e) {
			log.error("konan properties error", e);
		} finally {
			if (istream != null) {
				try {
					istream.close();
				} catch (Throwable ignore) {
				}
			}
		}
		return props;
	}

}
