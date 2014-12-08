package com.sysc.solrServer.common;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 字符串的帮助类，提供静态方法，不可以实例化。
 * 
 * @author liufang
 * 
 */
public class StrUtils {
	/**
	 * 禁止实例化
	 */
	private StrUtils() {
	}

	/**
	 * 处理url
	 * 
	 * url为null返回null，url为空串或以http://或https://开头，则加上http://，其他情况返回原参数。
	 * 
	 * @param url
	 * @return
	 */
	public static String handelUrl(String url) {
		if (url == null) {
			return null;
		}
		url = url.trim();
		if (url.equals("") || url.startsWith("http://")
				|| url.startsWith("https://")) {
			return url;
		} else {
			return "http://" + url.trim();
		}
	}

	/**
	 * 分割并且去除空格
	 * 
	 * @param str
	 *            待分割字符串
	 * @param sep
	 *            分割符
	 * @param sep2
	 *            第二个分隔符
	 * @return 如果str为空，则返回null。
	 */
	public static String[] splitAndTrim(String str, String sep, String sep2) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		if (!StringUtils.isBlank(sep2)) {
			str = StringUtils.replace(str, sep2, sep);
		}
		String[] arr = StringUtils.split(str, sep);
		// trim
		for (int i = 0, len = arr.length; i < len; i++) {
			arr[i] = arr[i].trim();
		}
		return arr;
	}

	/**
	 * 文本转html
	 * 
	 * @param txt
	 * @return
	 */
	public static String txt2htm(String txt) {
		if (StringUtils.isBlank(txt)) {
			return txt;
		}
		StringBuilder sb = new StringBuilder((int) (txt.length() * 1.2));
		char c;
		boolean doub = false;
		for (int i = 0; i < txt.length(); i++) {
			c = txt.charAt(i);
			if (c == ' ') {
				if (doub) {
					sb.append(' ');
					doub = false;
				} else {
					sb.append("&nbsp;");
					doub = true;
				}
			} else {
				doub = false;
				switch (c) {
				case '&':
					sb.append("&amp;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '"':
					sb.append("&quot;");
					break;
				case '\n':
					sb.append("<br/>");
					break;
				default:
					sb.append(c);
					break;
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 剪切文本。如果进行了剪切，则在文本后加上"..."
	 * 
	 * @param s
	 *            剪切对象。
	 * @param len
	 *            编码小于256的作为一个字符，大于256的作为两个字符。
	 * @return
	 */
	public static String textCut(String s, int len, String append) {
		if (s == null) {
			return null;
		}
		int slen = s.length();
		if (slen <= len) {
			return s;
		}
		// 最大计数（如果全是英文）
		int maxCount = len * 2;
		int count = 0;
		int i = 0;
		for (; count < maxCount && i < slen; i++) {
			if (s.codePointAt(i) < 256) {
				count++;
			} else {
				count += 2;
			}
		}
		if (i < slen) {
			if (count > maxCount) {
				i--;
			}
			if (!StringUtils.isBlank(append)) {
				if (s.codePointAt(i - 1) < 256) {
					i -= 2;
				} else {
					i--;
				}
				return s.substring(0, i) + append;
			} else {
				return s.substring(0, i);
			}
		} else {
			return s;
		}
	}

	/**
	 * 检查字符串中是否包含被搜索的字符串。被搜索的字符串可以使用通配符'*'。
	 * 
	 * @param str
	 * @param search
	 * @return
	 */
	public static boolean contains(String str, String search) {
		if (StringUtils.isBlank(str) || StringUtils.isBlank(search)) {
			return false;
		}
		String reg = StringUtils.replace(search, "*", ".*");
		Pattern p = Pattern.compile(reg);
		return p.matcher(str).matches();
	}

	public static boolean containsKeyString(String str) {
		if (StringUtils.isBlank(str)) {
			return false;
		}
		if (str.contains("'") || str.contains("\"") || str.contains("\r")
				|| str.contains("\n") || str.contains("\t")
				|| str.contains("\b") || str.contains("\f")) {
			return true;
		}
		return false;
	}

	// 将""和'转义
	public static String replaceKeyString(String str) {
		if (containsKeyString(str)) {
			return str.replace("'", "\\'").replace("\"", "\\\"").replace("\r",
					"\\r").replace("\n", "\\n").replace("\t", "\\t").replace(
					"\b", "\\b").replace("\f", "\\f");
		} else {
			return str;
		}
	}
	//单引号转化成双引号
	public static String replaceString(String str) {
		if (containsKeyString(str)) {
			return str.replace("'", "\"").replace("\"", "\\\"").replace("\r",
					"\\r").replace("\n", "\\n").replace("\t", "\\t").replace(
					"\b", "\\b").replace("\f", "\\f");
		} else {
			return str;
		}
	}

	public static void main(String args[]) {
		System.out.println(replaceKeyString("&nbsp;\r" + "</p>"));
	}
	
	/**
	 * 格式化路径,将所有的反斜杠\转换为正斜杠/
	 * @param path
	 * @return
	 */
	public static String formatPath(String path) {
		return path == null ? null : path.replaceAll("\\\\+", "/").replaceAll("/+", "/");
	}
	
	/**
	 * 判断一个数组是否为空数组
	 * @param arr
	 * @return
	 */
	public static Boolean isBlankArray(String[] arr) {
		
		if(arr == null || arr.length == 0) {
			return false;
		}
		
		for(String s : arr) {
			if(!StringUtils.isBlank(s)) {
				return false;
			}
		}
		
		return true;
	}
	
	public static String replaceFileName2Lower(String filename) {
		String name = FilenameUtils.getName(filename);
		if(filename.indexOf(name) + name.length() == filename.length()) {
			filename = filename.replace(name, name.toLowerCase());
		}
		return filename;
	}
	
	/**
	 * 讲文件名改为大写
	 * @param filename
	 * @return
	 */
	public static String replaceFileName2Upper(String filename) {
		String name = FilenameUtils.getName(filename);
		if(filename.indexOf(name) + name.length() == filename.length()) {
			filename = filename.replace(name, name.toUpperCase());
		}
		return filename;
	}
	
	/**
	 * 将文件后缀名转换为大写格式
	 * 
	 * @param filename
	 * @return
	 */
	public static String replaceExt2Upper(String filename) {
		String ext = FilenameUtils.getExtension(filename);
		if(ext == null) {
			return filename;
		} else {
			if(filename.indexOf(ext) + ext.length() == filename.length()) {
				filename = filename.replace(ext, ext.toUpperCase());
			}
			return filename;
		}
	}
	
	/**
	 * 将文件后缀名转换为小写格式
	 * 
	 * @param filename
	 * @return
	 */
	public static String replaceExt2Lower(String filename) {
		String ext = FilenameUtils.getExtension(filename);
		if(ext == null) {
			return filename;
		} else {
			if(filename.indexOf(ext) + ext.length() == filename.length()) {
				filename = filename.replace(ext, ext.toLowerCase());
			}
			return filename;
		}
	}
	
	/**
	 * 字符串转大写
	 * @param str
	 * @return
	 */
	public static String toUpperCase(String str) {
		return str == null ? null : str.toUpperCase();
	}
	
	/**
	 * 字符串转小写
	 * @param str
	 * @return
	 */
	public static String toLowerCase(String str) {
		return str == null ? null : str.toLowerCase();
	}
	
	/**
	 * 字符串转整型
	 * 
	 * @param str 要转换的字符串
	 * @return
	 */
	public static Integer parseInt(String str) {
		if(str == null || "".equals(str.trim())) {
			return 0;
		} else {
			return Integer.parseInt(str);
		}
	}
	
	/**
	 * 字符串转日期
	 * 
	 * @param formatPattern 转换规则
	 * @param publishDate 待转换字符串
	 * @return
	 */
	public static Date parseDate(String formatPattern ,String dateStr) {
		Date date = null;
        SimpleDateFormat sdf=new SimpleDateFormat(formatPattern); 
        try {
			date= sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	/**
	 * 返回一个限定长度的字符串
	 * @param str
	 * @param length
	 * @return
	 */
	public static String limitStringLength (String str, int length) {
		if(str == null) {
			return null;
		}
		return str.length() > length ? str.substring(0,length) : str;
	}
	
	/**
	 * 删除所有的HTML标签
	 * 
	 * @param source 需要进行除HTML的文本
	 * @return
	 */
	public static String deleteAllHTMLTag(String source) {
		
		if(source == null) {
			return "";
		}
		
		String s = source;
		/** 删除普通标签  */
		s = s.replaceAll("<(S*?)[^>]*>.*?|<.*? />", "");
		/** 删除转义字符 */
		s = s.replaceAll("&.{2,6}?;", "");
		return s;
	}
	
	/**
	 * 判断是否为中文字符
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c) {  
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);  
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS  
  
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS  
  
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A  
  
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION  
  
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION  
  
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {  
  
            return true;  
        }  
        return false;  
    } 
	
	
	/**
	 * 判断是否为标点符号
	 * @param c
	 * @return
	 */
	public static boolean isBiaoDian(char c) {
		String s = new String(new char[]{c});
		return s.matches("\\pP");
	}
	
	/**
	 * 重命名文件名
	 * @param file
	 */
	public static void renameAllToUpcase(File file){
		
		  File [] files = file.listFiles();
		  for(File f: files) {
			  
			   if(f.isDirectory()) {
				   renameAllToUpcase(f);
			   }
			   
			   if(f.isFile()) {
				   String parent = f.getParent();
				   String name = f.getName();
				   String newFile = parent + "/" + name.toUpperCase();
				   boolean flag = f.renameTo(new File(newFile));
				   //System.out.println("newFIle:" + newFile);
				   //System.out.println();
			   }
		  }
	}
	
	//是否为IP格式
	public static boolean isboolIp(String ipAddress) {  
	       //String ip = "([1-9]|[1-9]//d|1//d{2}|2[0-4]//d|25[0-5])(//.(//d|[1-9]//d|1//d{2}|2[0-4]//d|25[0-5])){3}";   
	       String ip = "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$";   
	       Pattern pattern = Pattern.compile(ip);   
	       Matcher matcher = pattern.matcher(ipAddress);   
	       return matcher.matches();   
	}  

}
