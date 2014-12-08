package com.sysc.solrServer.indexConfig;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;

/**
 * 对应solrIndexConfig中的entity元素
 * 
 * @author wz
 *
 */
public interface SolrEntity {
	
	/**
	 * 获取名称
	 * @return
	 */
	String getName();
	
	/**
	 * 当前entity所属document
	 * @return
	 */
	SolrConfigDocument getDocument();
	
	/**
	 * 获取父类entity
	 * @return
	 */
	SolrEntity getParent();
	
	/**
	 * 获取当前entity的下属字段
	 * @return
	 */
	List<SolrConfigField> getFields();
	
	/**
	 * 获取当前entity的子类entity
	 * @return
	 */
	List<SolrEntity> getChildEntityList();
	
	/**
	 * 从当前entity中获取字段取值
	 * @param map {字段 ：取值}容器
	 */
	void process(Map<String, Set<String>> map) throws SQLException, SolrServerException, IOException;
	
	/**
	 * 关闭数据库连接
	 */
	void closeConnection();
	
	String getQuery();
	
}
