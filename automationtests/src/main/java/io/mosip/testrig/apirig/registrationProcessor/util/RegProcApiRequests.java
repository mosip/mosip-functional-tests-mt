package io.mosip.testrig.apirig.registrationProcessor.util;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import io.mosip.testrig.apirig.global.utils.GlobalConstants;
import io.mosip.testrig.apirig.service.BaseTestCase;
import io.mosip.testrig.apirig.testrunner.MosipTestRunner;
import io.restassured.http.Cookie;
import io.restassured.response.Response;

public class RegProcApiRequests extends BaseTestCase {
	private static Logger logger = Logger.getLogger(RegProcApiRequests.class);

	public Response regProcSyncRequest(String url, Object body, String center_machine_refId, String ldt,
			String contentHeader, String regProcAuthToken) {
		Cookie.Builder builder = new Cookie.Builder(GlobalConstants.AUTHORIZATION, regProcAuthToken);
		Response postResponse = given().cookie(builder.build()).header("Center-Machine-RefId", center_machine_refId)
				.header("timestamp", ldt).relaxedHTTPSValidation().body("\"" + body + "\"").contentType(contentHeader)
				.log().all().when().post(ApplnURI + url).then().log().all().extract().response();
		return postResponse;
	}

	/**
	 * 
	 * @param file
	 * @param url
	 * @param regProcAuthToken
	 * @return
	 */
	public Response regProcPacketUpload(File file, String url, String regProcAuthToken) {

		logger.info("REST:ASSURED :Sending a data packet to" + ApplnURI + url);
		Response getResponse = null;
		Response newResponse = null;
		try {
			Cookie.Builder builder = new Cookie.Builder(GlobalConstants.AUTHORIZATION, regProcAuthToken);

			newResponse = given().cookie(builder.build()).baseUri(ApplnURI).basePath(url)
					.multiPart("file", file, "application/octet-stream").post().then().log().all().extract().response();
			logger.info("REST:ASSURED: The response from request is:" + newResponse.asString());
			logger.info("REST-ASSURED: the response time is: " + newResponse.time());

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return newResponse;
	}

	public Response regProcGetRequest(String url, Map<String, String> valueMap, String regProcAuthToken) {
		logger.info("REST-ASSURED: Sending a GET request to " + ApplnURI + url);

		Cookie.Builder builder = new Cookie.Builder(GlobalConstants.AUTHORIZATION, regProcAuthToken);
		Response getResponse = null;
		try {
			getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().queryParams(valueMap).log().all()
					.when().post(ApplnURI + url).then().log().all().extract().response();
			
			logger.info(GlobalConstants.REST_ASSURED_STRING_2 + getResponse.asString());
			logger.info(GlobalConstants.REST_ASSURED_STRING_3 + getResponse.time());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return getResponse;
	}

	public Response regProcGetIdRepo(String url, String regProcAuthToken) {
		logger.info("REST-ASSURED: Sending a GET request to " + ApplnURI + url);

		Cookie.Builder builder = new Cookie.Builder(GlobalConstants.AUTHORIZATION, regProcAuthToken);
		Response getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().log().all().when()
				.get(ApplnURI + url).then().log().all().extract().response();
		logger.info(GlobalConstants.REST_ASSURED_STRING_2 + getResponse.asString());
		logger.info(GlobalConstants.REST_ASSURED_STRING_3 + getResponse.time());
		return getResponse;
	}

	public Response postRequestToEncryptDecrypt(String url, Object body, String contentHeader, String acceptHeader,
			String token) {
		logger.info("REST:ASSURED:Sending a data packet to" + ApplnURI + url);
		logger.info("REST ASSURRED :: Request To Encrypt Is " + body);
		Cookie.Builder builder = new Cookie.Builder(GlobalConstants.AUTHORIZATION, token);
		Response postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().body(body)
				.contentType(contentHeader).accept(acceptHeader).log().all().when().post(ApplnURI + url).then().log()
				.all().extract().response();

		return postResponse;
	}

	public Response regProcPostRequest(String url, Map<String, String> valueMap, String contentHeader,
			String token) {
		logger.info("REST:ASSURED:Sending a post request to" + url);
		Cookie.Builder builder = new Cookie.Builder(GlobalConstants.AUTHORIZATION, token);

		Response postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().body(valueMap)
				.contentType(contentHeader).log().all().when().post(ApplnURI + url).then().log().all().extract()
				.response();
		logger.info(GlobalConstants.REST_ASSURED_STRING_2 + postResponse.asString());
		logger.info(GlobalConstants.REST_ASSURED_STRING_3 + postResponse.time());
		return postResponse;
	}

	public Response postRequest(String url, Object body, String contentHeader, String acceptHeader) {

		logger.info("URL IS  :: " + ApplnURI + url);

		Response postResponse = given().relaxedHTTPSValidation().body(body).contentType(contentHeader)
				.accept(acceptHeader).log().all().when().post(ApplnURI + url).then().log().all().extract().response();
		logger.info(GlobalConstants.REST_ASSURED_STRING_2 + postResponse.asString());
		logger.info(GlobalConstants.REST_ASSURED_STRING_3 + postResponse.time());
		logger.info("REST-ASSURED:454545445 The response Time is: " + postResponse.asString());
		return postResponse;
	}

	public boolean validateToken(String token) {
		String url = "/v1/authmanager/authorize/admin/validateToken";
		Cookie.Builder builder = new Cookie.Builder(GlobalConstants.AUTHORIZATION, token);
		Response response = given().cookie(builder.build()).relaxedHTTPSValidation().log().all().when()
				.get(ApplnURI + url).then().log().all().extract().response();
		logger.info(response.asString());
		List<String> errors = response.jsonPath().get("errors");
		if (errors == null) {
			return true;
		} else
			return false;
	}

	public Response regProcPacketGenerator(Object body, String url, String contentHeader, String token) {
		logger.info("REST:ASSURED:Sending a post request to" + url);
		Cookie.Builder builder = new Cookie.Builder(GlobalConstants.AUTHORIZATION, token);

		Response postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().body(body)
				.contentType(contentHeader).log().all().when().post(ApplnURI + url).then().log().all().extract()
				.response();
		logger.info(GlobalConstants.REST_ASSURED_STRING_2 + postResponse.asString());
		logger.info(GlobalConstants.REST_ASSURED_STRING_3 + postResponse.time());
		return postResponse;
	}

	public boolean getUinStatusFromIDRepo(JSONObject actualRequest, String idRepoToken, String expectedUinResponse) {
		boolean status = false;
		JSONObject generatorRequest = (JSONObject) actualRequest.get(GlobalConstants.REQUEST);
		String uin = generatorRequest.get("uin").toString();
		String idRepoUrl = "/idrepository/v1/identity/uin/";
		Cookie.Builder builder = new Cookie.Builder(GlobalConstants.AUTHORIZATION, idRepoToken);

		Response idRepoResponse = given().cookie(builder.build()).relaxedHTTPSValidation().when()
				.get(ApplnURI + idRepoUrl + uin).then().extract().response();
		String uinResponse = idRepoResponse.jsonPath().get("response.status").toString();
		logger.info(uinResponse);
		if (uinResponse.equals(expectedUinResponse)) {
			status = true;
		}
		return status;

	}

	/**
	 * The method to return class loader resource path
	 * 
	 * @return String
	 */
	public String getResourcePath() {
		return MosipTestRunner.getGlobalResourcePath() + "/";
	}

	/**
	 * @param url
	 * @param cookie
	 * @return this method is for get request with authentication(cookie) and
	 *         without any param.
	 */
	public Response getWithoutParams(String url, String cookie) {
		logger.info("REST-ASSURED: Sending a Get request to " + url);
		Cookie.Builder builder = new Cookie.Builder(GlobalConstants.AUTHORIZATION, cookie);
		Response getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().log().all().when().get(url);
		logger.info("REST-ASSURED: the response Time is: " + getResponse.time());
		logger.info("REST-ASSURED: the response from request is: " + getResponse.asString());
		return getResponse;
	}

	/**
	 * @param url
	 * @param patharams
	 * @param cookie
	 * @return this method is for get request with authentication(cookie) and with
	 *         pathParams Map(name, Value).
	 */
	public Response getWithPathParam(String url, Map<String, String> patharams, String cookie) {
		logger.info("REST-ASSURED: Sending a GET request to " + ApplnURI + url);

		Cookie.Builder builder = new Cookie.Builder(GlobalConstants.AUTHORIZATION, cookie);
		Response getResponse = given().cookie(builder.build()).relaxedHTTPSValidation().pathParams(patharams).log()
				.all().when().get(ApplnURI + url);
		logger.info("REST-ASSURED: the response from request is: " + getResponse.asString());
		logger.info(GlobalConstants.REST_ASSURED_STRING_3 + getResponse.time());
		return getResponse;
	}

	public boolean checkResponseTime(Response actualResponse) {
		boolean utcCheck = false;
		String responseTime = actualResponse.jsonPath().get("responsetime").toString();
		String cuurentUTC = (String) getUTCTime();
		SimpleDateFormat sdf = new SimpleDateFormat("mm");
		try {
			Date d1 = sdf.parse(responseTime.substring(14, 16));
			Date d2 = sdf.parse(cuurentUTC.substring(14, 16));

			long elapse = Math.abs(d1.getTime() - d2.getTime());
			if (elapse < 300000) {
				utcCheck = true;
			}

		} catch (java.text.ParseException e) {
			logger.error(e.getMessage());
		}
		return utcCheck;

	}

	public Object getUTCTime() {
		String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
		LocalDateTime time = LocalDateTime.now(Clock.systemUTC());
		String utcTime = time.format(dateFormat);
		return utcTime;

	}

	public Object getCurrentTime() {
		String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
		LocalDateTime time = LocalDateTime.now();
		String currentTime = time.format(dateFormat);
		return currentTime;

	}


	public void deleteFromAbis(JSONObject deleteRequest) {
		String url = "https://qa.mosip.io/T5CloudService/1.0/processRequest";
		Response getResponse = given().relaxedHTTPSValidation().body(deleteRequest)
				.contentType(MediaType.APPLICATION_JSON).log().all().when().post(url);
		logger.info(getResponse.asString());
	}

	public Response postRequestWithRequestResponseHeaders(String url, Object body, String contentHeader, String acceptHeader,
			String token) {
		try {
			Cookie.Builder builder = new Cookie.Builder(GlobalConstants.AUTHORIZATION, token);
			Response postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().body(body)
					.contentType(contentHeader).accept(acceptHeader).when().post(ApplnURI + url).then().extract()
					.response();
			return postResponse;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	public Response postRequestToSign(String url, Object body, String contentHeader, String acceptHeader,
			String token) {
		Response postResponse = null;
		try {
			Cookie.Builder builder = new Cookie.Builder(GlobalConstants.AUTHORIZATION, token);
			logger.info("Sending sign request to: " + ApplnURI + url);
			postResponse = given().cookie(builder.build()).relaxedHTTPSValidation().body(body)
					.contentType(contentHeader).accept(acceptHeader).log().all().when().post(ApplnURI + url).then()
					.log().all().extract().response();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return postResponse;
	}
	
	
}