package com.sysc.solrServer.task;

import java.io.IOException;
import java.sql.SQLException;
import java.util.TimerTask;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import com.sysc.solrServer.context.Global;
import com.sysc.solrServer.indexConfig.MainTaskSolrEntity;
import com.sysc.solrServer.indexConfig.SolrConfigAccess;
import com.sysc.solrServer.indexConfig.SolrConfigDocument;

/**
 * 定时器任务
 * 
 * @author wz
 *
 */
public class SolrIndexTask extends TimerTask {

	@Override
	public void run() {
		
		if(SolrConfigAccess.getConfig().getDocumentList() != null) {
			for(SolrConfigDocument doc : SolrConfigAccess.getConfig().getDocumentList()) {
				//如果该document没有启用
				if(!doc.getOnUse()) {
					continue;
				}
				
				MainTaskSolrEntity entity = doc.getEntity();
				
				try {
					
					HttpSolrServer server = doc.getServer();
					
					//向索引库中新增记录,全部新增完毕之后继续下一步
					while (entity.hasMoreRecord()) {
						
						//执行生成索引逻辑
						entity.process(null);
						
						//提交server.commit();
						if(!SolrConfigAccess.getConfig().isAutoCommit()) {
							server.commit();
						}
						
						//删除记录
						entity.deleteTaskRecord();
						
					}
						
				} catch (SQLException e) {
					Global.getLogger().error(e);
				} catch (SolrServerException e) {
					Global.getLogger().error(e);
				} catch (IOException e) {
					Global.getLogger().error(e);
				} finally {
					entity.closeConnection();
				}
			}
			
		}
		
	}

}
