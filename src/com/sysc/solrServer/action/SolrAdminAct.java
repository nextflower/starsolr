package com.sysc.solrServer.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sysc.solrServer.main.entity.SolrFile;
import com.sysc.solrServer.utils.FrontUtils;
import com.sysc.solrServer.utils.RequestUtils;

@Controller
public class SolrAdminAct {
	
	@RequestMapping("/log/list.do")
	public String testDecode(HttpServletRequest request, ModelMap model) {
		FrontUtils.frontData(request, model);
		return "admin/log/list";
	}
	
	@RequestMapping("/log/tree.do")
	public String logTree(String root, HttpServletRequest request, ModelMap model) {
		if(StringUtils.isBlank(root)) {
			root = "/WEB-INF/logs";
		}
		String realPath = RequestUtils.getRealPath(request, root);
		SolrFile sf = new SolrFile(realPath, RequestUtils.getRealPath(request, "/"));
		model.addAttribute("solrFile", sf);
		FrontUtils.frontData(request, model);
		return "admin/log/tree";
	}
	
	@RequestMapping("/log/showLogText.do")
	public String showLogText(String name, HttpServletRequest request, ModelMap model) {
		String realPath = RequestUtils.getRealPath(request, name);
		SolrFile sf = new SolrFile(realPath, RequestUtils.getRealPath(request, "/"));
		model.addAttribute("solrFile", sf);
		FrontUtils.frontData(request, model);
		return "admin/log/edit";
	}

}
