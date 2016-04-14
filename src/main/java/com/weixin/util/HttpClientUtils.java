package com.weixin.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpClientUtils {

	private static final String DEFAULT_CHARSET_UTF8 = "UTF-8";
	private static final int DETAULT_TRY_COUNT = 1;
	private static final String DEFAULT_SCHEMA = "http://";
	protected static Logger log = Logger.getLogger(HttpClientUtils.class);

	private static class LazyHolder {
		private static final HttpClientUtils instance = new HttpClientUtils();
	}

	public static HttpClientUtils getInstance() {
		return LazyHolder.instance;
	}

	public interface ResultListener {
		public void onConnectionPoolTimeoutError();
	}

	// 连接超时时间，默认10秒
	private int socketTimeout = 60000;

	// 传输超时时间，默认30秒
	private int connectTimeout = 60000;

	// 请求器的配置
	private RequestConfig requestConfig;

	private HttpClientUtils() {
		try {
			init();
		} catch (Exception e) {
			log.error("HTTP请求器启动失败", e);
		}
	}

	private void init() throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException,
			KeyManagementException {
		// 根据默认超时限制初始化requestConfig
		requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout)
				.build();
	}

	public CloseableHttpClient getHttpClient(String url) {
		if (url != null && url.startsWith("http://")) {
			return HttpClients.createDefault();
		}
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				// 信任所有
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		return HttpClients.createDefault();
	}

	private ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
		public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
			int statusCode = response.getStatusLine().getStatusCode();
			/* HttpClient 支持get请求的自动重定向 */
			if (statusCode == 301 || statusCode == 302) {
				String localtionUrl = response.getFirstHeader("Location").getValue();
				if (localtionUrl == null || !localtionUrl.startsWith("http")) {

				}
				return go(localtionUrl, false);
			}
			if (statusCode >= 400) {

			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String charset = entity.getContentEncoding() == null ? DEFAULT_CHARSET_UTF8
						: entity.getContentEncoding().getValue();
				return new String(EntityUtils.toByteArray(entity), charset);
			} else {
				return null;
			}
		}
	};

	/**
	 * 对指定参数发出请求 HttpClientUtils.go()<BR>
	 * <P>
	 * Author : yubin
	 * </P>
	 * <P>
	 * Date : 2015-3-12
	 * </P>
	 * 
	 * @param url
	 *            url
	 * @param isAsync
	 *            isAsync
	 * @return 返回页面源码
	 */
	public String go(String url, boolean isAsync) {
		return go(url, false, null, null, DEFAULT_CHARSET_UTF8, isAsync, DETAULT_TRY_COUNT);
	}

	/**
	 * 对指定参数发出请求 HttpClientUtils.go()<BR>
	 * <P>
	 * Author : yubin
	 * </P>
	 * <P>
	 * Date : 2015-3-12
	 * </P>
	 * 
	 * @param url
	 *            url
	 * @param isAsync
	 *            isAsync
	 * @return 返回页面源码
	 */
	public String go(String url, boolean isPost, Map<String, String> params, boolean isAsync) {
		return go(url, isPost, params, null, DEFAULT_CHARSET_UTF8, isAsync, DETAULT_TRY_COUNT);
	}

	/**
	 * 对指定参数发出请求 HttpClientUtils.go()<BR>
	 * <P>
	 * Author : yubin
	 * </P>
	 * <P>
	 * Date : 2015-3-12
	 * </P>
	 * 
	 * @param url
	 *            url
	 * @param isPost
	 *            isPost
	 * @param params
	 *            params
	 * @return str or absFile
	 */
	public String go(String url, boolean isPost, Map<String, String> params) {
		return go(url, isPost, params, null, DEFAULT_CHARSET_UTF8, false, DETAULT_TRY_COUNT);
	}

	/**
	 * 对指定参数发出请求 HttpClientUtils.go()<BR>
	 * <P>
	 * Author : yubin
	 * </P>
	 * <P>
	 * Date : 2015-3-11
	 * </P>
	 * 
	 * @param url
	 *            url
	 * @param isPost
	 *            isPost
	 * @param params
	 *            params
	 * @param headerMap
	 *            headerMap
	 * @return str or absFile
	 */
	public String go(String url, boolean isPost, Map<String, String> params, Map<String, String> headerMap) {
		return go(url, isPost, params, headerMap, DEFAULT_CHARSET_UTF8, false, DETAULT_TRY_COUNT);
	}

	/**
	 * 对指定参数发出请求 HttpClientUtils.go()<BR>
	 * <P>
	 * Author : yubin
	 * </P>
	 * <P>
	 * Date : 2015-3-11
	 * </P>
	 * 
	 * @param url
	 *            url
	 * @param isPost
	 *            isPost
	 * @param params
	 *            params
	 * @param headerMap
	 *            headerMap
	 * @param charset
	 *            charset
	 * @param isAsync
	 *            isAsync
	 * @param tryCount
	 *            tryCount
	 * @param absFile
	 *            下载的文件
	 * @return str or absFile
	 */
	public String go(String url, final boolean isPost, final Map<String, String> params,
			final Map<String, String> headerMap, final String charset, boolean isAsync, final int tryCount) {
		if (url == null || url.trim().length() == 0) {

		}
		if (!url.matches("^http[s]?://.*?")) {
			url = DEFAULT_SCHEMA + url;
		}
		String responseStr;
		if (isAsync) {
			final ExecutorService excutors = Executors.newSingleThreadExecutor();
			final String httpUrl = url;
			excutors.execute(new Runnable() {
				
				public void run() {
					executor(httpUrl, isPost, params, headerMap, charset, tryCount);
					excutors.shutdown();
				}
			});
			return null;
		} else {
			responseStr = executor(url, isPost, params, headerMap, charset, tryCount);
		}

		return responseStr;
	}

	/**
	 * http请求 HttpClientUtils.executor()<BR>
	 * <P>
	 * Author : yubin
	 * </P>
	 * <P>
	 * Date : 2015-3-11
	 * </P>
	 * 
	 * @param url
	 *            url
	 * @param isPost
	 *            ispost
	 * @param params
	 *            params
	 * @param headerMap
	 *            headerMap
	 * @param charset
	 *            charset
	 * @param tryCount
	 *            tryCount
	 * @return str or filePath
	 */
	private String executor(String url, boolean isPost, Map<String, String> params, Map<String, String> headerMap,
			String charset, final int tryCount) {
		List<NameValuePair> nameValuePairs = getNameValuePairs(params);
		Header[] headers = getHeaders(headerMap);
		String responseStr;
		if (isPost) {
			responseStr = post(url, nameValuePairs, charset, headers);
		} else {
			responseStr = get(url, nameValuePairs, charset, headers);
		}
		return responseStr;
	}

	/**
	 * 发送post请求 HttpClientUtils.post()<BR>
	 * <P>
	 * Author : yubin
	 * </P>
	 * <P>
	 * Date : 2015-3-11
	 * </P>
	 * 
	 * @param url
	 *            url
	 * @param nameValuePairs
	 *            nameValuePairs
	 * @param httpClient
	 *            httpClient
	 * @param charset
	 *            charset
	 * @param headers
	 *            headers
	 * @return 文本或路径
	 */
	private String post(String url, List<NameValuePair> nameValuePairs, String charset, Header[] headers) {
		HttpEntity httpEntity = null;
		if (nameValuePairs != null && nameValuePairs.size() == 1
				&& nameValuePairs.get(0).getName().trim().length() == 0) {
			try {
				if (charset == null || charset.trim().length() == 0) {
					httpEntity = new StringEntity(nameValuePairs.get(0).getValue());
				} else {
					httpEntity = new StringEntity(nameValuePairs.get(0).getValue(), charset);
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("不支持的编码集", e);
			}
		} else if (nameValuePairs != null && nameValuePairs.size() >= 1) {
			try {
				if (charset == null || charset.trim().length() == 0) {
					httpEntity = new UrlEncodedFormEntity(nameValuePairs);
				} else {
					httpEntity = new UrlEncodedFormEntity(nameValuePairs, charset);
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("不支持的编码集", e);
			}
		}
		HttpPost hp = new HttpPost(url);
		hp.setConfig(requestConfig);
		hp.setHeaders(headers);
		hp.setEntity(httpEntity);
		String responseStr = null;
		try {
			responseStr = getHttpClient(url).execute(hp, responseHandler);
		} catch (ClientProtocolException e) {
			throw new RuntimeException("访问" + url + "客户端连接协议错误", e);
		} catch (IOException e) {
			throw new RuntimeException("访问" + url + " IO操作异常", e);
		} catch (Exception e) {
			throw new RuntimeException(url, e);
		} finally {
			hp.abort();
		}
		return responseStr;
	}

	/**
	 * 发送get请求 HttpClientUtils.get()<BR>
	 * <P>
	 * Author : yubin
	 * </P>
	 * <P>
	 * Date : 2015-3-11
	 * </P>
	 * 
	 * @param url
	 *            url
	 * @param params
	 *            params
	 * @param httpClient
	 *            httpClient
	 * @param charset
	 *            charset
	 * @param headers
	 *            header
	 * @return str or absFile
	 */
	private String get(String url, List<NameValuePair> params, String charset, Header[] headers) {
		if (params == null) {
			params = new ArrayList<NameValuePair>();
		}
		params.addAll(getNameValuePairs(getURI(url)));
		charset = (charset == null ? DEFAULT_CHARSET_UTF8 : charset);
		String formatParams = URLEncodedUtils.format(params, charset);
		url = (url.indexOf("?")) < 0 ? (url + "?" + formatParams)
				: (url.subSequence(0, url.indexOf("?") + 1) + formatParams);
		HttpGet hg = new HttpGet(url);
		hg.setConfig(requestConfig);
		hg.setHeaders(headers);
		String responseStr = null;
		try {
			responseStr = getHttpClient(url).execute(hg, responseHandler);
		} catch (ClientProtocolException e) {
			throw new RuntimeException("访问" + url + "客户端连接协议错误", e);
		} catch (IOException e) {
			throw new RuntimeException("访问" + url + " IO操作异常", e);
		} catch (Exception e) {
			throw new RuntimeException(url, e);
		} finally {
			hg.abort();
		}
		return responseStr;
	}

	/**
	 * 获取uri键值对 HttpClientUtils.getURI()<BR>
	 * <P>
	 * Author : yubin
	 * </P>
	 * <P>
	 * Date : 2015-6-22
	 * </P>
	 * 
	 * @param url
	 *            url
	 * @return map
	 */
	public Map<String, String> getURI(String url) {
		int index = url.indexOf("?");
		if (index < 0 || index + 1 == url.length()) {
			return Collections.emptyMap();
		}
		Map<String, String> params = new HashMap<String, String>();
		String uri = url.substring(index + 1);
		String[] queries = uri.split("[&]+", -1);
		for (String query : queries) {
			String[] queryArr = query.split("=", -1);
			if (queryArr.length == 1) {
				params.put("", queryArr[0]);
				continue;
			}
			params.put(queryArr[0], queryArr[1]);
		}
		return params;
	}

	/**
	 * 获取请求参数 HttpClientUtils.getNameValuePairs()<BR>
	 * <P>
	 * Author : yubin
	 * </P>
	 * <P>
	 * Date : 2015-3-11
	 * </P>
	 * 
	 * @param params
	 *            params
	 * @return list
	 */
	private List<NameValuePair> getNameValuePairs(Map<String, String> params) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		if (params == null || params.size() == 0) {
			return nameValuePairs;
		}
		for (Map.Entry<String, String> map : params.entrySet()) {
			nameValuePairs.add(new BasicNameValuePair(map.getKey(), map.getValue()));
		}
		return nameValuePairs;
	}

	/**
	 * 获取头部信息 HttpClientUtils.getHeaders()<BR>
	 * <P>
	 * Author : yubin
	 * </P>
	 * <P>
	 * Date : 2015-3-11
	 * </P>
	 * 
	 * @param headerMap
	 *            headerMap
	 * @return header[]
	 */
	private Header[] getHeaders(Map<String, String> headerMap) {
		if (headerMap == null || headerMap.size() == 0) {
			return null;
		}
		Header[] headers = new BasicHeader[headerMap.size()];
		Header header;
		int i = 0;
		for (Map.Entry<String, String> map : headerMap.entrySet()) {
			header = new BasicHeader(map.getKey(), map.getValue());
			headers[i] = header;
			i++;
		}
		return headers;
	}
}
