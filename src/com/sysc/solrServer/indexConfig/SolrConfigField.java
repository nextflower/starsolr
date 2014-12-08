package com.sysc.solrServer.indexConfig;

/**
 * 对应solrIndexConfig中的field标签
 * 
 * @author wz
 *
 */
public class SolrConfigField {
	
	/*
	 * schema中对应的字段名
	 */
	private String name;
	/*
	 * 数据库表中的字段名
	 */
	private String column;
	/*
	 * 该字段的权重
	 */
	private float boost;
	
	/*
	 * 该字段所属的entity
	 */
	private SolrEntity entity;
	
	public SolrConfigField() {}
	
	public SolrConfigField(String name, String column, float boost) {
		this.name = name;
		this.column = column;
		this.boost = boost;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public String getColumn() {
		return column;
	}
	public void setBoost(float boost) {
		this.boost = boost;
	}
	public float getBoost() {
		return boost;
	}

	public void setEntity(SolrEntity entity) {
		this.entity = entity;
	}

	public SolrEntity getEntity() {
		return entity;
	}
	
}
