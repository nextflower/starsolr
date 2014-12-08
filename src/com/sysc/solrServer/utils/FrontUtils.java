package com.sysc.solrServer.utils;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * 前台显示工具类
 * @author wz
 *
 */
public class FrontUtils {
	
	public static final String BASE = "base";
	
	public static void frontData(HttpServletRequest request, Map<String, Object> model) {
		String contextPath = request.getContextPath();
		model.put(BASE, contextPath);
	}

}
