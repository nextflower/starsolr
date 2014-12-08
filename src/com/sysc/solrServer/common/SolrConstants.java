package com.sysc.solrServer.common;

/**
 * 常量定义类
 * 
 * @author wz
 *
 */
public class SolrConstants {
	
	public static final String UTF_8 = "UTF8";
	
	//数据源信息map的键值
	public static final String DATASOURCE_INFO_NAME = "name";//数据源名称
	public static final String DATASOURCE_INFO_DRIVERCLASSNAME = "driverClassName";//数据源驱动
	public static final String DATASOURCE_INFO_URL = "url";//数据源地址
	public static final String DATASOURCE_INFO_USERNAME = "username";//用户名
	public static final String DATASOURCE_INFO_PASSWORD = "password";//密码
	public static final String DATASOURCE_INFO_DBTYPE = "dbType";//数据库类型:oracle\mysql\sqlserver...
	
	/*
	 * 默认数据源在map中的key
	 */
	public static final String DEFAULT_DATASOURCE_NAME = "DEFAULT_DATASOURCE_NAME";

	/*
	 * 默认的字段权重
	 */
	public static final float DEFAULT_BOOST = 1.0f;
	
	/*
	 * 默认的fetchSize
	 */
	public static final int DEFAULT_FETCH_SIZE = 200;
	
	/*
	 * 配置文件中变量替换的前缀和后缀
	 */
	public static final String FIELD_PREFIX = "${";
	public static final String FIELD_POST = "}";
	
	/*
	 * 任务表中作为索引库id的字段
	 */
	public static final String TASK_SOLR_PK = "SOLR_PK";
	/*
	 * 任务表中用于区分增加、更新、删除操作的状态位字段名
	 * 	1:删除
	 * 	0:增加或更新
	 */
	public static final String TASK_STATUS_FIELD = "STATUS";
	/*
	 * 任务表中记录时间戳的字段
	 */
	public static final String TASK_STAMPTIME_FIELD = "SYS_LASTMODIFIED";
	
	/*
	 * 默认动态字段的前缀,当项目应用中仅有一个动态字段时,默认使用该值作为其动态字段的前缀
	 * 同时要求在schema.xml中定义类似：<dynamicField name="EpsDynamic_`*" type="string" indexed="true" stored="true"/>
	 */
	public static final String DYNAMIC_FILED_PREFIX = "EpsDynamic_`";
	
}
