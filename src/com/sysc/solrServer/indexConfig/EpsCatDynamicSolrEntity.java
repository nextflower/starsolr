package com.sysc.solrServer.indexConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


import com.sysc.solrServer.common.SolrConstants;

/**
 * 包含动态字段的entity实现类,该类解决动态分类相关属性的获取
 * 
 * @author wz
 *
 */
public class EpsCatDynamicSolrEntity extends AbstractSolrEntity {
	
	/*
	 * 动态字段的前缀,改为配置的方式完成
	 */
	//public static String PREFIX_EPSCAT_DYNAMIC_FIELD = "EpsCatDynamic_";

	//权重
	private float boost;
	//作为索引库中字段名的数据库字段
	private String keyColumn;
	//作为索引库中字段值的数据库字段
	private String valueColumn;
	//动态字段的前缀
	private String fieldPrefix;
	
	public void setBoost(float boost) {
		this.boost = boost;
	}

	public float getBoost() {
		return boost;
	}


	public void setKeyColumn(String keyColumn) {
		this.keyColumn = keyColumn;
	}


	public String getKeyColumn() {
		return keyColumn;
	}


	public void setValueColumn(String valueColumn) {
		this.valueColumn = valueColumn;
	}


	public String getValueColumn() {
		return valueColumn;
	}


	@Override
	public void parseResultSet(ResultSet rs, Map<String, Set<String>> map)
			throws SQLException {
		
		Map<String, Set<String>> temp = new HashMap<String, Set<String>>();
		
		//拼接分类属性
		while (rs.next()) {
			
			String key = getResultVauleToString(rs, keyColumn);
			String value = getResultVauleToString(rs, valueColumn);
			
			Set<String> set = temp.get(fieldPrefix + key);
			if(set == null) {
				set = new HashSet<String>();
			}
			set.add(value);
			temp.put(fieldPrefix + key, set);
		}
		
		//将分类属性填充到map
		for(String key : temp.keySet()) {
			Set<String> set = temp.get(key);
			StringBuffer sb = new StringBuffer();
			for(String s : set) {
				sb.append(" " + s);
			}
			sb.delete(0, 1);
			Set<String> list = new TreeSet<String>();
			list.add(sb.toString());
			map.put(key, list);
		}
		
	}

	@Override
	public void setAttrMap(Map<String, String> attrMap) {
		super.setAttrMap(attrMap);
		//设置属性
		String keyColumn = attrMap.get("keyColumn");
		String valueColumn = attrMap.get("valueColumn");
		String fieldPrefix = attrMap.get("fieldPrefix");
		String boostStr = attrMap.get("boost");
		float boost = boostStr == null ? SolrConstants.DEFAULT_BOOST : Float.parseFloat(boostStr);
		
		//动态字段前缀若没有定义,则设置默认值
		if(fieldPrefix == null) {
			fieldPrefix = SolrConstants.DYNAMIC_FILED_PREFIX;
		}
		
//		Assert.assertNotNull("keyColumn不能为空", keyColumn);
//		Assert.assertNotNull("valueColumn不能为空", valueColumn);
//		Assert.assertNotNull("fieldPrefix不能为空", fieldPrefix);
		setKeyColumn(keyColumn);
		setBoost(boost);
		setValueColumn(valueColumn);
		setFieldPrefix(fieldPrefix);
	}

	public void setFieldPrefix(String fieldPrefix) {
		this.fieldPrefix = fieldPrefix;
	}

	public String getFieldPrefix() {
		return fieldPrefix;
	}


}
