package com.sysc.solrServer.context;


import java.util.Timer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.sysc.solrServer.indexConfig.SolrConfigAccess;
import com.sysc.solrServer.indexConfig.parser.SolrConfigParser;
import com.sysc.solrServer.task.SolrIndexTask;

public class IndexWriterListener extends HttpServlet implements
		ServletContextListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1702699584891803241L;


	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}


	public void init() throws ServletException {
		
//		String realPath = getServletContext().getRealPath("/WEB-INF");
//		System.setProperty("myAppfuse.root", realPath);
		
		//解析配置文件
		boolean parseSucc = SolrConfigParser.parse();
		
//		Global.debug(SolrConfigAccess.getConfig().getDelay(), true);
//		Global.debug(SolrConfigAccess.getConfig().getPeriod(), true);
		
//		String delayStr = getInitParameter("delay");
//		String periodStr = getInitParameter("period");
//		
//		long delay = SolrConfigAccess.getConfig().getDelay();
//		if(!StringUtils.isBlank(delayStr)) {
//			delay = Long.parseLong(delayStr);
//		}
//		
//		long period = SolrConfigAccess.getConfig().getPeriod();
//		if(!StringUtils.isBlank(periodStr)) {
//			period = Long.parseLong(periodStr);
//		}
//		
		if(parseSucc) {//解析成功
			//启动定时器
			Timer timer = new Timer();
			timer.schedule(new SolrIndexTask(), 
								SolrConfigAccess.getConfig().getDelay(), 
								SolrConfigAccess.getConfig().getPeriod());
		}
		
	}

}
