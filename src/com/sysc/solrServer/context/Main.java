package com.sysc.solrServer.context;

import java.util.Timer;


import com.sysc.solrServer.indexConfig.parser.SolrConfigParser;
import com.sysc.solrServer.task.SolrIndexTask;

/**
 * 测试用程序入口
 * 
 * @author wz
 *
 */
public class Main {

	/*
	 * 此处为测试版本程序入口
	 * 	1、若要切换为正式版,当前的思路是将此处代码设置为通过网络访问的方式启动
	 * 	2、考虑给log4j的配置文件提供一个系统变量appdir
	 * 	3、
	 */
	public static void main(String[] args) {
		
		long t = System.currentTimeMillis();
		
//		String freemarkerRootDir = "D:/WORK/Solr/WebRoot/WEB-INF/template";
		
		//解析配置文件
		boolean parseSucc = SolrConfigParser.parse();
		
		if(parseSucc) {//解析成功
			
			//前置处理：生成sql语句和触发器语句并执行。
			//SQLExecutor.process(freemarkerRootDir);
			
			//启动定时器
			Timer timer = new Timer();
			timer.schedule(new SolrIndexTask(), 1000, 20 * 1000);
			
		}
		
		Global.getLogger().debug("耗时:" + (System.currentTimeMillis() - t) + "毫秒");
		
	}
	
}
