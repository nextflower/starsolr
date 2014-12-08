package com.sysc.solrServer.context;


import org.apache.log4j.Logger;


/**
 * 
 * @author wz
 *
 */
public final class Global
{
	
	//查询条件日志记录器
	public static Logger queryLog = Logger.getLogger("query");
	//全局日志
	public static Logger commonLog = Logger.getLogger("root");
	
	/**
	 * 获取全局日志记录器
	 * @return
	 */
	public static Logger getLogger() {
		return commonLog;
	}
	
}
