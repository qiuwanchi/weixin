package com.weixin.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.weixin.util.HttpClientUtils;
import com.weixin.util.Util;

public class AccessTokenServlet extends HttpServlet{

	private static final long serialVersionUID = -3781723823076277442L;
	private static final Logger logger = Logger.getLogger(AccessTokenServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String url = "https://api.weixin.qq.com/cgi-bin/token";
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "client_credential");
		params.put("appid", Util.getAppId());
		params.put("secret", Util.getSecret());
		
				
		String str = HttpClientUtils.getInstance().go(url, true, params);
		System.out.println(str);
		
		Util.writJson(resp, str);
	}
	
	
}
