package com.weixin.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestUtils;

import com.weixin.util.Util;

/**
 * 微信对接接口
 * @author qiuwanchi<br/>
 * @date: 2016年4月14日 <br/>
 */
public class WeixinInterfaceServlet extends HttpServlet{

	private static final long serialVersionUID = -2261411544363206999L;
	private static final Logger logger = Logger.getLogger(WeixinInterfaceServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String signature = ServletRequestUtils.getStringParameter(request, "signature","");//微信加密签名
		String timestamp = ServletRequestUtils.getStringParameter(request, "timestamp","");//时间戳 
		String nonce = ServletRequestUtils.getStringParameter(request, "nonce","");//随机数
		String echostr = ServletRequestUtils.getStringParameter(request, "echostr","");//随机字符串
		
		List<String> list = new ArrayList<String>();
		list.add(Util.getDefaultToken());
		list.add(timestamp);
		list.add(nonce);
		logger.error(request.getInputStream());
		//签名认证
		if(!signature.equals(Util.getSignature(list))){
			logger.error("请求参数["+request.getQueryString()+"]签名不正确");
    		response.getWriter().print("");
    		return ;
		}
		
		response.getWriter().print(echostr);
		
	}
}
