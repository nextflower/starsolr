package com.sysc.solrServer.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.sysc.solrServer.indexConfig.parser.SolrConfigParser;

public class PropertiesReader {
	
	public static String getProperties(String path, String key) {
		String result = null;
		InputStream in = null;
		try {
			in = SolrConfigParser.class.getClassLoader().getResourceAsStream(path);
			Properties p = new Properties();
			p.load(in);
			result = p.getProperty(key);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
}
