package com.sysc.solrServer.exception;


/**
 * solr相关异常
 * 
 * @author wz
 *
 */
public class SolrException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3025804977503661041L;

	public SolrException() {
	}

	public SolrException(String message) {
		super(message);
	}

	public SolrException(Throwable cause) {
		super(cause);
	}

	public SolrException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
