package io.mosip.testrig.apirig.authentication.fw.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.admin.fw.util.AdminTestUtil;
import io.mosip.testrig.apirig.authentication.fw.precon.XmlPrecondtion;

/**
 * Class is to create auth partner or demo app processor in new thread
 * 
 * @author Vignesh
 *
 */
public class AuthPartnerProcessor extends AdminTestUtil{

	private static final Logger DEMOAPP_LOGGER = Logger.getLogger(AuthPartnerProcessor.class);
	public static Process authPartherProcessor;

	/**
	 * Start demo app or auth partner applciation in seperate thread using runtime processor
	 * 
	 */
	public static void startProcess() {
		
		String encryptUtilPort = properties.getProperty("encryptUtilPort");
		String AuthClientID = propsKernel.getProperty("AuthClientID");
		String AuthClientSecret = propsKernel.getProperty("AuthClientSecret");
		String AuthAppID = propsKernel.getProperty("AuthAppID");
		try {
			authPartherProcessor = Runtime.getRuntime()
					.exec(new String[] { getJavaPath(), "-Dmosip.base.url="+ApplnURI,
							"-Dserver.port="+encryptUtilPort, "-Dauth-token-generator.rest.clientId="+AuthClientID, 
							"-Dauth-token-generator.rest.secretKey="+AuthClientSecret, 
							"-Dauth-token-generator.rest.appId="+AuthAppID, "-jar", getDemoAppJarPath() });
			Runnable startDemoAppTask = () -> {
				try (InputStream inputStream = authPartherProcessor.getInputStream();
						BufferedReader bis = new BufferedReader(new InputStreamReader(inputStream));) {
					String line;
					while ((line = bis.readLine()) != null)
						DEMOAPP_LOGGER.info(line);
				} catch (IOException e) {
					DEMOAPP_LOGGER.error(e.getMessage());
				}
			};
			new Thread(startDemoAppTask).start();
			Thread.sleep(120000);
		} catch (Exception e) {
			DEMOAPP_LOGGER.error("Exception occured in starting the demo auth partner processor");
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Method to get demo app jar path accodring to OS
	 * 
	 * @return filepath
	 */
	private static String getDemoAppJarPath() {
		String demoAppVersion = properties.getProperty("demoAppVersion");
		if (getOSType().equals("WINDOWS")) {
			return "C:/Users/" + System.getProperty("user.name")
					+ "/.m2/repository/io/mosip/authentication/authentication-demo-service/"
					+ demoAppVersion+ "/authentication-demo-service-" + demoAppVersion + ".jar";
		} else {
			DEMOAPP_LOGGER.info("Maven Path: " + RunConfigUtil.getLinuxMavenPath());
			String mavenPath = RunConfigUtil.getLinuxMavenPath();
			String settingXmlPath = mavenPath + "/conf/settings.xml";
			String repoPath = XmlPrecondtion.getValueFromXmlFile(settingXmlPath, "//localRepository");
			return repoPath + "/io/mosip/authentication/authentication-demo-service/" + demoAppVersion
					+ "/authentication-demo-service-" + demoAppVersion + ".jar";
		}
	}
	
	/**
	 * Method to get current device java path from environment detail according to OS
	 * 
	 * @return string
	 */
	private static String getJavaPath() {
		String path = "java";
		if (getOSType().equals("WINDOWS")) {
			String javaHome = System.getenv("JAVA_HOME");
			if (javaHome != null && javaHome.isEmpty()== false)
				path = javaHome + "/bin/java";
		}
		return path;
	}

}
