/*******************************************************************************
 * Copyright 2014 Giovanni Paolo Gibilisco
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * 
 */
package it.polimi.modaclouds.space4cloud.utils;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

// TODO: Auto-generated Javadoc
/**
 * Provides useful methods to handle XML based documents and models.
 * 
 * @author Davide Franceschelli
 * 
 */
public class DOM {

	/**
	 * Provides a new Document.
	 * 
	 * @return the new Document object.
	 */
	public static Document getDocument() {
		return getDocument(null);
	}

	/**
	 * Loads an existing Document.
	 * 
	 * @param f
	 *            is the input File containing the serialized document.
	 * @return the de-serialized Document if the input File is not null, a new
	 *         Document otherwise.
	 */
	public static Document getDocument(File f) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			if (f != null)
				if (f.exists())
					doc = docBuilder.parse(f);
			return doc;
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
	}

	/**
	 * Serializes the DOM within a File using indentation, with indentation
	 * amount equals to 4.
	 * 
	 * @param doc
	 *            is the Document to serialize.
	 * @param f
	 *            is the output File.
	 */
	public static void serialize(Document doc, File f) {
		serialize(doc, f, true, 4);
	}

	/**
	 * Serializes the DOM within a File.
	 * 
	 * @param doc
	 *            is the Document to serialize.
	 * @param f
	 *            is the output File.
	 * @param indent
	 *            is the boolean that indicates whether or not to use
	 *            indentation.
	 * @param indent_amount
	 *            is the integer that indicates indicates the indentation
	 *            amount.
	 */
	public static void serialize(Document doc, File f, boolean indent,
			int indent_amount) {
		try {
			if (indent_amount < 0)
				indent_amount = 0;
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes"
					: "no");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", ""
							+ indent_amount);
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(f);
			transformer.transform(source, result);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Instantiates a new dom.
	 */
	private DOM() {
	}
}
