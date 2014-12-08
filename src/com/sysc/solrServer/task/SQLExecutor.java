package com.sysc.solrServer.task;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.io.FilenameUtils;

import com.sysc.solrServer.common.SolrConstants;
import com.sysc.solrServer.common.StrUtils;
import com.sysc.solrServer.context.Global;
import com.sysc.solrServer.exception.SolrExecuteException;
import com.sysc.solrServer.indexConfig.SolrConfigAccess;
import com.sysc.solrServer.indexConfig.SolrConfigDocument;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * 负责创建任务表和触发器文件并执行
 * 创建使用freemarker.
 * @author wz
 *
 */
public class SQLExecutor {
	
	public static final String SQL = "_sql";//建表标识
	public static final String TRIGGER = "_trigger";//触发器标识
	public static final String SQL_POST = ".sql";//后缀名
	public static final String SQL_DIR = "/sql/";//SQL模板文件存放位置
	public static final String TRIGGER_DIR = "/trigger/";//触发器模板文件存放位置
	public static final String FREEMARKER_POST = ".ftl";
	public static final String TASK_TABLE = "TASK_TABLE";//任务表的名称,必须固定
	
	/**
	 * 执行
	 * @param freemarkerRootDir 模板文件所在的目录,生成的sql及触发器文件会自动放在该目录下的sql文件夹下
	 */
	public static boolean process(String freemarkerRootDir) {
		
		boolean flag = false;
		try {
			//模板目录是否存在
			File root = new File(StrUtils.formatPath(freemarkerRootDir));
			if(root.exists()) {
				freemarkerRootDir = StrUtils.formatPath(freemarkerRootDir);
				
				List<SolrConfigDocument> documentList = SolrConfigAccess.getConfig().getDocumentList();
				for(SolrConfigDocument document : documentList) {
					
					//如果当前document没有启用,将不会进行下一步操作
					if(!document.getOnUse()) {
						continue;
					}
					
					//获取core名称,用于拼接模板存放路径
					String coreName = document.getCore();//core8
					String coreTemplatePath = freemarkerRootDir + "/" + coreName;
					
					//DDL语句生成及执行
					handle(document, coreTemplatePath + SQL_DIR, true);
					
					//触发器语句生成及执行
					handle(document, coreTemplatePath + TRIGGER_DIR, false);
					
				}
				flag = true;
			} else {
				throw new SolrExecuteException("文件目录[" + root.getAbsolutePath() + "]不存在!!!");
			}
			
		} catch (SolrExecuteException e) {
			Global.getLogger().error(e);
		}
		
		return flag;
		
	}
	
	private static void handle(SolrConfigDocument document, String dirPath, boolean execute) {
		
		try {
			
			//遍历该文件夹下的ftl后缀的文件,用于生成对应的脚本文件
			File sqlDirFile = new File(dirPath);
			File[] ftlFiles = sqlDirFile.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return "ftl".equalsIgnoreCase(FilenameUtils.getExtension(name));
				}
			});
			
			if(ftlFiles != null) {
				for(File ftlFile : ftlFiles) {
					boolean needCreate = false;
					String target = ftlFile.getAbsolutePath().replace(FREEMARKER_POST, SQL_POST);
					
					File targetFile = new File(target);
					//如果存在,对比两个文件的最后修改时间
					if(targetFile.exists()) {
						long tModify = ftlFile.lastModified();
						long sModify = targetFile.lastModified();
						if(tModify > sModify) {
							needCreate = true;
						}
					} else {
						needCreate = true;
					}
					
					if(needCreate) {
						
						//配置freemarker
						Configuration cfg = new Configuration();
						cfg.setDirectoryForTemplateLoading(new File(dirPath));//freemarker从什么地方加载模板文件  
						cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);//忽略异常  
						
						//生成脚本文件
						Map<String, Object> rootMap = new HashMap<String, Object>();
						rootMap.put("taskTableName", document.getTaskTableName().toUpperCase());
						String taskTableShellPath = processTpl(cfg, ftlFile.getName(), target, rootMap);
						
						//执行脚本
						if(execute) {
							excuteJDBCSQL(document, readFileToString(new File(taskTableShellPath)));
						}
						
					}
				}
			}
		} catch (IOException e) {
			Global.getLogger().error(e);
		}
		
	}

	/**
	 * 执行sql脚本
	 * @param document
	 * @param taskTableShellSql
	 * @param triggerShellFileNames
	 */
	private static void excuteJDBCSQL(SolrConfigDocument document, String...triggerShellFileNames) {
		DataSource dataSource = SolrConfigAccess.getConfig().getDataSourceMap().get(document.getDataSourceName());
		Connection conn = null;
		Statement st = null;
		try {
			conn = dataSource.getConnection();
			st = conn.createStatement();
			//开启事务
			conn.setAutoCommit(false);
			
			//执行SQL脚本
			for(String sql : triggerShellFileNames) {
				st.execute(sql);
				Global.getLogger().info(sql + "执行完毕!");
			}
			
			//提交
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Global.getLogger().error(e1);
			}
			Global.getLogger().error(e);
		} finally {
			try {
				if(st != null) {
					st.close();
				}
				if(conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				Global.getLogger().error(e);
			}
		}
		
	}

	/**
	 * 通过freemarker生成对应的sql和触发器语句
	 * @param template
	 * @param destPath
	 * @param model
	 * @return
	 */
	private static String processTpl(Configuration cfg, String templateName, String destPath, @SuppressWarnings("rawtypes") Map model) {
		
		//判断目标路径是否已经存在，若不存在就创建文件路径
		File destFile = new File(destPath);
		
        Writer out = null;
        try {
        	if(!destFile.exists()) {
    			destFile.createNewFile();
    		}
        	Template template = cfg.getTemplate(templateName, SolrConstants.UTF_8);
			out = new OutputStreamWriter(new FileOutputStream(destFile), SolrConstants.UTF_8);
			template.process(model, out);
		} catch (IOException e) {
			Global.getLogger().error(e);
		} catch (TemplateException e) {
			Global.getLogger().error(e);
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				Global.getLogger().error(e);
			}
		}
		return destPath;
	}
	
	/**
	 * 从文件中读取字符串
	 * @param file
	 * @return
	 */
	private static String readFileToString(File file) {
		StringBuffer sb = new StringBuffer();
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			
			String temp = null;
			while((temp = br.readLine())!=null)
			{
				sb.append(temp);
				sb.append(" ");
			}
		} catch (FileNotFoundException e) {
			Global.getLogger().error(e);
		} catch (IOException e) {
			Global.getLogger().error(e);
		} finally{
			try {
				fis.close();
				isr.close();
				br.close();
			} catch (IOException e) {
				Global.getLogger().error(e);
			}
		}
		return sb.toString();
	}

}
