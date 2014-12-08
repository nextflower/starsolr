package com.sysc.solrServer.indexConfig.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.sysc.solrServer.common.PropertiesReader;
import com.sysc.solrServer.common.SolrConstants;
import com.sysc.solrServer.context.Context;
import com.sysc.solrServer.context.Global;
import com.sysc.solrServer.exception.SolrConfigException;
import com.sysc.solrServer.indexConfig.AbstractSolrEntity;
import com.sysc.solrServer.indexConfig.CommonSolrEntity;
import com.sysc.solrServer.indexConfig.MainTaskSolrEntity;
import com.sysc.solrServer.indexConfig.SolrConfigAccess;
import com.sysc.solrServer.indexConfig.SolrConfigDocument;
import com.sysc.solrServer.indexConfig.SolrConfigField;
import com.sysc.solrServer.indexConfig.SolrEntity;
import com.sysc.solrServer.indexConfig.SolrIndexDataConfig;

/**
 * solrIndexConfig.xml解析器
 * 
 * @author wz
 *
 */
public class SolrConfigParser {
	
	public static final String CONFIG_LOCATION = "solrIndexConfig.xml";
	
	/**
	 * 解析solrIndexConfig.xml文件,封装到系统配置属性中
	 */
	public static boolean parse() {
		boolean result = false;
		SAXReader saxReader = new SAXReader();
		InputStream is = null;
		try {
			is = SolrConfigParser.class.getClassLoader().getResourceAsStream(CONFIG_LOCATION);
			Document doc = saxReader.read(is);
			Element root = (Element) doc.selectSingleNode("solrIndexConfig");
			
			//校验配置文件
			boolean flag = validate(doc);
			
			if(flag) {
				
				//数据源配置解析
				parseDatasourcesNode(root);
				Global.getLogger().info("数据源配置解析成功!");
				//document对象解析
				parseDocumentNode(root);
				Global.getLogger().info("document对象解析成功!");
			}
			
			//解析成功
			result = true;
		} catch (DocumentException e) {
			Global.getLogger().error(e);
		} catch (SolrConfigException e) {
			Global.getLogger().error("solrIndexConfig.xml配置文件解析错误!!!", e);
		} catch (ClassNotFoundException e) {
			Global.getLogger().error(e);
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					Global.getLogger().error(e);
				}
			}
		}
		return result;
	}
	
	/**
	 * 解析document对象,其中包含了entity以及field信息。
	 * @param root
	 * @throws SolrConfigException
	 */
	private static void parseDocumentNode(Element root) throws SolrConfigException {
		Element documentNode = root.element("documents");
		if(documentNode == null) {
			throw new SolrConfigException("<documents>标签未定义!!!");
		}
		
		/*
		 * task_delay:Solr启动多久之后开始运行定时任务
		 * task_period:定时任务的扫描周期,一般上一次没有扫描完之前,下一次是不会开始的
		 * autoCommit:是否自动提交,默认值为false
		 */
		String task_delay = documentNode.attributeValue("task_delay");
		String task_period = documentNode.attributeValue("task_period");
		String autoCommit = documentNode.attributeValue("autoCommit");
		String showSQL = documentNode.attributeValue("showSQL");
		String showDebug = documentNode.attributeValue("showDebug");
		if(!StringUtils.isBlank(task_delay)) {
			SolrConfigAccess.getConfig().setDelay(Long.parseLong(task_delay));
		}
		if(!StringUtils.isBlank(task_period)) {
			SolrConfigAccess.getConfig().setPeriod(Long.parseLong(task_period));
		}
		if(!StringUtils.isBlank(autoCommit)) {
			SolrConfigAccess.getConfig().setAutoCommit(Boolean.parseBoolean(autoCommit));
		}
		if(!StringUtils.isBlank(showSQL)) {
			SolrConfigAccess.getConfig().setShowSQL(Boolean.parseBoolean(showSQL));
		}
		if(!StringUtils.isBlank(showDebug)) {
			SolrConfigAccess.getConfig().setShowDebug(Boolean.parseBoolean(showDebug));
		}
		
		@SuppressWarnings("unchecked")
		List<Element> docList = documentNode.elements("document");
		if(docList == null || docList.size() == 0) {
			throw new SolrConfigException("<documents>下至少要定义一个<document>!!!");
		}
		
		for(Element e : docList) {
			String name = e.attributeValue("name");
			String core = e.attributeValue("core");
			String dataSource = e.attributeValue("dataSource");
			String fetchSizeStr = e.attributeValue("fetchSize");
			String taskTableName = e.attributeValue("taskTableName");
			String dbType = e.attributeValue("dbType");
			String onUseStr = e.attributeValue("onUse");
			
			
			Integer fetchSize = fetchSizeStr == null ? SolrConstants.DEFAULT_FETCH_SIZE : Integer.parseInt(fetchSizeStr);
			
			Boolean onUse = null;
			if("true".equalsIgnoreCase(onUseStr)) {
				onUse = true;
			} else if("false".equalsIgnoreCase(onUseStr)) {
				onUse = false;
			}
			
			if(StringUtils.isBlank(dbType)) {
				dbType = SolrConfigAccess.getConfig().getDataSourceInfoMap().get(dataSource).get(SolrConstants.DATASOURCE_INFO_DBTYPE);
			}
			
			SolrConfigDocument doc = new SolrConfigDocument(name, core, dataSource, taskTableName, dbType, fetchSize, onUse);
			SolrConfigAccess.getConfig().getDocumentList().add(doc);
			
			//解析document下的根entity标签
			parseMainEntityNode(e, doc);
			
		}
		
	}

	/**
	 * 解析entity对象
	 * 
	 * @param document
	 * @param doc
	 * @throws SolrConfigException 
	 */
	private static void parseMainEntityNode(Element document, SolrConfigDocument doc) throws SolrConfigException {
		@SuppressWarnings("unchecked")
		List<Element> entityList = document.elements("entity");
		//根entity的数量只能为1个。
		if(entityList == null || entityList.size() == 0) {
			throw new SolrConfigException("<document>标签下未定义<entity>!!!");
		} else if(entityList.size() > 1) {
			throw new SolrConfigException("<document>标签下的一级<entity>只能有一个!!!");
		}
		
		//document的根entity,需要特殊处理
		Element entityE = entityList.get(0);
		MainTaskSolrEntity entity = new MainTaskSolrEntity();
		Map<String, String> allAttrMap = getAllAttrMap(entityE);
		//生成query语句 
		String querySQL = doc.getTaskQuerySQL();
		allAttrMap.put("query", querySQL);
		entity.setAttrMap(allAttrMap);
		entity.setDocument(doc);
		doc.setEntity(entity);
		
		//解析字段标签
		parseFieldNode(entityE, entity);
		parseChildEntityNode(entityE, entity);
		
	}


	/**
	 * 解析子类entity 
	 * @param entityE
	 * @param entity
	 */
	private static void parseChildEntityNode(Element parentEntityE, SolrEntity parentEntity) {
		@SuppressWarnings("unchecked")
		List<Element> entityList = parentEntityE.elements("entity");
		if(entityList != null) {
			for(Element entityE : entityList) {
				
				String type = entityE.attributeValue("type");
				AbstractSolrEntity entity = createEntityByTpye(type);
				
				entity.setAttrMap(getAllAttrMap(entityE));
				
				//设置父entity
				entity.setParent(parentEntity);
				parentEntity.getChildEntityList().add(entity);
				
				//解析字段标签
				parseFieldNode(entityE, entity);
				parseChildEntityNode(entityE, entity);
			}
		}
	}

	private static Map<String, String> getAllAttrMap(Element entityE) {
		@SuppressWarnings("unchecked")
		List<Attribute> attributes = entityE.attributes();
		//获取所有的属性
		Map<String, String> attrMap = new HashMap<String, String>();
		if(attributes != null) {
			for(Attribute attr : attributes) {
				String name = attr.getName();
				String value = attr.getValue();
				attrMap.put(name, value);
			}
		}
		return attrMap;
	}

	/**
	 * 根据type定义生成对应的SolrEntity
	 * @param type
	 * @return
	 */
	private static AbstractSolrEntity createEntityByTpye(String type) {
		if(type == null) {
			return new CommonSolrEntity();
		}
		String className = PropertiesReader.getProperties(Context.PROP_LOCATION_DYNAMIC_FIELD, type);
		try {
			return (AbstractSolrEntity) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			Global.getLogger().error(e);
		} catch (IllegalAccessException e) {
			Global.getLogger().error(e);
		} catch (ClassNotFoundException e) {
			Global.getLogger().error(e);
		}
		return null;
	}

	/**
	 * 解析字段标签
	 * @param entityE
	 * @param entity 父类entity
	 */
	private static void parseFieldNode(Element entityE, SolrEntity entity) {
		@SuppressWarnings("unchecked")
		List<Element> fieldList = entityE.elements("field");
		if(fieldList != null) {
			for(Element fieldE : fieldList) {
				String name = fieldE.attributeValue("name");
				String column = fieldE.attributeValue("column");
				String boostStr = fieldE.attributeValue("boost");
				//Assert.assertNotNull("<field>:name不能为空", name);
				//Assert.assertNotNull("<field>:column不能为空", column);
				float boost = boostStr == null ? SolrConstants.DEFAULT_BOOST : Float.parseFloat(boostStr);
				SolrConfigField field = new SolrConfigField(name, column, boost);
				field.setEntity(entity);
				entity.getFields().add(field);
			}
		}
	}

	/**
	 * 解析数据源配置
	 * @param root
	 * @throws SolrConfigException 
	 * @throws ClassNotFoundException 
	 */
	private static void parseDatasourcesNode(Element root) throws SolrConfigException, ClassNotFoundException {
		
		SolrIndexDataConfig config = SolrConfigAccess.getConfig();
		
		Element datasourcesNode = root.element("dataSources");
		
		if(datasourcesNode == null) {
			throw new SolrConfigException("<dataSources>标签未定义!!!");
		}
		@SuppressWarnings("unchecked")
		List<Element> dsList = datasourcesNode.elements("dataSource");
		if(dsList == null || dsList.size() == 0) {
			throw new SolrConfigException("<dataSources>下至少要定义一个数据源<dataSource>!!!");
		}
		
		for(int i=0; i<dsList.size(); i++) {
			
			Element ds = dsList.get(i);
			String name = ds.attributeValue(SolrConstants.DATASOURCE_INFO_NAME);
			String driverClassName = ds.attributeValue(SolrConstants.DATASOURCE_INFO_DRIVERCLASSNAME);
			String url = ds.attributeValue(SolrConstants.DATASOURCE_INFO_URL);
			String username = ds.attributeValue(SolrConstants.DATASOURCE_INFO_USERNAME);
			String password = ds.attributeValue(SolrConstants.DATASOURCE_INFO_PASSWORD);
			String dbType = ds.attributeValue(SolrConstants.DATASOURCE_INFO_DBTYPE);
			
			//Assert.assertNotNull(name, "<datasource>:name不能为空");
			//Assert.assertNotNull(driverClassName, "<datasource>:driverClassName不能为空");
			//Assert.assertNotNull(url, "<datasource>:url不能为空");
			//Assert.assertNotNull(username, "<datasource>:username不能为空");
			//Assert.assertNotNull(password, "<datasource>:password不能为空");
			
			//获取数据源并存放在map中
			Class.forName(driverClassName);
			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName(driverClassName);
			dataSource.setUrl(url);
			dataSource.setUsername(username);
			dataSource.setPassword(password);
			
			//数据源
			Map<String, DataSource> dataSourceMap = config.getDataSourceMap();
			dataSourceMap.put(name, dataSource);
			
			//保存数据源配置信息
			if(dbType == null) {
				dbType = checkDBType(driverClassName);
			}
			Map<String, String> infoMap = new HashMap<String, String>();
			infoMap.put(SolrConstants.DATASOURCE_INFO_NAME, name);
			infoMap.put(SolrConstants.DATASOURCE_INFO_DRIVERCLASSNAME, driverClassName);
			infoMap.put(SolrConstants.DATASOURCE_INFO_URL, url);
			infoMap.put(SolrConstants.DATASOURCE_INFO_USERNAME, username);
			infoMap.put(SolrConstants.DATASOURCE_INFO_PASSWORD, password);
			infoMap.put(SolrConstants.DATASOURCE_INFO_DBTYPE, dbType);
			config.getDataSourceInfoMap().put(name, infoMap);
			
			//第一个数据源同时设置为默认数据源
			if(i == 0) {
				dataSourceMap.put(SolrConstants.DEFAULT_DATASOURCE_NAME, dataSource);
			}
			
		}
		
	}
	
	/**
	 * 根据数据源驱动判断数据库类型
	 * @param driverClassName
	 * @return
	 * @throws SolrConfigException
	 */
	private static String checkDBType(String driverClassName) throws SolrConfigException {
		/*
		 * oracle.jdbc.driver.OracleDriver
		 * com.mysql.jdbc.Driver
		 * com.microsoft.jdbc.sqlserver.SQLServerDriver
		 */
		String dbType = null;
		
		if("oracle.jdbc.driver.OracleDriver".equalsIgnoreCase(driverClassName)) {
			dbType = "oracle";
		} else if("com.mysql.jdbc.Driver".equalsIgnoreCase(driverClassName)) {
			dbType = "mysql";
		}
		
		if(dbType == null) {
			throw new SolrConfigException("数据源配置的[driverClassName]确定数据库类型," +
							"修改配置文件或者在com.sysc.solrServer.indexConfig.parser.SolrConfigParser#checkDBType中调整.");
		}
		return dbType;
	}

	/**
	 * 配置文件校验 
	 * 		1、同一个document下不能有重复的field
	 * 		2、
	 * @param doc xml的document对象根对象
	 * @return
	 * @throws SolrConfigException 
	 */
	@SuppressWarnings("unchecked")
	private static boolean validate(Document document) throws SolrConfigException {
		boolean flag = true;
		/*
		 * datasource不能同名
		 * document不能同名
		 * 同一个document下field不能同名 
		 */
		List<Element> dsList = document.selectNodes("//dataSource");
		Set<String> dsSet = new HashSet<String>();
		for(Element ds : dsList) {
			String name = ds.attributeValue("name");
			String driverClassName = ds.attributeValue("driverClassName");
			String url = ds.attributeValue("url");
			String username = ds.attributeValue("username");
			String password = ds.attributeValue("password");
			String dbType = ds.attributeValue("dbType");
			if(checkNull(name, driverClassName,url, username,password)) {
				throw new SolrConfigException("<dataSource>标签的name, driverClassName,url,username,password属性不能为空.");
			}
			
			//如果用户自定义了数据库类型,判断一下该类型是否合法
			if(!StringUtils.isBlank(dbType)) {
				String[] supportDB = new String[]{"oracle", "mysql"};
				boolean rightDBName = false;
				for(String db : supportDB) {
					if(db.equalsIgnoreCase(dbType)) {
						rightDBName = true;
						break;
					}
				}
				if(!rightDBName) {
					throw new SolrConfigException("<dataSource>标签的dbType属性当前仅支持[oracle, mysql],若要扩展,请调整代码.");
				}
			}

			if(dsSet.contains(name)) {
				throw new SolrConfigException("<dataSource>标签的name属性重复：[" + name + "]");
			} else {
				dsSet.add(name);
			}
		}
		
		//检查document有无重名
		List<Element> docList = document.selectNodes("//document");
		Set<String> docNameSet = new HashSet<String>();
		for(Element doc : docList) {
			String name = doc.attributeValue("name");
			String core = doc.attributeValue("core");
			String taskTableName = doc.attributeValue("taskTableName");
			String dataSource = doc.attributeValue("dataSource");
			if(checkNull(name, core,taskTableName, dataSource)) {
				throw new SolrConfigException("<document>标签的name, core,taskTableName,dataSource属性不能为空.");
			}
			if(docNameSet.contains(name)) {
				throw new SolrConfigException("<document>标签的name属性重复：[" + name + "]");
			} else {
				docNameSet.add(name);
			}
			
			//检查当前document下的field是否有重复的情况
//			List<Element> elements = doc.selectNodes("//field");
			List<Element> elements = doc.selectNodes("entity/field");
			Set<String> fieldNameSet = new HashSet<String>();
			for(Element field : elements) {
				String fieldName = field.attributeValue("name");
				String fieldColumn = field.attributeValue("column");
				if(checkNull(fieldName, fieldColumn)) {
					throw new SolrConfigException("<field>标签的name, column属性不能为空.");
				}
				if(fieldNameSet.contains(fieldName)) {
					throw new SolrConfigException("<document name='" + name + "'>标签下<field> name 属性重复：[" + fieldName + "]");
				} else {
					fieldNameSet.add(fieldName);
				}
			}
		}
		
		return flag;
		
	}
	
	/**
	 * 检查对象是否为空
	 * @param objs
	 * @return
	 */
	private static boolean checkNull(Object...objs) {
		boolean flag = false;
		if(objs == null) {
			flag = true;
		} else {
			for(Object o : objs) {
				if(o == null) {
					flag = true;
					break;
				}
			}
		}
		return flag;
	}
	
}
