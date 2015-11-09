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
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import it.polimi.modaclouds.resourcemodel.cloud.CloudElement;
import it.polimi.modaclouds.resourcemodel.cloud.CloudPlatform;
import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;
import it.polimi.modaclouds.resourcemodel.cloud.Cost;
import it.polimi.modaclouds.resourcemodel.cloud.CostProfile;
import it.polimi.modaclouds.resourcemodel.cloud.V_Storage;
import it.polimi.modaclouds.resourcemodel.cloud.VirtualHWResource;
import it.polimi.modaclouds.space4cloud.types.palladio.AllocationProfile;
import it.polimi.modaclouds.space4cloud.types.palladio.AllocationSpecification;
import it.polimi.modaclouds.space4cloud.types.palladio.ExtendedResourceContainer;

// TODO: Auto-generated Javadoc
/**
 * Provides utility methods to derive system costs.
 * 
 * @author Davide Franceschelli
 * 
 */
public class CostDerivation {

	/** The output. */
	private File output;

	/** The doc. */
	private Document doc;

	/** The root. */
	private Element root;

	/** The total cost. */
	private double totalCost = 0.0;

	/** The hourly costs. */
	private double hourlyCosts[];

	/**
	 * Initialize the class creating the file "costs.xml" in the specified path.
	 * 
	 * @param outputPath
	 *            is the absolute path of the cost file to create.
	 */
	public CostDerivation(String outputPath) {
		output = new File(outputPath + "costs.xml");
		hourlyCosts = new double[24];
		try {
			DocumentBuilderFactory x = DocumentBuilderFactory.newInstance();
			DocumentBuilder y = x.newDocumentBuilder();
			doc = y.newDocument();
			root = doc.createElement("Daily_System_Costs");
			doc.appendChild(root);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Derives the system costs analyzing the mapping between Extended Resource
	 * Containers and Cloud Elements. Information about costs is saved within
	 * the cost model serialized within the "costs.xml" file. Cloud Platforms
	 * and Cloud Resources are automatically recognized and separately treated.
	 * 
	 * @param map
	 *            is the Map object containing key-value elements, where the key
	 *            is an ExtendedResourceContainer object, while the value is a
	 *            CloudElement object.
	 * @throws TransformerException
	 * @see CloudElement
	 * @see ExtendedResourceContainer
	 * @see #deriveCostsForCloudPlatform(ExtendedResourceContainer,
	 *      CloudPlatform)
	 * @see #deriveCostsForCloudResource(ExtendedResourceContainer,
	 *      CloudResource)
	 */
	public void derive(Map<ExtendedResourceContainer, CloudElement> map)
			throws TransformerException {
		for (Map.Entry<ExtendedResourceContainer, CloudElement> e : map
				.entrySet())
			if (e.getValue() instanceof CloudResource)
				deriveCostsForCloudResource(e.getKey(),
						(CloudResource) e.getValue());
			else if (e.getValue() instanceof CloudPlatform)
				deriveCostsForCloudPlatform(e.getKey(),
						(CloudPlatform) e.getValue());
		for (int i = 0; i < 24; i++) {
			Element z = doc.createElement("Total_Hourly_Cost");
			z.setAttribute("hour", "" + i);
			z.setAttribute("cost", "" + hourlyCosts[i]);
			root.appendChild(z);
		}
		Element x = doc.createElement("Total_Daily_Cost");
		x.setAttribute("value", "" + totalCost);
		root.appendChild(x);
		serialize();
	}

	/**
	 * Helper method used to derive costs starting from a list of Costs, a Cost
	 * Profile and an Allocation Profile. The cost model is updated accordingly.
	 * 
	 * @param lc
	 *            is the List of Cost elements.
	 * @param cp
	 *            is the CostProfile object.
	 * @param costList
	 *            is the Element representing the root of the cost list within
	 *            the cost model.
	 * @param as
	 *            is the AllocationProfile object.
	 * @return the total cost derived from the input parameters.
	 * @see Cost
	 * @see CostProfile
	 * @see AllocationProfile
	 */
	private double deriveCosts(List<Cost> lc, CostProfile cp, Element costList,
			AllocationSpecification[] as) {
		Element el;
		double cost = 0.0, temp;

		// Consider the costs which do not belong to a cost profile.
		if (lc != null)
			for (Cost c : lc) {
				temp = 0.0;
				el = doc.createElement("Cost");
				el.setAttribute("unit", c.getUnit().getName());
				el.setAttribute("lowerBound", "" + c.getLowerBound());
				el.setAttribute("upperBound", "" + c.getUpperBound());
				el.setAttribute("value", "" + c.getValue());
				el.setTextContent(c.getDescription());
				costList.appendChild(el);
				switch (c.getUnit()) {

				// Instances have per hour costs
				case PER_HOUR:
					// Instances (VM) scale according to the Allocation Profile,
					// so for each hour we consider the allocation size and we
					// multiply it for the cost value.
					if (as != null)
						for (int i = 0; i < 24; i++) {
							double delta = c.getValue() * as[i].getSize();
							temp += delta;
							hourlyCosts[i] += delta;
						}
					// If the Allocation Profile is not defined, then we suppose
					// that for each hour the allocation size is one.
					else {
						temp += c.getValue() * 24;
						for (int i = 0; i < 24; i++) {
							hourlyCosts[i] += c.getValue();
						}
					}
					break;

				// Storage systems (storage/DB) have per GB-month costs
				case PER_GBMONTH:
					// Storage resources do not scale, so we don't have to take
					// into account allocation when deriving costs! We must
					// consider the daily cost, so we multiply the cost for the
					// size in GB, divide by (365*24)/12=730 hours/month and
					// multiply by 24 hours/day.
					VirtualHWResource v = c.getDefinedOn();
					Element vel = doc.createElement("VHR");
					vel.setAttribute("id", "" + v.getId());
					vel.setAttribute("type", v.getType().getName());
					int size;
					if (v != null)
						switch (v.getType()) {
						case MEMORY:
							/*
							 * size = ((V_Memory) v).getSize();
							 * vel.setAttribute("size", "" + size); temp =
							 * (getIntervalCost(c, size / 1024) * 24) / 730;
							 */
							break;
						case STORAGE:
							size = ((V_Storage) v).getSize();
							vel.setAttribute("size", "" + size);
							double delta = getIntervalCost(c, size) / 730;
							temp += delta * 24;
							for (int i = 0; i < 24; i++)
								hourlyCosts[i] += delta;
							break;
						case CPU:
						default:
							break;
						}
					el.appendChild(vel);
					break;

				// Future works
				case PER_MILLION_IO:
				default:
					break;
				}

				// Update the total cost
				cost += temp;
			}

		// Consider the Cost Profile
		if (cp != null) {
			lc = cp.getComposedOf();
			if (lc != null)
				for (Cost c : lc) {
					temp = 0.0;
					el = doc.createElement("Cost");
					el.setAttribute("unit", c.getUnit().getName());
					el.setAttribute("lowerBound", "" + c.getLowerBound());
					el.setAttribute("upperBound", "" + c.getUpperBound());
					el.setAttribute("value", "" + c.getValue());
					el.setAttribute("period", "" + c.getPeriod());
					el.setTextContent(c.getDescription());
					costList.appendChild(el);
					switch (c.getUnit()) {

					// Instances have per hour costs
					case PER_HOUR:
						// Instances (VM) scale according to the Allocation
						// Profile, so we have to take into account the
						// allocation size corresponding to the actual cost
						// reference period.
						int n = as != null ? as[c.getPeriod()].getSize() : 1;
						temp = c.getValue() * n;
						hourlyCosts[c.getPeriod()] = temp;
						break;

					// Storage systems (storage/DB) have per GB-month costs
					case PER_GBMONTH:
						// Storage resources do not scale, so we don't have to
						// take into account allocation when deriving costs! We
						// must consider only the per hour cost because the cost
						// profile is supposed to be composed of 24 cost
						// definitions, one for each hour of the day. So we
						// multiply for the size in GB and divide by
						// (365*24)/12=730 hours/month.
						VirtualHWResource v = c.getDefinedOn();
						int size;
						if (v != null)
							switch (v.getType()) {
							case MEMORY:
								/*
								 * size = ((V_Memory) v).getSize();
								 * System.out.println("Storage Size: " + size);
								 * temp = getIntervalCost(c, size / 1024) / 730;
								 */
								break;
							case STORAGE:
								size = ((V_Storage) v).getSize();
								temp = getIntervalCost(c, size) / 730;
								hourlyCosts[c.getPeriod()] = temp;
								break;
							case CPU:
							default:
								break;
							}
						break;
					case PER_MILLION_IO:
					default:
						break;
					}

					// Update the total cost
					cost += temp;
				}
		}
		return cost;
	}

	/**
	 * Derives costs from the mapping between an Extended Resource Container and
	 * a Cloud Platform.
	 * 
	 * @param erc
	 *            is the ExtendedResourceContainer object derived from the
	 *            CloudPlatform.
	 * @param cp
	 *            is the CloudPlatform object.
	 * @see ExtendedResourceContainer
	 * @see CloudPlatform
	 */
	private void deriveCostsForCloudPlatform(ExtendedResourceContainer erc,
			CloudPlatform cp) {
		List<Cost> lc = cp.getHasCost();
		CostProfile costp = cp.getHasCostProfile();
		List<CloudResource> lcr = cp.getRunsOnCloudResource();
		Element x = doc.createElement("CloudPlatform");
		x.setAttribute("name", cp.getName());
		x.setAttribute("type", cp.getPlatformType().getName());
		x.setAttribute("id", "" + cp.getId());
		x.setAttribute("mappedTo", erc.getName() + "[" + erc.getId() + "]");
		Element costList = doc.createElement("Cost_List");
		x.appendChild(costList);
		AllocationProfile ap = erc.getAllocationProfile();
		AllocationSpecification[] as = null;
		if (ap != null)
			as = ap.getSpecifications();
		double cost = deriveCosts(lc, costp, costList, as);
		if (lcr != null)
			for (CloudResource cr : lcr) {
				Element y = doc.createElement("CloudResource");
				y.setAttribute("name", cr.getName());
				y.setAttribute("type", cr.getResourceType().getName());
				y.setAttribute("id", "" + cr.getId());
				List<Cost> lc1 = cr.getHasCost();
				CostProfile cp1 = cr.getHasCostProfile();
				Element costList1 = doc.createElement("Cost_List");
				y.appendChild(costList1);
				double temp = deriveCosts(lc1, cp1, costList1, as);

				// Update the total system cost
				cost += temp;

				x.appendChild(y);
			}
		totalCost += cost;
		Element total = doc.createElement("Total_Container_Cost");
		total.setAttribute("value", "" + cost);
		x.appendChild(total);
		root.appendChild(x);
	}

	/**
	 * Derives costs from the mapping between an Extended Resource Container and
	 * a Cloud Resource.
	 * 
	 * @param erc
	 *            is the ExtendedResourceContainer object derived from the
	 *            CloudResource.
	 * @param cr
	 *            is the CloudResource object.
	 * @see ExtendedResourceContainer
	 * @see CloudResource
	 */
	private void deriveCostsForCloudResource(ExtendedResourceContainer erc,
			CloudResource cr) {
		Element x = doc.createElement("CloudResource");
		x.setAttribute("name", cr.getName());
		x.setAttribute("type", cr.getResourceType().getName());
		x.setAttribute("id", "" + cr.getId());
		x.setAttribute("mappedTo", erc.getName() + "[" + erc.getId() + "]");
		List<Cost> lc = cr.getHasCost();
		CostProfile cp = cr.getHasCostProfile();
		Element costList = doc.createElement("Cost_List");
		x.appendChild(costList);
		AllocationProfile ap = erc.getAllocationProfile();
		AllocationSpecification[] as = null;
		if (ap != null)
			as = ap.getSpecifications();
		double cost = deriveCosts(lc, cp, costList, as);
		totalCost += cost;
		Element total = doc.createElement("Total_Container_Cost");
		total.setAttribute("value", "" + cost);
		x.appendChild(total);
		root.appendChild(x);
	}

	/**
	 * Derive the cost relative to the specified size considering the cost
	 * intervals defined within the cost specification.
	 * 
	 * @param c
	 *            is the Cost specification.
	 * @param size
	 *            is the size to check against the intervals.
	 * @return the cost associated to the specified size, relative to the
	 *         intervals within the specified cost specification.
	 * @see Cost
	 */
	private double getIntervalCost(Cost c, int size) {
		int a = c.getLowerBound();
		int b = c.getUpperBound();

		if (size < 0)
			return 0.0;

		if (a < 0)
			if (b < 0)
				// case: |0|----|size|
				return c.getValue() * size;
			else {
				if (size < b)
					// case: |0|--|size|----|b|
					return c.getValue() * size;
				else
					// case: |0|---|b|------|size|
					return c.getValue() * b;
			}
		else {
			if (b < a)
				if (size > a)
					// case: |b|---|a|----|size|
					return c.getValue() * (size - a);
				else
					// case: |b|---|size|------|a|
					// OR
					// case: |size|---|b|----|a|
					return 0.0;
			else {
				if (size < b)
					if (size > a)
						// case: |a|----|size|-------|b|
						return c.getValue() * (size - a);
					else
						// case: |size|---|a|----|b|
						return 0.0;
				else
					// case: |a|-----|b|----|size|
					return c.getValue() * (b - a);
			}
		}
	}

	/**
	 * Serializes the cost model.
	 * 
	 * @throws TransformerException
	 */
	private void serialize() throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "4");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(output);
		transformer.transform(source, result);
	}
}
