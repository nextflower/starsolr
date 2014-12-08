package com.sysc.solrServer.indexConfig;

/**
 * 配置文件上下文
 * 
 * @author wz
 *
 */
public final class SolrConfigAccess {

	private SolrConfigAccess() {
		//禁止初始化
	}
	
	/*
	 * solrIndexConfig.xml映射
	 */
	private static SolrIndexDataConfig config = new SolrIndexDataConfig();
	
	public static void setConfig(SolrIndexDataConfig config) {
		SolrConfigAccess.config = config;
	}

	public static SolrIndexDataConfig getConfig() {
		return config;
	}
	
}
