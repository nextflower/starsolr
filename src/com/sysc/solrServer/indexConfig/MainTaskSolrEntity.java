package com.sysc.solrServer.indexConfig;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.sysc.solrServer.common.SolrConstants;
import com.sysc.solrServer.common.StrUtils;
import com.sysc.solrServer.context.Global;

/**
 * 主任务表entity执行逻辑,区别于其他entity,因为该entity是程序的入口,每次遍历都要生成一个SolrInputDocument
 * 对应于solrIndexConfig.xml中document下的根entity,每个document只能有一个。
 * 
 * @author wz
 *
 */
public final class MainTaskSolrEntity extends CommonSolrEntity {
	
	//每次处理的数量：优先级：fetchSize---SQL语句中定义的结果集数量
	private Integer fetchSize;

	@Override
	public void parseResultSet(ResultSet rs, Map<String, Set<String>> map)
			throws SQLException, SolrServerException, IOException {
		
		long total = 0;
		int count = 1;
		//重新实现逻辑
		while(rs.next()) {
			count++;
			int status = rs.getInt(SolrConstants.TASK_STATUS_FIELD); 
			if(status == 1) {
				//删除操作
				String id = rs.getString(SolrConstants.TASK_SOLR_PK);
				deleteSolrDocumentById(id);
			} else {
				
				long t = System.currentTimeMillis();
				
				//新增或删除操作
				map = new HashMap<String, Set<String>>();
				
				//首先封装基本属性
				putFieldValueToMap(rs, map);
				
				//调用子类entity
				processChildren(map);
				
				long tt = System.currentTimeMillis() - t;
				
				total += tt;
				
				Global.getLogger().debug("生成一条记录耗时:" + tt + "毫秒--平均" + (total * 1.0 / count));
				
				//调用完毕之后,map获取到所有的属性
				//TODO 使用上下文工具类将map封装为SolrInputDocument,并调用server.add(document);方法。
				addToSolrIndex(map);
				
			}
		}
	}


	/**
	 * 删除索引记录
	 * @param id
	 */
	private void deleteSolrDocumentById(String id) {
		try {
			getDocument().getServer().deleteById(id);
		} catch (SolrServerException e) {
			Global.getLogger().error(id, e);
		} catch (IOException e) {
			Global.getLogger().error(id, e);
		}
	}
	
	
	/**
	 * 添加到索引库中
	 * @param map
	 * @throws IOException 
	 * @throws SolrServerException 
	 */
	private void addToSolrIndex(Map<String, Set<String>> map) throws SolrServerException, IOException {
		long t = System.currentTimeMillis();
		SolrInputDocument doc = new SolrInputDocument();
		for(String key : map.keySet()) {
			Set<String> set = map.get(key);
			for(String str : set) {
				//去除所有的HTML标签
				if(!StringUtils.isBlank(str)) {
					doc.addField(key, StrUtils.deleteAllHTMLTag(str));
				}
			}
		}
		SolrConfigDocument configDocument = getDocument();
		HttpSolrServer server = configDocument.getServer();
		
		Global.getLogger().info("添加:" + doc);
		
		server.add(doc);
		
		Global.getLogger().debug("添加单个doc耗时:" + (System.currentTimeMillis() - t) + "毫秒");
		
	}

	/**
	 * 删除任务表中已经入库的记录
	 * @throws SQLException 
	 */
	public void deleteTaskRecord() throws SQLException {
		
		String realQuery = getDocument().getTaskDeleteSQL();
		
		Global.getLogger().info(realQuery);
		
		Connection conn = getConnection();
		Statement st = null;
		try {
			conn.setAutoCommit(false);
			st = conn.createStatement();
			st.executeUpdate(realQuery);
			conn.commit();
		} catch (SQLException e) {
			//回滚
			if(conn != null) {
				conn.rollback();
			}
			Global.getLogger().error(realQuery, e);
		} finally {
			//只关闭结果集
			closeAllDatabaseResource(null, st, null);
		}
		
	}
	
	/**
	 * 任务表中是否还有未入库的记录
	 * @return
	 * @throws SQLException 
	 */
	public boolean hasMoreRecord() throws SQLException {
		
		Connection conn = getConnection();
		Statement st = null;
		ResultSet rs = null;
		
		try {
			st = conn.createStatement();
			
			Global.getLogger().debug(getDocument().getTaskQuerySQL());
			
			rs = st.executeQuery(getDocument().getTaskQuerySQL());
			
			return rs.next();
			
		} catch (Exception e) {
			Global.getLogger().error(getDocument().getTaskQuerySQL(), e);
		} finally {
			//只关闭结果集
			closeAllDatabaseResource(null, st, rs);
		}
		return false;
	}
	
	public void setFetchSize(Integer fetchSize) {
		this.fetchSize = fetchSize;
	}

	public Integer getFetchSize() {
		return fetchSize;
	}

}
