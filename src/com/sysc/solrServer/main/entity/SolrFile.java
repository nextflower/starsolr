package com.sysc.solrServer.main.entity;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.sysc.solrServer.common.SolrConstants;
import com.sysc.solrServer.common.StrUtils;


/**
 * 文件对象,对File进行了封装,主要用于前台显示
 * @author wz
 *
 */
public class SolrFile implements Serializable{
	
	private File file = null;
	
	//应用的根目录
	private String root = null;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7086117799226400860L;
	
	/**
	 * 
	 * @param filePath
	 * @param root
	 */
	public SolrFile(String filePath, String root) {
		setFile(new File(filePath));
		this.setRoot(root);
		validate();
	}
	
	/**
	 * 
	 * @param file
	 * @param root
	 */
	public SolrFile(File file, String root) {
		this.setFile(file);
		this.setRoot(root);
		validate();
	}
	
	private void validate() {
		if(this.root == null) {
			throw new NullPointerException("必须指定当前应用的根目录root!!!");
		}
	}

	public void setFile(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}
	
	/**
	 * 获取子级文件
	 * @return
	 */
	public List<SolrFile> getChilds() {
		List<SolrFile> result = new ArrayList<SolrFile>();
		if(this.file.exists()) {
			File[] childs = file.listFiles();
			if(childs != null) {
				for(File f : childs) {
					result.add(new SolrFile(f, this.root));
				}
			}
		}
		Collections.sort(result, new Comparator<SolrFile>() {
			public int compare(SolrFile sf1, SolrFile sf2) {
				int result = 0;
				if(sf1.isDirectory() && !sf2.isDirectory()) {
					result = -1;
				} else if(!sf1.isDirectory() && sf2.isDirectory()) {
					result = 1;
				}
				return result;
			}
		});
		return result;
	}
	
	/**
	 * 获取父目录
	 * @return
	 */
	public SolrFile getParent() {
		File parentFile = getFile().getParentFile();
		if(parentFile != null) {
			return new SolrFile(parentFile, this.root);
		}
		return null;
	}
	
	public boolean isDirectory() {
		return getFile().isDirectory();
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getRoot() {
		return root;
	}
	
	public String getName() {
		return getFile().getName();
	}
	
	public String getRelativeRoot() {
		return StrUtils.formatPath("/" + getFile().getAbsolutePath().replace(this.root, ""));
	}
	
	public String getSource() {
		if (file.isDirectory() || !file.exists()) {
			return "";
		}
		try {
			return FileUtils.readFileToString(this.file, SolrConstants.UTF_8).trim();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
