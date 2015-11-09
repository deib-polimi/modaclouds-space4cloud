/**
 * Copyright 2014 deib-polimi
 * Contact: deib-polimi <marco.miglierina@polimi.it>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package it.polimi.modaclouds.space4cloud.utils;

import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.jxpath.JXPathContext;

public class XMLHelper {

	public static <T> boolean containsId(Collection<T> elements, String id) {
		return getElementByID(elements, id) != null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialize(URL xmlUrl, Class<T> targetClass)
			throws JAXBException {
		T object = (T) JAXBContext.newInstance(targetClass)
				.createUnmarshaller().unmarshal(xmlUrl);
		return object;
	}

	public static <T> T getElementByID(Collection<T> elements, String id) {
		for (T element : elements) {
			if (JXPathContext.newContext(element).getValue("id").equals(id))
				return element;
		}
		return null;
	}

	public static <T> void serialize(T object, Class<T> sourceClass,
			OutputStream resultStream) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(sourceClass);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(object, resultStream);
	}

	// /**
	// * Gets the XML block corresponding to <tt>id</tt> at the given
	// * <tt>pathToBlock</tt> (xpath format) from <tt>xmlText</tt>
	// *
	// * @param xmlText
	// * The XML string representation
	// * @param id
	// * The ID identifying the desired block
	// * @param pathToBlock
	// * the path to the XML block in xpath format
	// * @return the string representation of the desired XML block
	// */
	// public static String getXMLBlockById(String xmlText, String id,
	// String pathToBlock) {
	// String xmlBlock = null;
	// Document doc = convertXMLStringToXMLDocument(xmlText);
	// try {
	//
	// XPath xpath = XPathFactory.newInstance().newXPath();
	// String expression = pathToBlock + "[@id='" + id + "']";
	// Node node;
	// node = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
	// TransformerFactory transFactory = TransformerFactory.newInstance();
	// Transformer transformer = transFactory.newTransformer();
	// StringWriter buffer = new StringWriter();
	// transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
	// "yes");
	// transformer
	// .transform(new DOMSource(node), new StreamResult(buffer));
	// xmlBlock = buffer.toString();
	// } catch (XPathExpressionException | TransformerException e) {
	// e.printStackTrace();
	// }
	// return xmlBlock;
	// }
	//
	// /**
	// * Converts an XML string representation into a org.w3c.dom.Document
	// *
	// * @param monitoringRulesXMLString
	// * the XML string representation to be converted
	// * @return the XML as a org.w3c.dom.Document object
	// */
	// public static Document convertXMLStringToXMLDocument(
	// String monitoringRulesXMLString) {
	// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	// DocumentBuilder builder;
	// try {
	// builder = factory.newDocumentBuilder();
	// Document document = builder.parse(new InputSource(new StringReader(
	// monitoringRulesXMLString)));
	// return document;
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return null;
	// }
	//
	// /**
	// * Gets the XML string representation from an XML file at the given URL,
	// * decoding using the specified charset.
	// *
	// * @param urlToXMLFile
	// * @param charsetName
	// * @return
	// * @throws URISyntaxException
	// * @throws UnsupportedEncodingException
	// * @throws IOException
	// */
	// public static String getXMLText(URL urlToXMLFile, String charsetName)
	// throws URISyntaxException, UnsupportedEncodingException,
	// IOException {
	// Path resPath = Paths.get(urlToXMLFile.toURI());
	// return new String(Files.readAllBytes(resPath), charsetName);
	// }
	//
	// public static String getAttributeValue(String attributeName, String
	// xmlBlock) {
	// Document xmlBlockDoc = convertXMLStringToXMLDocument(xmlBlock);
	// return xmlBlockDoc.getAttributes().getNamedItem(attributeName)
	// .getNodeValue();
	// }
	//
	// public static List<String> getXMLBlocks(String xmlText, String path)
	// throws XPathExpressionException {
	// List<String> blocks = new ArrayList<String>();
	// Document xmlTextDoc = convertXMLStringToXMLDocument(xmlText);
	//
	// XPath xpath = XPathFactory.newInstance().newXPath();
	// String expression = path;
	// NodeList nodes;
	// nodes = (NodeList) xpath.evaluate(expression, xmlTextDoc,
	// XPathConstants.NODESET);
	// TransformerFactory transFactory = TransformerFactory.newInstance();
	// Transformer transformer = null;
	// try {
	// transformer = transFactory.newTransformer();
	//
	// for (int i = 0; i < nodes.getLength(); i++) {
	// StringWriter buffer = new StringWriter();
	// transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
	// "yes");
	// transformer.transform(new DOMSource(nodes.item(i)),
	// new StreamResult(buffer));
	// blocks.add(buffer.toString());
	// }
	// } catch (TransformerException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// return blocks;
	// }

}
