package com.sysc.solrServer.indexConfig;


import org.apache.solr.client.solrj.impl.HttpSolrServer;

import com.sysc.solrServer.common.PropertiesReader;
import com.sysc.solrServer.common.SolrConstants;
import com.sysc.solrServer.context.Context;

/**
 * 对应solrIndexConfig中的document元素
 * 
 * @author wz
 * 
 */
public class SolrConfigDocument {
	
	public static final String JOIN_CHAR = "`";
	
	/*
	 * document名称,对应一个索引库
	 */
	private String name;
	
	/*
	 * 对应的索引库的名称
	 */
	private String core;
	
	/*
	 * 任务表名称
	 */
	private String taskTableName;
	
	/*
	 * 数据源名称
	 */
	private String dataSourceName;
	
	/*
	 * 数据源对应的数据库类型:
	 * oracle/mysql/sqlserver/...
	 */
	private String dbType;
	
	/*
	 * 批量处理数量
	 */
	private Integer fetchSize;
	
	/*
	 * 该document是否已经启用
	 */
	private Boolean onUse;
	
	/*
	 * 根entity,肯定是一个MainTaskSolrEntity
	 */
	private MainTaskSolrEntity entity = new MainTaskSolrEntity();
	
	/*
	 * solr server.
	 */
	private HttpSolrServer server;
	
	/*
	 * 构造器
	 */
	public SolrConfigDocument(String name, String core, String dataSourceName, String taskTableName, String dbType, Integer fetchSize, Boolean onUse) {
		
//		Assert.assertNotNull("solrIndexConfig.xml中定义的document标签必须提供core属性!!!", core);
//		Assert.assertNotNull("solrIndexConfig.xml中定义的document标签必须提供dataSourceName属性!!!", dataSourceName);
//		Assert.assertNotNull("solrIndexConfig.xml中定义的document标签必须提供dbType属性!!!", dbType);
//		Assert.assertNotNull("solrIndexConfig.xml中定义的document标签必须提供onUse属性!!!", onUse);
		
		this.name = name == null ? core : name;
		this.core = core;
		this.dataSourceName = dataSourceName == null ? SolrConstants.DEFAULT_DATASOURCE_NAME : dataSourceName;
		this.dbType = dbType;
		this.fetchSize = fetchSize;
		this.taskTableName = taskTableName;
		this.onUse = onUse;
		String baseURL = "http://localhost:" + PropertiesReader.getProperties(Context.PROP_LOCATION_SERVER, "port") + "/" + PropertiesReader.getProperties(Context.PROP_LOCATION_SERVER, "webName") + "/" + core;
		this.server = new HttpSolrServer(baseURL);
	}
	
	/**
	 * 释放数据库连接,此处保证必须被调用
	 */
	public void releaseConnection() {
		entity.closeConnection();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setCore(String core) {
		this.core = core;
	}

	public String getCore() {
		return core;
	}
	
	public MainTaskSolrEntity getEntity() {
		return entity;
	}

	public void setEntity(MainTaskSolrEntity entity) {
		this.entity = entity;
	}

	public void setServer(HttpSolrServer server) {
		this.server = server;
	}

	public HttpSolrServer getServer() {
		return server;
	}

	public void setTaskTableName(String taskTableName) {
		this.taskTableName = taskTableName;
	}

	public String getTaskTableName() {
		return this.taskTableName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getDbType() {
		return dbType;
	}

	public void setFetchSize(Integer fetchSize) {
		this.fetchSize = fetchSize;
	}

	public Integer getFetchSize() {
		return fetchSize;
	}
	
	/**
	 * 生成查询任务表的语句
	 * @return
	 */
	public String getTaskQuerySQL() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT *  FROM ");
		sb.append(getTaskTableName());
		sb.append(fetchCondition());
		return sb.toString();
		
	}
	
	/**
	 * 生成删除任务表的语句
	 * @return
	 */
	public String getTaskDeleteSQL() {
		StringBuffer sb = new StringBuffer();
		sb.append("DELETE FROM ");
		sb.append(getTaskTableName() + " ");
		sb.append(fetchCondition());
		return sb.toString();
	}
	
	/**
	 * 获取分页语句
	 * @return
	 */
	private String fetchCondition() {
		if("oracle".equalsIgnoreCase(this.dbType)) {
			return " WHERE ROWNUM<=" + this.fetchSize;
		} else if("mysql".equalsIgnoreCase(this.dbType)) {
			return " LIMIT " + this.fetchSize;
		}
		return null;
	}
	
	/**
	 * 获取一个document的唯一标识
	 * @return 索引库名称+数据源名称+任务表名称+数据库类型:例如core8`gzpg`eps_solr_task`oracle,该结果用于生成对应的sql文件名
	 */
	public String getDistinctFlag() {
		return this.core + JOIN_CHAR + this.dataSourceName + JOIN_CHAR + this.taskTableName + JOIN_CHAR + this.dbType;
	}

	public void setOnUse(Boolean onUse) {
		this.onUse = onUse;
	}

	public Boolean getOnUse() {
		return onUse;
	}
	
	
}
