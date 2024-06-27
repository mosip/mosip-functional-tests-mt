package io.mosip.testrig.apirig.authentication.testdata.mapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import io.mosip.testrig.apirig.admin.fw.util.AdminTestUtil;
import io.mosip.testrig.apirig.authentication.fw.precon.XmlPrecondtion;

/**
 * The class to generate list of xpath for xml
 * 
 * @author Vignesh
 *
 */
public class XmlXpathGeneration extends DefaultHandler {
	private static final Logger xmlXpathLogger = Logger.getLogger(XmlXpathGeneration.class);
	private static Map<String, String> mappingFieldvalue = new HashMap<>();
	private static Set<String> xpathList = new HashSet<>();
	String xPath = "/";
	XMLReader xmlReader;
	XmlXpathGeneration parent;
	StringBuilder characters = new StringBuilder();
	Map<String, Integer> elementNameCount = new HashMap<>();
	private static final String SAX_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
	private static final String SAX_EXTERNAL_GENERAL_FEATURE = "http://xml.org/sax/features/external-general-entities";
	private static final String SAX_EXTERNAL_PARAMETER_FEATURE = "http://xml.org/sax/features/external-parameter-entities";

	public XmlXpathGeneration(XMLReader xmlReader) {
		this.xmlReader = xmlReader;
	}

	private XmlXpathGeneration(String xPath, XMLReader xmlReader, XmlXpathGeneration parent) {
		this(xmlReader);
		this.xPath = xPath;
		this.parent = parent;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		Integer count = elementNameCount.get(qName);
		if (null == count)
			count = 1;
		else
			count++;
		elementNameCount.put(qName, count);
		String childXPath = xPath + "/" + qName + "[" + count + "]";
		int attsLength = attributes.getLength();
		for (int x = 0; x < attsLength; x++)
			xpathList.add(childXPath + "/@" + attributes.getQName(x));
		XmlXpathGeneration child = new XmlXpathGeneration(childXPath, xmlReader, this);
		xmlReader.setContentHandler(child);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String value = characters.toString().trim();
		if (value.length() > 0) {
			xpathList.add(xPath + "/text()");
		}
		xmlReader.setContentHandler(parent);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		characters.append(ch, start, length);
	}

	public static void generateXpath(String filePath, String ouputFilePath) {
		FileInputStream inputStream = null;
		try {
			SAXParserFactory saxFactory = SAXParserFactory.newInstance();
			  // to be compliant, completely disable DOCTYPE declaration:
			saxFactory.setFeature(SAX_FEATURE, true);
			  // or completely disable external entities declarations:
			saxFactory.setFeature(SAX_EXTERNAL_GENERAL_FEATURE, false);
			saxFactory.setFeature(SAX_EXTERNAL_PARAMETER_FEATURE, false);
			saxFactory.setNamespaceAware(false);
			SAXParser saxParser = saxFactory.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(new XmlXpathGeneration(xmlReader));
			inputStream = new FileInputStream(filePath);
			xmlReader.parse(new InputSource(inputStream));
			refactorFieldValueName();
			generateXmlMappingDic(ouputFilePath);
		} catch (Exception e) {
			xmlXpathLogger.info("Exception occured: " + e.getMessage());
		}finally {
			AdminTestUtil.closeInputStream(inputStream);
		}
	}
	
	public static Map<String,String> generateXpath(String filePath) {
		FileInputStream inputStream = null;
		try {
			SAXParserFactory saxFactory = SAXParserFactory.newInstance();
			  // to be compliant, completely disable DOCTYPE declaration:
			saxFactory.setFeature(SAX_FEATURE, true);
			  // or completely disable external entities declarations:
			saxFactory.setFeature(SAX_EXTERNAL_GENERAL_FEATURE, false);
			saxFactory.setFeature(SAX_EXTERNAL_PARAMETER_FEATURE, false);
			saxFactory.setNamespaceAware(false);
			SAXParser saxParser = saxFactory.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(new XmlXpathGeneration(xmlReader));
			inputStream = new FileInputStream(filePath);
			xmlReader.parse(new InputSource(inputStream));
			refactorFieldValueName();
			return mappingFieldvalue;
		} catch (Exception e) {
			xmlXpathLogger.info("Exception occured: " + e.getMessage());
			return mappingFieldvalue;
		}finally {
			AdminTestUtil.closeInputStream(inputStream);
		}
	}

	/**
	 * Method generate xml mapping in property file
	 * 
	 * @param filePath
	 */
	public static void generateXmlMappingDic(String filePath) {
		Properties prop = new Properties();
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(filePath);
			for (Entry<String, String> entry : mappingFieldvalue.entrySet()) {
				prop.setProperty(entry.getKey(), entry.getValue());
			}
			prop.store(outputStream, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			xmlXpathLogger.error(e.getMessage());
		}finally {
			AdminTestUtil.closeOutputStream(outputStream);
		}
	}

	/**
	 * Method to modify xpath value name
	 */
	private static void refactorFieldValueName() {
		xmlXpathLogger.info(xpathList);
		for (String entry : xpathList) {
			String tempValue = entry.replace("[", "").replace("]", "").replace("/text()", "").replace("@", "");
			String[] listValue = tempValue.split(Pattern.quote("/"));
			String fieldKey = "";
			for(int i=1;i<listValue.length;i++) {
				if(i!=4)
					fieldKey = fieldKey + listValue[listValue.length-i];
				else if(mappingFieldvalue.containsKey(fieldKey))
					fieldKey = fieldKey + listValue[listValue.length-i];
				else
					break;
			}
			fieldKey=fieldKey.replace("\"", "");
			fieldKey=fieldKey.replace(":", "");
			mappingFieldvalue.put(fieldKey, entry);
		}
	}
}