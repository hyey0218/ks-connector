package com.konantech.forensic.dao;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//엔진 KQL 데이터 접근용 - 공통
public class KqlDao {
	private static final Logger log = LoggerFactory.getLogger(KqlDao.class);

	protected static String charsetType = "UTF-8";
	protected static String ksfCharsetType = "UTF-8";
	protected static boolean useWarning = false;
	private static int defaultTimeOut = 100000;
	private static String ksearch_api_kcode = "/kql";

	//작업 조회 용도
	public static String kqlResult(String ksearchEngineAddr, String kqlQuery) throws Exception{
		// 검색쿼리 
		//StringBuffer query = new StringBuffer();
		// rest URL 제조용
		String result = getEngineApi_hc_post(ksearchEngineAddr, ksearch_api_kcode, kqlQuery, defaultTimeOut);
		log.debug("::: 작업 요청 request success - "+ kqlQuery +"  :::");
		log.debug("::: 작업 요청결과 return success - "+ result +"  :::");
		
		return result;
	}
	
	//작업 조회 용도 - timeout 없음
	public static String kqlResultNoTimeout(String ksearchEngineAddr, String kqlQuery) throws Exception{
		// 검색쿼리 
		//StringBuffer query = new StringBuffer();
		// rest URL 제조용
		String result = getEngineApi_hc_post(ksearchEngineAddr, ksearch_api_kcode, kqlQuery, 0);
		log.debug("::: 작업 요청 request success - "+ kqlQuery +"  :::");
		log.debug("::: 작업 요청결과 return success - "+ result +"  :::");
		
		return result;
	}
	
	/**
	 * REST 방식으로 엔진 호출 POST방식 (파라미터 전달 방식)(HTTPCLIENT 호출방식)
	 * @version 1.2
	 * @modify 2019.03.14
	 * @modifier 이인혁(inhyeok.lee)
	 * @return String
	 * @throws Exception 
	 */
	public static String getEngineApi_hc_post(String engineUrl, String apiString, String postData, int timeout) throws Exception {
		try {			
			
			StringBuffer engineRestUrl = new StringBuffer();
			engineRestUrl.append(engineUrl);
			engineRestUrl.append(apiString);
			//engineRestUrl.append(param);
			
			log.debug("getEngineApi url : " + engineRestUrl.toString());
			
			String result = request(engineRestUrl.toString(),postData,"POST",timeout);
			if(result==null) {
				return "page error!";
			}
			
			String returnStr="";
			if(!useWarning) {
				returnStr = result.replaceAll("\\(WARNING: EVALUATION COPY\\[SEARCH\\]\\)", "");
			} else {
				returnStr = result;
			}
			return returnStr;
			// 파싱 끝			
		} catch (Exception e) {
			log.error("ERROR: "+log.getName() +" getEngineApi Exception");
			throw e;
		}	
	}	
	
	public  String request(String targetURL, int timeout) throws Exception {
		return request(targetURL, null, "GET", timeout);
	}

	public static String request(String targetURL, String payload, String httpMethod, int timeout) throws Exception {
		return request(targetURL, payload, httpMethod, charsetType, timeout);
	}

	public static String request(String targetURL, String payload, String httpMethod, final String charset, int timeout) throws Exception {

		CloseableHttpClient httpclient = HttpClients.createDefault();

		String responseBody = "";

		try {
			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				public String handleResponse(final HttpResponse response) throws IOException {
					int status = response.getStatusLine().getStatusCode();
					HttpEntity entity = response.getEntity();
					if ((status >= 200 && status < 300) || status==400) {
						return entity != null ? EntityUtils.toString(entity, charset) : null;
					} else {
							throw new IOException();
					}
				}
			};

			if ("POST".equalsIgnoreCase(httpMethod)) { //create
				HttpPost http = new HttpPost(targetURL);
				//time out config
				RequestConfig requestConfig = RequestConfig.custom()
						  .setSocketTimeout(timeout)
						  .setConnectTimeout(timeout)
						  .setConnectionRequestTimeout(timeout)
						  .build();
				http.setConfig(requestConfig);
				http.setEntity(new StringEntity(payload, charset));
				responseBody = httpclient.execute(http, responseHandler);
			} 
			else if("PUT".equalsIgnoreCase(httpMethod)) { //update
				HttpPut http = new HttpPut(targetURL);
				//time out config
				RequestConfig requestConfig = RequestConfig.custom()
						  .setSocketTimeout(timeout)
						  .setConnectTimeout(timeout)
						  .setConnectionRequestTimeout(timeout)
						  .build();
				http.setConfig(requestConfig);
				http.setHeader("Accept", "application/json");
				http.setHeader("Content-type", "text/plain; charset="+charset);
				if(payload != null)
					http.setEntity(new StringEntity(payload, charset));	
				responseBody = httpclient.execute(http, responseHandler);
			}
			else if("DELETE".equalsIgnoreCase(httpMethod)) { //delete
				HttpDelete http = new HttpDelete(targetURL);
				//time out config
				RequestConfig requestConfig = RequestConfig.custom()
						  .setSocketTimeout(timeout)
						  .setConnectTimeout(timeout)
						  .setConnectionRequestTimeout(timeout)
						  .build();
				http.setConfig(requestConfig);
				responseBody = httpclient.execute(http, responseHandler);
			}
			else { //get
				HttpGet http = new HttpGet(targetURL);
				//time out config
				RequestConfig requestConfig = RequestConfig.custom()
						  .setSocketTimeout(timeout)
						  .setConnectTimeout(timeout)
						  .setConnectionRequestTimeout(timeout)
						  .build();
				http.setConfig(requestConfig);
				responseBody = httpclient.execute(http, responseHandler);
			}
			//logger.debug("responseBody : " + responseBody );
			return responseBody;
		} catch (Exception e) {
			log.error("responseBody : "+responseBody);
			log.error("responseBody fail : "+log.getName() +" \n"+ e.getMessage());
			throw e;
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
			}
		}
	}
}
