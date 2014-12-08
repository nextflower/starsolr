package com.sysc.solrServer.exception;

/**
 * 创建任务表、触发器产生的相关异常
 * 
 * @author wz
 *
 */
public class SolrExecuteException extends SolrException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -218980119803301806L;

	public SolrExecuteException() {
	}

	public SolrExecuteException(String message) {
		super(message);
	}

	public SolrExecuteException(Throwable cause) {
		super(cause);
	}

	public SolrExecuteException(String message, Throwable cause) {
		super(message, cause);
	}

}
