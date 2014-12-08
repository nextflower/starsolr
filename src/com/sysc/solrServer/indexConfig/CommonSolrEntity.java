package com.sysc.solrServer.indexConfig;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;

/**
 * 基本SolrEntity实现类,默认的非自定义动态字段对应的entity都是使用该实现类
 * 
 * @author wz
 *
 */
public class CommonSolrEntity extends AbstractSolrEntity {
	
	@Override
	public void parseResultSet(ResultSet rs, Map<String, Set<String>> map)
			throws SQLException, SolrServerException, IOException {
		while (rs.next()) {
			putFieldValueToMap(rs, map);
		}
	}

	/**
	 * 封装基本属性
	 * @param rs
	 * @param map
	 * @throws SQLException
	 */
	protected void putFieldValueToMap(ResultSet rs, Map<String, Set<String>> map)
			throws SQLException {
		for(SolrConfigField field : getFields()) {
			String column = field.getColumn();
			String value = getResultVauleToString(rs, column);
			String fieldName = field.getName();
			Set<String> set = map.get(fieldName);
			if(set == null) {
				set = new HashSet<String>();
				map.put(fieldName, set);
			}
			set.add(value);
		}
	}

}
