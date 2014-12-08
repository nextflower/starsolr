package com.sysc.solrServer.indexConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

/**
 * 生成索引配置文件,对应solrIndexConfig.xml配置文件
 * 
 * @author wz
 *
 */
public class SolrIndexDataConfig {
	
	/*
	 * Solr启动多久之后开始运行定时任务
	 */
	private long delay = 10000;
	
	/*
	 * 定时任务的扫描周期,一般上一次没有扫描完之前,下一次是不会开始的
	 */
	private long period = 5000;
	
	/*
	 * 是否自动提交,默认值为false
	 */
	private boolean autoCommit = false;
	
	/*
	 * 是否显示SQL语句
	 */
	private boolean showSQL = false;
	
	/*
	 * 是否显示调试信息
	 */
	private boolean showDebug = false;
	
	/*
	 * 配置文件中定义的数据源
	 */
	private Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();
	
	/*
	 * 配置文件中定义的数据源的相关信息
	 */
	private Map<String, Map<String, String>> dataSourceInfoMap = new HashMap<String, Map<String,String>>();
	
	/*
	 * document 列表
	 */
	private List<SolrConfigDocument> documentList = new ArrayList<SolrConfigDocument>();

	public void setDataSourceMap(Map<String, DataSource> dataSourceMap) {
		this.dataSourceMap = dataSourceMap;
	}

	public Map<String, DataSource> getDataSourceMap() {
		return dataSourceMap;
	}

	public void setDocumentList(List<SolrConfigDocument> documentList) {
		this.documentList = documentList;
	}

	public List<SolrConfigDocument> getDocumentList() {
		return documentList;
	}

	public void setDataSourceInfoMap(Map<String, Map<String, String>> dataSourceInfoMap) {
		this.dataSourceInfoMap = dataSourceInfoMap;
	}

	public Map<String, Map<String, String>> getDataSourceInfoMap() {
		return dataSourceInfoMap;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public long getDelay() {
		return delay;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	public long getPeriod() {
		return period;
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	public void setShowSQL(boolean showSQL) {
		this.showSQL = showSQL;
	}

	public boolean isShowSQL() {
		return showSQL;
	}

	public void setShowDebug(boolean showDebug) {
		this.showDebug = showDebug;
	}

	public boolean isShowDebug() {
		return showDebug;
	}
	
}
