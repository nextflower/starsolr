package com.sysc.solrServer.common;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;

public class EPSJdbcUtil {

	/**
	  * 将数据库CLOB对象中的文本读取到String中.
	  * 
	  * @param clob 数据库clob对象 
	  * @return
	  */
	public static String getCLOBString(Clob clob) {
		
		if(clob == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		
		try {
			
			Reader in = clob.getCharacterStream();
			
			int len = 0;
			
			char[] buf = new char[1024];
			
			while((len = in.read(buf)) != -1) {
				sb.append(buf,0,len);
			}
			in.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
}
