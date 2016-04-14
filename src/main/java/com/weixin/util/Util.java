package com.weixin.util;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

public class Util {

	/**
	 * 获取默认令牌
	 * @return<br/>
	 * 
	 * @author qiuwanchi<br/>
	 * @date: 2016年3月4日 <br/>
	 */
	public static String getDefaultToken() {
		return Config.getString("Token");
	}
	
	public static String getAppId() {
		return Config.getString("AppID");
	}
	
	public static String getSecret() {
		return Config.getString("AppSecret");
	}

	/**
	 * 获取签名
	 * 
	 * @param list
	 * @return<br/>
	 * @author qiuwanchi<br/>
	 * @date: 2016年3月4日 <br/>
	 */
	public static String getSignature(List<String> list) {
		Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();

		for (String str : list) {
			sb.append(str);
		}

		return DigestUtils.shaHex(sb.toString());
	}
	
	public static void writJson(HttpServletResponse response,String str){
		try {
			response.getWriter().print(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
