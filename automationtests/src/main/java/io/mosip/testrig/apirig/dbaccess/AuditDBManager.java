package io.mosip.testrig.apirig.dbaccess;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.jdbc.Work;
import org.testng.Assert;

import io.mosip.testrig.apirig.admin.fw.util.AdminTestUtil;
import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.apirig.testrunner.MosipTestRunner;

public class AuditDBManager extends AdminTestUtil {
	private static final Logger DBCONNECTION_LOGGER = Logger.getLogger(AuditDBManager.class);
	private static Map<String, Object> records;
	private static List<Map<String, Object>> allRecords;
	public static String env = System.getProperty("env.user");
	public static Map<String, SessionFactory> sessionFactoryMapS = Collections
			.synchronizedMap(new HashMap<String, SessionFactory>());

	/**
	 * Execute query to get generated otp value
	 * 
	 * @param query
	 * @param moduleName
	 * @return otp record
	 */

	public static Map<String, Object> executeQueryAndGetRecord(String moduleName, String query) {
		Session session = null;
		Map<String, Object> record = new HashMap<>();
		try {
			session = getDataBaseConnection(moduleName);
			if (session != null) {
				session.doWork(new Work() {
					@Override
					public void execute(Connection connection) throws SQLException {
						Statement statement = connection.createStatement();
						try {
							ResultSet rs = statement.executeQuery(query);
							ResultSetMetaData md = rs.getMetaData();
							int columns = md.getColumnCount();
							while (rs.next()) {
								for (int i = 1; i <= columns; i++) {
									record.put(md.getColumnName(i), rs.getObject(i));
								}
							}
						} finally {
							statement.close();
						}
					}
				});
			}
		} catch (NullPointerException e) {
			DBCONNECTION_LOGGER.error("Exception occured " + e.getMessage());
		} finally {
			closeDataBaseConnection(session);
		}
		return record;

	}

	public static void executeQueryAndDeleteRecord(String moduleName, String deleteQuery) {
		Session session = null;
		try {
			session = getDataBaseConnection(moduleName);
			if (session != null) {
				session.doWork(new Work() {
					@Override
					public void execute(Connection connection) throws SQLException {
						Statement statement = connection.createStatement();
						try {
							int rs = statement.executeUpdate(deleteQuery);
							if (rs > 0) {
								DBCONNECTION_LOGGER.info("deleted successfully!");
							}
						} finally {
							statement.close();
						}
					}
				});
			}
		} catch (NullPointerException e) {
			DBCONNECTION_LOGGER.error("Exception occured " + e.getMessage());
		} finally {
			closeDataBaseConnection(session);
		}
	}

	private static Session getDataBaseConnection(String dbName) {
		Session session = null;
		SessionFactory sessionFactory = sessionFactoryMapS.get(dbName);
		if (sessionFactory == null) {
		try {
				sessionFactory = getDataBaseConnectionSessionFactory(dbName);
				sessionFactoryMapS.put(dbName, sessionFactory);
			} catch (HibernateException e) {
				DBCONNECTION_LOGGER.error("Exception in Database Connection with following message: " + e.getMessage());
			} catch (NullPointerException e) {
				Assert.assertTrue(false, "Exception in getting the SessionFactory for DB Schema : " + dbName );
			}
		}
		if (sessionFactory != null) {
			session = sessionFactory.getCurrentSession();
			session.beginTransaction();
			DBCONNECTION_LOGGER.info("Session begined with Schema : " + dbName);
		}
		return session;	
	}
	
	private static SessionFactory getDataBaseConnectionSessionFactory(String dbName) {
		SessionFactory factory = null;
		String dbschema = ConfigManager.getValueForKey("audit_db_schema");

		if(dbName.equalsIgnoreCase("partner"))
			dbschema=ConfigManager.getValueForKey("ida_db_schema");
		
		if(dbName.equalsIgnoreCase("master"))
			dbschema=ConfigManager.getValueForKey("master_db_schema");

		try {
			Configuration config = new Configuration();
			config.setProperty("hibernate.connection.driver_class", propsKernel.getProperty("driver_class"));
			config.setProperty("hibernate.connection.url",
					"jdbc:" + propsKernel.getProperty("postgresqlUser") + "://"
							+ ConfigManager.getValueForKey("db-server") + ":" + ConfigManager.getValueForKey("db-port")
							+ "/mosip_" + dbschema);
			config.setProperty("hibernate.connection.username", ConfigManager.getAuditDbUser());
			config.setProperty("hibernate.connection.password", ConfigManager.getValueForKey(ConfigManager.DB_PASSWORD_KEY));
			config.setProperty("hibernate.default_schema", propsKernel.getProperty(dbName + "_default_schema"));
			config.setProperty("hibernate.connection.pool_size", propsKernel.getProperty("pool_size"));
			config.setProperty("hibernate.dialect", propsKernel.getProperty("dialect"));
			config.setProperty("hibernate.show_sql", propsKernel.getProperty("show_sql"));
			config.setProperty("hibernate.current_session_context_class",
					propsKernel.getProperty("current_session_context_class"));
			config.addFile(new File(MosipTestRunner.getGlobalResourcePath() + "/dbFiles/dbConfig.xml"));
			factory = config.buildSessionFactory();		
		} catch (HibernateException e) {
			DBCONNECTION_LOGGER.error("Exception in Database Connection with following message: " + e.getMessage());
		} catch (NullPointerException e) {
			Assert.assertTrue(false, "Exception in getting the SessionFactory for DB Schema : " + dbschema );
		}
		return factory;
	}


	public static void closeDataBaseConnection(Session session) {
		if (session != null) {
			DBCONNECTION_LOGGER.info("Session closed");
			session.close();
		}
	}
	
}
