package com.sysc.solrServer.indexConfig;

import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.solr.client.solrj.SolrServerException;

import com.sysc.solrServer.common.EPSJdbcUtil;
import com.sysc.solrServer.common.SolrConstants;
import com.sysc.solrServer.context.Global;

/**
 * SolrEntity适配器类,提供SolrEntity子类的通用方法
 * 
 * @author wz
 *
 */
public abstract class AbstractSolrEntity implements SolrEntity {
	
	/*
	 * 封装了数据库连接的本地对象
	 */
	private ThreadLocal<Connection> local = new ThreadLocal<Connection>();
	
	/*
	 * 查询语句: 例如：select * from eps_reslib_3;
	 */
	private String query;
	
	/*
	 * 当前entity的名称,需要具有唯一性
	 */
	private String name;
	
	/*
	 * 子entity列表
	 */
	private ArrayList<SolrEntity> childEntityList = new ArrayList<SolrEntity>();
	
	/*
	 * 父类entity
	 */
	private SolrEntity parent;
	
	/*
	 * 该类所属的document
	 */
	private SolrConfigDocument document;
	
	/*
	 * 当前entity所有的字段
	 */
	private List<SolrConfigField> fields = new ArrayList<SolrConfigField>();
	
	private Map<String, String> attrMap = new HashMap<String, String>();
	
	public void process(Map<String, Set<String>> map) throws SQLException, SolrServerException, IOException {
		Connection conn = getConnection();
		Statement st = null;
		ResultSet rs = null;
		String sql = getRealQuery(map);
		try {
			st = conn.createStatement();
			/** 获取结果集 */
			rs = st.executeQuery(sql);
			parseResultSet(rs, map);
		} catch (Exception e) {
			Global.getLogger().error(sql, e);
		} finally {
			//只关闭结果集
			closeAllDatabaseResource(null, st, rs);
		}
	}

	/**
	 * 由子类实现对结果集的处理逻辑
	 * 
	 * @param rs
	 * @param map
	 * @throws SQLException
	 */
	public abstract void parseResultSet(ResultSet rs, Map<String, Set<String>> map)  throws SQLException, SolrServerException, IOException ;

	/**
	 * 获取数据库连接
	 * @return
	 * @throws SQLException 
	 */
	protected Connection getConnection() throws SQLException {
		Connection conn = local.get();
		if(conn == null || conn.isClosed()) {
			//获取数据库连接,设置到local对象中
			Global.getLogger().debug("dataSourceName:" + getDocument().getDataSourceName());
			DataSource dataSource = SolrConfigAccess.getConfig().getDataSourceMap().get(getDocument().getDataSourceName());
			conn = dataSource.getConnection();
			local.set(conn);
		}
		return conn;
	}
	
	/**
	 * 关闭数据库连接
	 */
	public void closeConnection() {
		
		//先关闭子entity的数据库连接
		for(SolrEntity entity : childEntityList) {
			entity.closeConnection();
		}
		
		//关闭自身使用的数据库连接
		Connection conn = local.get();
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				Global.getLogger().error(e);
			}
		}
	}
	
	/**
	 * 关闭所有的数据库相关的资源
	 * @param conn
	 * @param st
	 * @param rs
	 */
	protected void closeAllDatabaseResource(Connection conn, Statement st, ResultSet rs) {
		try {
			if(rs != null) {
				rs.close();
			}
			if(st != null) {
				st.close();
			}
			if(conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			Global.getLogger().error(e);
		}
	}
	
	protected void processChildren(Map<String, Set<String>> map) {
		try {
			for(SolrEntity entity : getChildEntityList()) {
				entity.process(map);
				((AbstractSolrEntity)entity).processChildren(map);
			}
		} catch (SQLException e) {
			Global.getLogger().error(e);
		} catch (SolrServerException e) {
			Global.getLogger().error(e);
		} catch (IOException e) {
			Global.getLogger().error(e);
		}
	}
	
	/**
	 * 生成真实的查询语句
	 * @param map
	 * @return
	 */
	public String getRealQuery(Map<String, Set<String>> map) {
		
		String rawQuery = getQuery();
		
		if(map != null) {
			for(String key : map.keySet()) {
				if(rawQuery.contains(SolrConstants.FIELD_PREFIX + key + SolrConstants.FIELD_POST)) {
					String value = null;
					Set<String> set = map.get(key);
					for(String s : set) {
						value = s;
						break;
					}
					if(value == null) {
						throw new NullPointerException();
					}
					rawQuery = rawQuery.replace(SolrConstants.FIELD_PREFIX + key + SolrConstants.FIELD_POST, value);
				}
			}
		}
		
		if(rawQuery == null && getParent() == null) {
			//说明是根entity
			rawQuery = getDocument().getTaskQuerySQL();
			Global.getLogger().debug("realQuery:" + rawQuery);
		}
		
		return rawQuery;
	}
	
	/**
	 * 将数据库结果集中对应列的数据转换为String
	 * @param rs 结果集
	 * @param columnName 列名
	 * @return 字符串结果
	 * @throws SQLException
	 */
	protected String getResultVauleToString(ResultSet rs, String columnName) throws SQLException {
		String value = "";
		ResultSetMetaData metaData = rs.getMetaData();
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			String columnLabel = metaData.getColumnLabel(i);
			if(columnName.equalsIgnoreCase(columnLabel)) {
				String columnClassName = metaData.getColumnClassName(i);
				
				//转换,主要是针对特殊字段
				if ("oracle.sql.CLOB".equalsIgnoreCase(columnClassName)) {
					//TODO  将CLOB转换为字符串
					Clob clob = rs.getClob(columnLabel);
					value = EPSJdbcUtil.getCLOBString(clob);
				} else {
					//一般字段直接使用getString获取
					value = rs.getString(columnLabel);
				}
			}
		}
		return value;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}
	
	public void setChildEntityList(ArrayList<SolrEntity> childEntityList) {
		this.childEntityList = childEntityList;
	}

	public ArrayList<SolrEntity> getChildEntityList() {
		return childEntityList;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setParent(SolrEntity parent) {
		this.parent = parent;
	}

	public SolrEntity getParent() {
		return parent;
	}

	public void setDocument(SolrConfigDocument document) {
		this.document = document;
	}

	/**
	 * 若当前entity没有制定document,则查找父类entity的document.
	 */
	public SolrConfigDocument getDocument() {
		if(document != null) {
			return document;
		}
		
		SolrConfigDocument doc = null;
		SolrEntity entity = null;
		while(doc == null) {
			entity = getParent();
			doc = entity.getDocument();
		}
		
		setDocument(doc);
		
		return doc;
	}
	

	
	public void setFields(List<SolrConfigField> fields) {
		this.fields = fields;
	}

	public List<SolrConfigField> getFields() {
		return fields;
	}

	public void setAttrMap(Map<String, String> attrMap) {
		
		this.attrMap = attrMap;
		String query = attrMap.get("query");
		//Assert.assertNotNull("<entity>:query不能为空", query);
		setQuery(query);
		
		//entity name的处理 
		String name = attrMap.get("name");
		if(name == null) {
			name = UUID.randomUUID().toString();
		} 
		setName(name);
	}

	public Map<String, String> getAttrMap() {
		return attrMap;
	}
	
}
