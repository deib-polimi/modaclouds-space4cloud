package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.polimi.modaclouds.qos_models.schema.CostType;
import it.polimi.modaclouds.qos_models.schema.Costs;
import it.polimi.modaclouds.qos_models.schema.Costs.Providers;
import it.polimi.modaclouds.qos_models.schema.HourPriceType;
import it.polimi.modaclouds.qos_models.schema.MultiCloudExtension;
import it.polimi.modaclouds.qos_models.schema.MultiCloudExtensions;
import it.polimi.modaclouds.qos_models.schema.ObjectFactory;
import it.polimi.modaclouds.qos_models.schema.Provider;
import it.polimi.modaclouds.qos_models.schema.Replica;
import it.polimi.modaclouds.qos_models.schema.ReplicaElement;
import it.polimi.modaclouds.qos_models.schema.ResourceContainer;
import it.polimi.modaclouds.qos_models.schema.ResourceModelExtension;
import it.polimi.modaclouds.qos_models.schema.WorkloadPartition;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.contractor4cloud.Contractor;
import it.polimi.modaclouds.space4cloud.db.DataHandler;
import it.polimi.modaclouds.space4cloud.db.DataHandlerFactory;
import it.polimi.modaclouds.space4cloud.optimization.bursting.PrivateCloud;
import it.polimi.modaclouds.space4cloud.utils.Configuration;

/**
 * This class should handle a multi-provider solution, or also, in the
 * particular case, that of a single solution.
 * 
 */
public class SolutionMulti implements Cloneable, Serializable, Iterable<Solution> {

	private static final long serialVersionUID = -9050926347950168327L;
	private static final Logger logger = LoggerFactory.getLogger(SolutionMulti.class);

	private int generationIteration = 0;
	private long generationTime = 0;

	public boolean usesPaaS() {
		for (Solution s : solutions.values())
			if (s.usesPaaS())
				return true;
		return false;
	}

	public static double getCost(File solution) {
		if (isCosts(solution))
			return getCostFromCosts(solution);
		else
			return getCostFromFileSolution(solution);
	}

	@Deprecated
	private static double getCostFromFileSolution(File solution) {
		double cost = Double.MAX_VALUE;

		if (solution != null && solution.exists())
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(solution);
				doc.getDocumentElement().normalize();

				{
					Element root = (Element) doc.getElementsByTagName("SolutionMultiResult").item(0);

					cost = Double.parseDouble(root.getAttribute("cost"));

				}
			} catch (Exception e) {
				logger.error("Error while reading the cost from a solution file.", e);
			}

		return cost;
	}

	private static double getCostFromCosts(File solution) {
		try {
			Costs costs = XMLHelper.deserialize(solution.toURI().toURL(), Costs.class);

			CostType ct = costs.getCost();
			if (ct != null)
				return ct.getTotalCost();

		} catch (MalformedURLException | JAXBException | SAXException e) {
			logger.error("Error in getting the cost from the file", e);
		}
		return Double.MAX_VALUE;
	}

	public static int getDuration(File solution) {
		int duration = Integer.MAX_VALUE;

		if (solution != null && solution.exists())
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(solution);
				doc.getDocumentElement().normalize();

				{
					Element root = (Element) doc.getElementsByTagName("SolutionMultiResult").item(0);

					duration = Integer.parseInt(root.getAttribute("generationTime"));

				}
			} catch (Exception e) {
				logger.error("Error while reading the duration from a solution file.", e);
			}

		return duration;
	}

	public static boolean isEmpty(File solution) {
		if (isResourceModelExtension(solution))
			return isEmptyResourceModelExtension(solution);
		else
			return isEmptyFileSolution(solution);
	}

	public static boolean isResourceModelExtension(File f) {
		try {
			XMLHelper.deserialize(f.toURI().toURL(), ResourceModelExtension.class);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isCosts(File f) {
		try {
			XMLHelper.deserialize(f.toURI().toURL(), Costs.class);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean isEmptyResourceModelExtension(File solution) {
		try {
			ResourceModelExtension rme = XMLHelper.deserialize(solution.toURI().toURL(), ResourceModelExtension.class);

			for (ResourceContainer rc : rme.getResourceContainer()) {
				it.polimi.modaclouds.qos_models.schema.CloudService cs = rc.getCloudElement();
				if (cs != null) {
					Replica r = cs.getReplicas();
					if (r != null) {
						List<ReplicaElement> re = r.getReplicaElement();
						if (re.size() > 0)
							return false;
					}
				}
			}

		} catch (MalformedURLException | JAXBException | SAXException e) {
			logger.error("Error in checking if the solution is empty", e);
		}
		return true;
	}

	@Deprecated
	private static boolean isEmptyFileSolution(File solution) {
		if (solution != null && solution.exists())
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(solution);
				doc.getDocumentElement().normalize();

				{
					NodeList nl = doc.getElementsByTagName("HourAllocation");
					return (nl.getLength() == 0);
				}
			} catch (Exception e) {
				logger.error("Error in checking if the solution is empty", e);
			}
		return true;
	}

	/**
	 * if the solution has been evaluated or not.
	 */
	private boolean evaluated = false;

	/** if the solution is feasible or not. */
	private boolean feasible = false;

	/** The Cost. */
	private double cost = 0.0;

	private HashMap<String, Solution> solutions;

	public SolutionMulti() {
		this.solutions = new HashMap<String, Solution>();
	}

	public void add(Solution s) {
		String provider = s.getProvider();
		if (!provider.equals("Error")) {
			this.solutions.put(provider, s);
			updateEvaluation();
			signalProvidersNumber();
		} else
			logger.error("Error! The provider isn't defined in the solution!");
	}

	public void remove(Solution s) {
		String provider = s.getProvider();
		if (this.solutions.remove(provider) != null) {
			updateEvaluation();
			signalProvidersNumber();
		} else
			logger.error("Error! The provider isn't defined in the solution!");
	}

	private void signalProvidersNumber() {
		for (Solution s : solutions.values())
			s.setTotalProviders(solutions.size());
	}

	public void removeUselessSolutions() {
		for (Solution s : getAll()) {
			if (!s.hasAtLeastOneReplicaInOneHour())
				remove(s);
		}
	}

	/**
	 * Change values of a certain resource far all the solutions.
	 * 
	 * @param resId
	 *            the res id
	 * @param propertyNames
	 *            the property names
	 * @param propertyValues
	 *            the property values
	 */
	public void changeValues(String resId, ArrayList<String> propertyNames, ArrayList<Object> propertyValues) {

		for (Solution s : getAll())
			s.changeValues(resId, propertyNames, propertyValues);

		updateEvaluation();

	}

	@Override
	public SolutionMulti clone() {
		SolutionMulti clone;
		try {
			clone = (SolutionMulti) super.clone();
		} catch (CloneNotSupportedException e) {
			clone = new SolutionMulti();
		}

		clone.solutions = new HashMap<String, Solution>();
		for (Solution s : getAll())
			clone.add(s.clone());

		return clone;
	}

	public void exportCSV(Path filePath) {
		String text = "";

		text += "total cost: " + costFormatter.format(getCost()) + "\n";

		text += "number of providers: " + solutions.size() + "\n\n";

		for (Solution s : getAll()) {
			ArrayList<Instance> hourApplication = s.getApplications();

			text += "cost: " + s.getCost() + "\n";
			text += "provider: " + s.getProvider() + "\n";

			for (Tier t : hourApplication.get(0).getTiers())
				text += t.getId() + ",";
			text += "\n";
			for (Instance i : hourApplication) {
				for (Tier t : i.getTiers())
					text += s.getReplicas(t) + ",";
				text += "\n";
			}

			for (Tier t : hourApplication.get(0).getTiers())
				for (Component c : t.getComponents())
					text += c.getId() + ",";
			text += "\n";

			for (Instance i : hourApplication) {
				for (Tier t : i.getTiers())
					for (Component c : t.getComponents())
						text += c.getResponseTime() + ",";

				text += "\n";
			}

			text += "\n";

		}

		try {
			PrintWriter outFile = new PrintWriter(filePath.toFile());
			outFile.println(text);
			outFile.close();
		} catch (FileNotFoundException e) {
			logger.error("Error while exporting the data via CSV.", e);
		}
	}
	
	
	public void exportStatisticCSV(Path filePath) {
		String text = "Generation Time, Cost, Feasibility\n";
		text += getGenerationTime() + ",";
		logger.info("Cost>"+getCost());		
		text += getCost() + ",";
		text += isFeasible()+"\n";


		try {
			PrintWriter outFile = new PrintWriter(filePath.toFile());
			outFile.println(text);
			outFile.close();
		} catch (FileNotFoundException e) {
			logger.error("Error while exporting the data via CSV.", e);
		}
	}

	public void exportLight(Path filePath) {
		if (!isEvaluated()) {
			logger.error("Trying to export a solution that has not been evaluated!");
			return;
		}

		boolean res = exportAsFileSolution(filePath);
		if (res)
			exportMultiCloudInfoAsExtension(Paths.get(filePath.getParent().toString(), "mce.xml"));
	}

	@Deprecated
	public boolean exportAsFileSolution(Path filePath) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("SolutionMultiResult");
			doc.appendChild(rootElement);

			rootElement.setAttribute("generationTime", Long.toString(getGenerationTime()));
			rootElement.setAttribute("generationIteration", Integer.toString(getGenerationIteration()));
			// set cost
			rootElement.setAttribute("cost", "" + costFormatter.format(getCost()));
			// set feasibility
			rootElement.setAttribute("feasibility", "" + isFeasible());

			for (Solution sol : getAll()) {
				Element solution = doc.createElement("Solution");
				rootElement.appendChild(solution);

				solution.setAttribute("provider", sol.getProvider());
				// set cost
				solution.setAttribute("cost", Double.toString(sol.getCost()));
				// set generation time
				solution.setAttribute("generationTime", Long.toString(sol.getGenerationTime()));
				// set generation iteration
				solution.setAttribute("generationIteration", Integer.toString(sol.getGenerationIteration()));
				// set feasibility
				solution.setAttribute("feasibility", Boolean.toString(sol.isFeasible()));

				ArrayList<Instance> hourApplication = sol.getApplications();

				// create tier container element
				Element tiers = doc.createElement("Tiers");
				solution.appendChild(tiers);
				for (Tier t : hourApplication.get(0).getTiers()) {
					// create the tier
					Element tier = doc.createElement("Tier");
					tiers.appendChild(tier);

					// set id, name, provider name, service name, resource name,
					// service type
					tier.setAttribute("id", t.getId());
					tier.setAttribute("name", t.getPcmName());

					CloudService cs = t.getCloudService();
					tier.setAttribute("providerName", cs.getProvider());
					tier.setAttribute("serviceName", cs.getServiceName());
					tier.setAttribute("resourceName", cs.getResourceName());
					tier.setAttribute("serviceType", cs.getServiceType());

					if (cs instanceof IaaS || (cs instanceof PaaS && ((PaaS) cs).areReplicasChangeable())) {
						for (int i = 0; i < 24; i++) {
							// create the allocation element
							Element hourAllocation = doc.createElement("HourAllocation");
							tier.appendChild(hourAllocation);
							hourAllocation.setAttribute("hour", "" + i);
							hourAllocation.setAttribute("allocation",
									"" + sol.getReplicas(hourApplication.get(i).getTierById(t.getId())));
							if (sol.getProvider().indexOf(PrivateCloud.BASE_PROVIDER_NAME) > -1)
								hourAllocation.setAttribute("hosts",
										"" + PrivateCloud.getInstance().getUsedHostsForTier(i, t.getId()).size());
						}
					}
				}

				// create the element with the response times
				Element functionalities = doc.createElement("functionalities");
				solution.appendChild(functionalities);

				HashMap<String, Functionality> funcList = new HashMap<>();
				for (Tier t : hourApplication.get(0).getTiers())
					for (Component c : t.getComponents())
						for (Functionality f : c.getFunctionalities())
							funcList.put(f.getId(), f);

				for (String id : funcList.keySet()) {
					// create the tier
					Element functionality = doc.createElement("Functionality");

					solution.appendChild(functionality);

					// set id, name, provider name, service name, resource name,
					// service type
					functionality.setAttribute("id", id);
					functionality.setAttribute("name", funcList.get(id).getName());

					for (int i = 0; i < 24; i++) {
						// create the allocation element
						Element hourlyRT = doc.createElement("HourlyRT");
						functionality.appendChild(hourlyRT);
						hourlyRT.setAttribute("hour", "" + i);
						for (Tier t : hourApplication.get(i).getTiers())
							for (Component c : t.getComponents())
								for (Functionality fun : c.getFunctionalities())
									if (fun.getId().equals(id))
										hourlyRT.setAttribute("responseTime", "" + fun.getResponseTime());

					}
				}

			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			File file = filePath.toFile();
			StreamResult result = new StreamResult(file);
			logger.info("Exported in: " + file.getAbsolutePath());

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);

			return true;
		} catch (ParserConfigurationException | TransformerException e) {
			logger.error("Error exporting the solution statistics", e);
			return false;
		}
	}

	public boolean exportMultiCloudInfoAsExtension(Path path) {
		if (!isEvaluated()) {
			logger.error("Trying to export a solution that has not been evaluated!");
			return false;
		}

		MultiCloudExtensions mces = getMultiCloudInfoAsExtension();

		try {
			XMLHelper.serialize(mces, MultiCloudExtensions.class, new FileOutputStream(path.toFile()));
			return true;
		} catch (FileNotFoundException | JAXBException e) {
			logger.error("Error while exporting the solution.", e);
			return false;
		}
	}

	public MultiCloudExtensions getMultiCloudInfoAsExtension() {
		MultiCloudExtensions mces = new MultiCloudExtensions();
		MultiCloudExtension mce = new MultiCloudExtension();
		mces.setMultiCloudExtensions(mce);

		mce.setId(hashCode() + "");

		for (Solution sol : getAll()) {

			Provider p = new Provider();
			mce.getProvider().add(p);

			p.setName(sol.getProvider());

			for (int i = 0; i < 24; i++) {
				WorkloadPartition wp = new WorkloadPartition();
				p.getWorkloadPartition().add(wp);

				wp.setHour(i);
				wp.setValue((int) (sol.getPercentageWorkload(i) * 100));
			}
		}

		return mces;
	}

	public Solution get(int i) {
		if (i < 0 || i >= size())
			return null;

		Collection<Solution> sols = getAll();
		int k = 0;
		for (Solution s : sols) {
			if (k == i)
				return s;
			++k;
		}
		return null;
	}

	public Solution get(String provider) {
		return solutions.get(provider);
	}

	public Collection<Solution> getAll() {
		return solutions.values();
	}

	public double getCost() {
		return cost;
	}

	public static DecimalFormat costFormatter = null;

	static {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
		otherSymbols.setDecimalSeparator('.');
		costFormatter = new DecimalFormat("0.00", otherSymbols);
	}

	public boolean greaterThan(SolutionMulti sol) {

		if (sol == null)
			return true;
		if (isFeasible() && sol.isFeasible()) {
			/* if both are feasible */
			if (getCost() >= 0) {
				if (sol.getCost() >= 0) {
					return getCost() < sol.getCost();
				} else {
					return true;
				}
			} else {
				if (sol.getCost() >= 0) {
					return false;
				} else {
					return (getNumberOfViolatedConstraints() <= sol.getNumberOfViolatedConstraints());
				}
			}
		} else if (!isFeasible() && !sol.isFeasible()) {

			/*
			 * if both are not feasible we consider the solution with the
			 * minimum number of violated constraints among the two
			 */

			return (getNumberOfViolatedConstraints() <= sol.getNumberOfViolatedConstraints());

		} else {

			return this.isFeasible();
		}

	}

	private int getNumberOfViolatedConstraints() {
		int constraints = 0;
		for (Solution sol : this.getAll()) {
			constraints += sol.getNumberOfViolatedConstraints();
		}
		return constraints;
	}

	public boolean isEvaluated() {
		return evaluated;
	}

	public boolean isFeasible() {
		return feasible;
	}

	private boolean setFromResourceModelExtension(File initialSolution) {
		boolean res = false;

		if (initialSolution != null) {
			try {
				ResourceModelExtension rme = XMLHelper.deserialize(initialSolution.toURI().toURL(),
						ResourceModelExtension.class);

				cost = Double.MAX_VALUE;

				for (ResourceContainer rc : rme.getResourceContainer()) {
					it.polimi.modaclouds.qos_models.schema.CloudService cs = rc.getCloudElement();

					String provider = rc.getProvider();
					String tierId = rc.getId();
					String resourceName = cs.getResourceSizeID();
					String serviceName = cs.getServiceName();

					Solution solution = get(provider);
					if (solution == null)
						continue;

					DataHandler dataHandler = DataHandlerFactory.getHandler();

					double speed = dataHandler.getProcessingRate(provider, serviceName, resourceName);
					int ram = dataHandler.getAmountMemory(provider, serviceName, resourceName);
					int numberOfCores = dataHandler.getNumberOfReplicas(provider, serviceName, resourceName);

					Replica r = cs.getReplicas();
					if (r == null)
						continue;
					List<ReplicaElement> hourAllocations = r.getReplicaElement();

					if (hourAllocations.size() < 24) {
						int minValue = 0;
						ArrayList<Integer> hours = new ArrayList<Integer>();
						for (ReplicaElement m : hourAllocations) {
							hours.add(m.getHour());
							if (minValue > m.getValue())
								minValue = m.getValue();
						}

						if (cs.getServiceType().equals("PaaS"))
							minValue = 1;

						for (int h = 0; h < 24; ++h) {
							if (hours.contains(h))
								continue;
							ReplicaElement m = new ReplicaElement();
							m.setHour(h);
							m.setValue(minValue);
							hourAllocations.add(m);
						}
					}

					for (ReplicaElement m : hourAllocations) {
						int hour = m.getHour();
						int allocation = m.getValue();

						Instance app = solution.getApplication(hour);

						ArrayList<String> propertyNames = new ArrayList<String>();
						ArrayList<Object> propertyValues = new ArrayList<Object>();

						propertyNames.add("replicas");
						propertyValues.add(allocation);

						propertyNames.add("resourceName");
						propertyNames.add("speed");
						propertyNames.add("ram");
						propertyNames.add("numberOfCores");
						propertyValues.add(resourceName);
						propertyValues.add(speed);
						propertyValues.add(ram);
						propertyValues.add(numberOfCores);

						app.changeValues(tierId, propertyNames, propertyValues);

						app.setEvaluated(false);
					}

				}

				res = true;

			} catch (Exception e) {
				logger.error("Error while importing data from a solution file.", e);
				return false;
			}
		}

		return res;
	}

	@Deprecated
	private boolean setFromFileSolution(File initialSolution) {
		boolean res = false;

		if (initialSolution != null) {
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(initialSolution);
				doc.getDocumentElement().normalize();

				{
					Element root = (Element) doc.getElementsByTagName("SolutionMultiResult").item(0);

					cost = (int) Math.round(Double.parseDouble(root.getAttribute("cost")));
				}

				NodeList tiers = doc.getElementsByTagName("Tier");

				for (int i = 0; i < tiers.getLength(); ++i) {
					Node n = tiers.item(i);

					if (n.getNodeType() != Node.ELEMENT_NODE)
						continue;

					Element tier = (Element) n;
					String provider = tier.getAttribute("providerName");
					String tierId = tier.getAttribute("id");
					String resourceName = tier.getAttribute("resourceName");
					String serviceName = tier.getAttribute("serviceName");
					String serviceType = tier.getAttribute("serviceType");

					Solution solution = get(provider);
					if (solution == null)
						continue;

					DataHandler dataHandler = DataHandlerFactory.getHandler();

					double speed = dataHandler.getProcessingRate(provider, serviceName, resourceName);
					int ram = dataHandler.getAmountMemory(provider, serviceName, resourceName);
					int numberOfCores = dataHandler.getNumberOfReplicas(provider, serviceName, resourceName);

					// System.out.printf("DEBUG: %s, %s, %s <-> %f, %d, %d.\n",
					// provider, serviceName, resourceName, (float)speed, ram,
					// numberOfCores);

					NodeList hourAllocations = tier.getElementsByTagName("HourAllocation");

					if (hourAllocations.getLength() < 24) {
						int minValue = 0;
						ArrayList<Integer> hours = new ArrayList<Integer>();

						for (int j = 0; j < hourAllocations.getLength(); ++j) {
							Node m = hourAllocations.item(j);

							if (m.getNodeType() != Node.ELEMENT_NODE)
								continue;

							Element hourAllocation = (Element) m;
							int hour = Integer.parseInt(hourAllocation.getAttribute("hour"));
							int allocation = Integer.parseInt(hourAllocation.getAttribute("allocation"));

							hours.add(hour);
							if (minValue > allocation)
								minValue = allocation;
						}

						if (serviceType.equals("PaaS"))
							minValue = 1;

						for (int h = 0; h < 24; ++h) {
							if (hours.contains(h))
								continue;

							Element m = doc.createElement("HourAllocation");
							m.setAttribute("hour", Integer.valueOf(h).toString());
							m.setAttribute("allocation", Integer.valueOf(minValue).toString());

							tier.appendChild(m);
						}
					}

					hourAllocations = tier.getElementsByTagName("HourAllocation");

					for (int j = 0; j < hourAllocations.getLength(); ++j) {
						Node m = hourAllocations.item(j);

						if (m.getNodeType() != Node.ELEMENT_NODE)
							continue;

						Element hourAllocation = (Element) m;
						int hour = Integer.parseInt(hourAllocation.getAttribute("hour"));
						int allocation = Integer.parseInt(hourAllocation.getAttribute("allocation"));

						Instance app = solution.getApplication(hour);

						ArrayList<String> propertyNames = new ArrayList<String>();
						ArrayList<Object> propertyValues = new ArrayList<Object>();

						propertyNames.add("replicas");
						propertyValues.add(allocation);

						propertyNames.add("resourceName");
						propertyNames.add("speed");
						propertyNames.add("ram");
						propertyNames.add("numberOfCores");
						propertyValues.add(resourceName);
						propertyValues.add(speed);
						propertyValues.add(ram);
						propertyValues.add(numberOfCores);

						app.changeValues(tierId, propertyNames, propertyValues);

						app.setEvaluated(false);
					}
				}

				res = true;

				// for (Solution s : getAll()) {
				// System.out.println("DEBUG prima: " + s.getProvider());
				// for (int i = 0; i < 24; ++i)
				// System.out.printf("%d (%d) ", s.getApplication(i)
				// .getWorkload(), (int) (s
				// .getPercentageWorkload(i) * 100));
				// System.out.println();
				// }

			} catch (Exception e) {
				logger.error("Error while importing data from a solution file.", e);
				return false;
			}
		}

		return res;
	}

	public boolean setFrom(File initialSolution, File initialMce) {
		boolean res = false;

		if (isResourceModelExtension(initialSolution))
			res = setFromResourceModelExtension(initialSolution);
		else
			res = setFromFileSolution(initialSolution);

		if (initialMce != null) {
			try {
				MultiCloudExtensions mces = XMLHelper.deserialize(initialMce.toURI().toURL(),
						MultiCloudExtensions.class);

				MultiCloudExtension mce = mces.getMultiCloudExtensions();

				for (Provider p : mce.getProvider()) {
					Solution s = get(p.getName());

					if (s == null)
						continue;

					int diff = 0;

					for (WorkloadPartition wp : p.getWorkloadPartition()) {
						Instance app = s.getApplication(wp.getHour());

						ArrayList<String> propertyNames = new ArrayList<String>();
						ArrayList<Object> propertyValues = new ArrayList<Object>();

						int value = wp.getValue();
						int minimumValue = 0;
						if (value < minimumValue) {
							diff = minimumValue - value;
							value = minimumValue;
						}

						propertyNames.add("workload");
						propertyValues.add((int) Math
								.ceil((app.getWorkload() / s.getPercentageWorkload(wp.getHour())) * value / 100));

						s.setPercentageWorkload(wp.getHour(), value / 100.0);
						app.changeValues(null, propertyNames, propertyValues);

						if (diff > 0) {
							for (Provider p2 : mce.getProvider()) {
								if (!p2.getName().equals(p.getName())) {
									Solution s2 = get(p2.getName());

									WorkloadPartition wp2 = p2.getWorkloadPartition().get(wp.getHour());

									Instance app2 = s2.getApplication(wp2.getHour());

									int value2 = wp2.getValue();
									if (value2 < (10 + diff)) {
										continue;
									} else {
										value2 -= diff;
										diff = 0;
									}

									ArrayList<String> propertyNames2 = new ArrayList<String>();
									ArrayList<Object> propertyValues2 = new ArrayList<Object>();

									propertyNames2.add("workload");
									propertyValues2.add((int) Math
											.ceil((app2.getWorkload() / s2.getPercentageWorkload(wp2.getHour()))
													* value2 / 100));

									s2.setPercentageWorkload(wp2.getHour(), value2 / 100.0);
									app2.changeValues(null, propertyNames2, propertyValues2);
								}
							}
						}
					}

				}

				res = true;

				// for (Solution s : getAll()) {
				// System.out.println("DEBUG dopo: " + s.getProvider());
				// for (int i = 0; i < 24; ++i)
				// System.out.printf("%d (%d) ", s.getApplication(i)
				// .getWorkload(), (int) (s
				// .getPercentageWorkload(i) * 100));
				// System.out.println();
				// }

			} catch (Exception e) {
				logger.error("Error while importing data from a multicloudextension file.", e);
				return false;
			}
		}

		updateEvaluation();

		return res;
	}

	public boolean addFrom(File initialSolution) {
		boolean res = false;

		if (isResourceModelExtension(initialSolution))
			res = addFromResourceModelExtension(initialSolution);
		else
			res = addFromFileSolution(initialSolution);

		updateEvaluation();

		return res;
	}

	private boolean addFromResourceModelExtension(File initialSolution) {
		boolean res = false;

		if (initialSolution != null) {
			try {
				ResourceModelExtension rme = XMLHelper.deserialize(initialSolution.toURI().toURL(),
						ResourceModelExtension.class);

				cost = Double.MAX_VALUE;

				for (ResourceContainer rc : rme.getResourceContainer()) {
					it.polimi.modaclouds.qos_models.schema.CloudService cs = rc.getCloudElement();

					String provider = rc.getProvider();
					String tierId = rc.getId();
					String resourceName = cs.getResourceSizeID();
					String serviceName = cs.getServiceName();

					Solution solution = get(provider);
					if (solution == null)
						continue;

					DataHandler dataHandler = DataHandlerFactory.getHandler();

					double speed = dataHandler.getProcessingRate(provider, serviceName, resourceName);
					int ram = dataHandler.getAmountMemory(provider, serviceName, resourceName);
					int numberOfCores = dataHandler.getNumberOfReplicas(provider, serviceName, resourceName);

					Replica r = cs.getReplicas();
					if (r == null)
						continue;
					List<ReplicaElement> hourAllocations = r.getReplicaElement();

					for (ReplicaElement m : hourAllocations) {
						int hour = m.getHour();
						int allocation = m.getValue();

						Instance app = solution.getApplication(hour);

						ArrayList<String> propertyNames = new ArrayList<String>();
						ArrayList<Object> propertyValues = new ArrayList<Object>();

						propertyNames.add("replicas");
						propertyValues.add(allocation + solution.getReplicas(app.getTierById(tierId)));

						propertyNames.add("resourceName");
						propertyNames.add("speed");
						propertyNames.add("ram");
						propertyNames.add("numberOfCores");
						propertyValues.add(resourceName);
						propertyValues.add(speed);
						propertyValues.add(ram);
						propertyValues.add(numberOfCores);

						app.changeValues(tierId, propertyNames, propertyValues);

						app.setEvaluated(false);
					}

				}

				res = true;

			} catch (Exception e) {
				logger.error("Error while importing data from a solution file.", e);
				return false;
			}
		}

		return res;
	}

	@Deprecated
	private boolean addFromFileSolution(File initialSolution) {
		boolean res = false;

		if (initialSolution != null) {
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(initialSolution);
				doc.getDocumentElement().normalize();

				{
					Element root = (Element) doc.getElementsByTagName("SolutionMultiResult").item(0);

					cost = (int) Math.round(Double.parseDouble(root.getAttribute("cost")));
				}

				NodeList tiers = doc.getElementsByTagName("Tier");

				for (int i = 0; i < tiers.getLength(); ++i) {
					Node n = tiers.item(i);

					if (n.getNodeType() != Node.ELEMENT_NODE)
						continue;

					Element tier = (Element) n;
					String provider = tier.getAttribute("providerName");
					String tierId = tier.getAttribute("id");
					String resourceName = tier.getAttribute("resourceName");
					String serviceName = tier.getAttribute("serviceName");

					Solution solution = get(provider);
					if (solution == null)
						continue;

					DataHandler dataHandler = DataHandlerFactory.getHandler();

					double speed = dataHandler.getProcessingRate(provider, serviceName, resourceName);
					int ram = dataHandler.getAmountMemory(provider, serviceName, resourceName);
					int numberOfCores = dataHandler.getNumberOfReplicas(provider, serviceName, resourceName);

					// System.out.printf("DEBUG: %s, %s, %s <-> %f, %d, %d.\n",
					// provider, serviceName, resourceName, (float)speed, ram,
					// numberOfCores);

					NodeList hourAllocations = tier.getElementsByTagName("HourAllocation");

					for (int j = 0; j < hourAllocations.getLength(); ++j) {
						Node m = hourAllocations.item(j);

						if (m.getNodeType() != Node.ELEMENT_NODE)
							continue;

						Element hourAllocation = (Element) m;
						int hour = Integer.parseInt(hourAllocation.getAttribute("hour"));
						int allocation = Integer.parseInt(hourAllocation.getAttribute("allocation"));

						Instance app = solution.getApplication(hour);

						ArrayList<String> propertyNames = new ArrayList<String>();
						ArrayList<Object> propertyValues = new ArrayList<Object>();

						propertyNames.add("replicas");
						propertyValues.add(allocation + solution.getReplicas(app.getTierById(tierId)));

						propertyNames.add("resourceName");
						propertyNames.add("speed");
						propertyNames.add("ram");
						propertyNames.add("numberOfCores");
						propertyValues.add(resourceName);
						propertyValues.add(speed);
						propertyValues.add(ram);
						propertyValues.add(numberOfCores);

						app.changeValues(tierId, propertyNames, propertyValues);

						app.setEvaluated(false);
					}
				}

				res = true;

				// for (Solution s : getAll()) {
				// System.out.println("DEBUG prima: " + s.getProvider());
				// for (int i = 0; i < 24; ++i)
				// System.out.printf("%d (%d) ", s.getApplication(i)
				// .getWorkload(), (int) (s
				// .getPercentageWorkload(i) * 100));
				// System.out.println();
				// }

			} catch (Exception e) {
				logger.error("Error while considering data from a solution file.", e);
				return false;
			}
		}

		return res;
	}

	/**
	 * Show status.
	 */
	public String showStatus() {
		String result = "SolutionMulti Status\n";

		result += "Total cost: " + costFormatter.format(getCost());
		result += "\tEvaluated: " + isEvaluated();
		result += "\tFeasible: " + isFeasible();
		result += "\tProviders: " + size();

		int i = 0;
		for (Solution s : getAll()) {
			result += "\n" + ++i + ")\n";
			result += s.showStatus();
		}
		return result;
	}

	public int size() {
		return solutions.size();
	}

	@Override
	public String toString() {
		String result = "SolutionMulti@" + Integer.toHexString(super.hashCode());
		result += "[Cost: " + costFormatter.format(getCost());
		result += ", Providers: " + size();
		result += ", Evaluated: " + isEvaluated();
		result += ", Feasible: " + isFeasible();
		result += "]";
		return result;
	}

	/**
	 * Updates the feasibility of the solution
	 */
	public void updateEvaluation() {
		boolean evaluated = true, feasible = true;
		// int previousCost = cost;
		cost = 0;

		for (Solution s : getAll()) {
			s.updateEvaluation();
			evaluated = evaluated && s.isEvaluated();
			feasible = feasible && s.isFeasible();
			cost += s.getCost();
		}

		this.evaluated = evaluated;
		this.feasible = feasible;

		// System.out.printf("DEBUG: Cost updated from %d to %d.\n",
		// previousCost, cost);
	}

	public boolean isUsingPrivateCloud() {
		for (String provider : solutions.keySet())
			if (provider.indexOf(PrivateCloud.BASE_PROVIDER_NAME) > -1)
				return true;
		return false;

	}

	public String showWorkloadPercentages() {
		String result = "SolutionMulti workload percentages";

		int size = Integer.MIN_VALUE;
		for (Solution s : getAll()) {
			if (size < s.getProvider().length())
				size = s.getProvider().length();
		}

		for (Solution s : getAll()) {
			result += "\n" + String.format("%1$" + size + "s", s.getProvider()) + ":";
			for (int h = 0; h < 24; ++h)
				result += " " + s.getPercentageWorkload(h);
		}
		return result;
	}

	public int getGenerationIteration() {
		return generationIteration;
	}

	public void setGenerationIteration(int generationIteration) {
		this.generationIteration = generationIteration;
	}

	public long getGenerationTime() {
		return generationTime;
	}

	public void setGenerationTime(long generationTime) {
		this.generationTime = generationTime;
	}

	public static List<String> getAllProviders(File solution) {
		List<String> res = new ArrayList<String>();

		if (isResourceModelExtension(solution))
			res = getAllProvidersFromResourceModelExtension(solution);
		else
			res = getAllProvidersFromFileSolution(solution);

		return res;
	}

	private static List<String> getAllProvidersFromResourceModelExtension(File solution) {
		List<String> res = new ArrayList<String>();

		if (solution != null && solution.exists())
			try {
				ResourceModelExtension rme = XMLHelper.deserialize(solution.toURI().toURL(),
						ResourceModelExtension.class);

				for (ResourceContainer rc : rme.getResourceContainer()) {
					String provider = rc.getProvider();

					boolean alreadyIn = false;
					for (int j = 0; j < res.size() && !alreadyIn; ++j) {
						if (res.get(j).equals(provider))
							alreadyIn = true;
					}
					if (!alreadyIn)
						res.add(provider);
				}
			} catch (Exception e) {
				logger.error("Error while reading data from a solution file.", e);
			}

		return res;

	}

	@Deprecated
	private static List<String> getAllProvidersFromFileSolution(File solution) {
		List<String> res = new ArrayList<String>();

		if (solution != null && solution.exists())
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(solution);
				doc.getDocumentElement().normalize();

				NodeList tiers = doc.getElementsByTagName("Tier");

				for (int i = 0; i < tiers.getLength(); ++i) {
					Node n = tiers.item(i);

					if (n.getNodeType() != Node.ELEMENT_NODE)
						continue;

					Element tier = (Element) n;
					String provider = tier.getAttribute("providerName");

					boolean alreadyIn = false;
					for (int j = 0; j < res.size() && !alreadyIn; ++j) {
						if (res.get(j).equals(provider))
							alreadyIn = true;
					}
					if (!alreadyIn)
						res.add(provider);
				}
			} catch (Exception e) {
				logger.error("Error while reading data from a solution file.", e);
			}

		return res;

	}

	public ResourceModelExtension getAsExtension() {
		// Build the objects
		ObjectFactory factory = new ObjectFactory();
		ResourceModelExtension extension = factory.createResourceModelExtension();
		List<ResourceContainer> resourceContainers = extension.getResourceContainer();

		for (Solution s : getAll()) {
			ResourceModelExtension ext = s.getAsExtension();
			List<ResourceContainer> rcs = ext.getResourceContainer();

			for (ResourceContainer rc : rcs)
				resourceContainers.add(rc);
		}

		return extension;
	}

	/**
	 * Export the solution in the format of the extension used as input for
	 * space4cloud
	 */
	public boolean exportAsExtension(Path fileName) {
		ResourceModelExtension extension = getAsExtension();
		final String schemaLocation = "http://www.modaclouds.eu/xsd/2013/6/resource-model-extension https://raw.githubusercontent.com/deib-polimi/modaclouds-qos-models/master/metamodels/s4cextension/resource_model_extension.xsd";
		// serialize them
		try {
			XMLHelper.serialize(extension, ResourceModelExtension.class, new FileOutputStream(fileName.toFile()),
					schemaLocation);
			return true;
		} catch (JAXBException e) {
			logger.error("The generated solution is not valid", e);
		} catch (FileNotFoundException e) {
			logger.error("Error exporting the solution", e);
		}
		return false;

	}

	public Costs getCostsAsExtension() {
		Costs costs = new Costs();

		costs.setSolutionID(hashCode() + "");

		CostType ct = new CostType();

		for (int h = 0; h < 24; ++h) {
			float cost = 0;
			for (Solution s : getAll())
				cost += s.getCost(h);

			HourPriceType hour = new HourPriceType();
			hour.setHour(h);
			hour.setCost(cost);

			ct.getHourPrice().add(hour);
		}

		ct.setTotalCost((float) cost);
		costs.setCost(ct);

		HashMap<String, Providers> providersMap = new HashMap<String, Providers>();

		for (Solution s : getAll()) {
			for (Tier t : s.getApplication(0).getTiers()) {
				String provider = s.getProvider();
				String serviceName = t.getCloudService().getServiceName();

				Providers p = providersMap.get(provider + "@" + serviceName);
				CostType ctp;

				if (p == null) {
					p = new Providers();
					p.setName(provider);
					p.setServiceName(serviceName);
					providersMap.put(provider + "@" + serviceName, p);

					costs.getProviders().add(p);

					ctp = new CostType();

					float totalCost = 0.0f;

					for (int h = 0; h < 24; ++h) {
						float cost = (float) s.getCost(t.getId(), h);
						totalCost += cost;

						HourPriceType hour = new HourPriceType();
						hour.setHour(h);
						hour.setCost(cost);
						ctp.getHourPrice().add(hour);
					}

					p.setCost(ctp);
					ctp.setTotalCost((float) totalCost);
				} else {
					ctp = p.getCost();

					float totalCost = ctp.getTotalCost();

					for (HourPriceType hour : ctp.getHourPrice()) {
						int h = hour.getHour();
						float cost = (float) s.getCost(t.getId(), h);
						totalCost += cost;
						hour.setCost(cost + hour.getCost());
					}

					ctp.setTotalCost((float) totalCost);
				}

			}

		}

		return costs;
	}

	public boolean exportCostsAsExtension(Path fileName) {
		Costs costs = getCostsAsExtension();
		// serialize them
		try {
			XMLHelper.serialize(costs, Costs.class, new FileOutputStream(fileName.toFile()));
			return true;
		} catch (JAXBException e) {
			logger.error("The generated solution is not valid", e);
		} catch (FileNotFoundException e) {
			logger.error("Error exporting the solution", e);
		}
		return false;
	}

	public File generateOptimizedCosts() {
		boolean doIt = false;
		for (Solution s : getAll()) {
			if (s.getProvider().equals("Amazon")) {
				doIt = true;
			}
		}

		if (!doIt)
			return null;

		logger.info("Exporting the optimized costs for Amazon...");

		double m = 1000.0; // TODO: boh?

		String configuration = null;
		try {
			configuration = Files.createTempFile("space4cloud", ".properties").toString();
		} catch (IOException e) {
			logger.error("Error creating a new temporary file", e);
			return null;
		}
		try {
			Configuration.saveConfiguration(configuration);
		} catch (IOException e) {
			logger.error("Error exporting the configuration", e);
			return null;
		}

		String solution = null;
		try {
			solution = Files.createTempFile("solution", ".xml").toString();
		} catch (IOException e) {
			logger.error("Error creating a new temporary file", e);
			return null;
		}
		exportAsExtension(Paths.get(solution));

		Contractor.removeTempFiles = true;

		try {
			File f = Contractor.perform(configuration, solution, m);
			if (f != null && f.exists()) {
				logger.info("Optimized costs: " + f.getAbsolutePath());
			}

			return f;
		} catch (Exception e) {
			logger.error("Error while using the contractor tool!", e);
			return null;
		}
	}

	public static Map<String, List<String>> getResourceSizesByTier(File solution) {
		Map<String, List<String>> res = new HashMap<String, List<String>>();

		if (isResourceModelExtension(solution))
			res = getResourceSizesByTierFromResourceModelExtension(solution);
		else
			res = getResourceSizesByTierFromFileSolution(solution);

		return res;
	}

	private static Map<String, List<String>> getResourceSizesByTierFromResourceModelExtension(File solution) {
		Map<String, List<String>> res = new HashMap<String, List<String>>();

		if (solution != null && solution.exists())
			try {
				ResourceModelExtension rme = XMLHelper.deserialize(solution.toURI().toURL(),
						ResourceModelExtension.class);

				for (ResourceContainer rc : rme.getResourceContainer()) {
					String resourceSize = rc.getCloudElement().getResourceSizeID();
					String tierId = rc.getId();

					List<String> values = res.get(tierId);
					if (values == null) {
						values = new ArrayList<String>();
						res.put(tierId, values);
					}

					res.get(tierId).add(resourceSize);
				}
			} catch (Exception e) {
				logger.error("Error while reading data from a solution file.", e);
			}

		return res;

	}

	@Deprecated
	private static Map<String, List<String>> getResourceSizesByTierFromFileSolution(File solution) {
		Map<String, List<String>> res = new HashMap<String, List<String>>();

		if (solution != null && solution.exists())
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(solution);
				doc.getDocumentElement().normalize();

				NodeList tiers = doc.getElementsByTagName("Tier");

				for (int i = 0; i < tiers.getLength(); ++i) {
					Node n = tiers.item(i);

					if (n.getNodeType() != Node.ELEMENT_NODE)
						continue;

					Element tier = (Element) n;

					String resourceSize = tier.getAttribute("resourceName");
					String tierId = tier.getAttribute("id");

					List<String> values = res.get(tierId);
					if (values == null) {
						values = new ArrayList<String>();
						res.put(tierId, values);
					}

					res.get(tierId).add(resourceSize);
				}
			} catch (Exception e) {
				logger.error("Error while reading data from a solution file.", e);
			}

		return res;

	}

	@Override
	public Iterator<Solution> iterator() {
		return solutions.values().iterator();
	}

}
