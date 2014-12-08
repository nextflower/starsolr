package com.sysc.solrServer.exception;

/**
 * 解析配置文件产生的相关异常
 * 
 * @author wz
 *
 */
public class SolrConfigException extends SolrException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -218980119803301806L;

	public SolrConfigException() {
	}

	public SolrConfigException(String message) {
		super(message);
	}

	public SolrConfigException(Throwable cause) {
		super(cause);
	}

	public SolrConfigException(String message, Throwable cause) {
		super(message, cause);
	}

}
