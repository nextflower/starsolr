package com.sysc.solrServer.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sysc.solrServer.utils.RequestUtils;

/**
 * 自定义Solr应用访问过滤器:
 * 主要目的是记录查询语句等到日志中,便于分析错误.
 * @author wz
 *
 */
public class SolrAccessFilter implements Filter {

	public void destroy() {
	}

	public void doFilter(ServletRequest req, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		
		
		/**
		 * 记录来访请求信息
		 */
		HttpServletRequest request = (HttpServletRequest) req;
		String requestURI = request.getRequestURI();
		String ipAddr = RequestUtils.getIpAddr(request);
		
		//排除掉
		if(needWrite2Log(requestURI)) {
			Map<String, Object> params = RequestUtils.getQueryParams(request);
			Logger.getLogger("query").info("IP为[" + ipAddr + "]的主机访问[" + requestURI + "],请求参数为[" + params + "]");
		}
		
		//放行
		chain.doFilter(request, response);
	}

	public void init(FilterConfig arg0) throws ServletException {
	}
	
	private boolean needWrite2Log(String requestURI) {
		boolean result = false;
		if(!StringUtils.isBlank(requestURI)) {
			if(requestURI.contains("/select")) {
				result = true;
			}
		}
		return result;
	}

}
