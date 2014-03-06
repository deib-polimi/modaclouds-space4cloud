/*
 * 
 */
package it.polimi.modaclouds.space4cloud.types.palladio;

import it.polimi.modaclouds.space4cloud.types.LinkT;
import it.polimi.modaclouds.space4cloud.utils.DOM;
import it.polimi.modaclouds.space4cloud.utils.IdHandler;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

// TODO: Auto-generated Javadoc
/**
 * The Class LinkingResource.
 */
public class LinkingResource {

	/** The linking resource element. */
	private Element linkingResourceElement;
	
	/** The name. */
	private String name;
	
	/** The id. */
	private String id;
	
	/** The latency. */
	private double latency;
	
	/** The failure probability. */
	private double failureProbability;
	
	/** The throughput. */
	private int throughput;
	
	/** The type. */
	private LinkT type;
	
	/** The rc2. */
	private ArrayList<String> connectedElements=new ArrayList<String>();

	/**
	 * Instantiates a new linking resource.
	 *
	 * @param e the e
	 */
	public LinkingResource(Element e) {
		initialize(e);
	}

	/**
	 * Instantiates a new linking resource.
	 *
	 * @param name the name
	 * @param type the type
	 * @param throughput the throughput
	 * @param latency the latency
	 * @param failureProbability the failure probability
	 * @param rcA the rc a
	 * @param rcB the rc b
	 */
	public LinkingResource(String name, LinkT type, int throughput,
			double latency, double failureProbability, ArrayList<ResourceContainer> rcl) {
		try {
			Document doc = DOM.getDocument();
			Element el = doc
					.createElement("linkingResources__ResourceEnvironment");
			el.setAttribute("entityName", name);
			el.setAttribute("id", IdHandler.getId(el));
			
			String tmp = "";
			for(ResourceContainer container:rcl){
				tmp=tmp+container.getId()+" ";
			}
			el.setAttribute("connectedResourceContainers_LinkingResource",tmp);
			Element x = doc
					.createElement("communicationLinkResourceSpecifications_LinkingResource");
			x.setAttribute("id", IdHandler.getId(x));
			x.setAttribute("failureProbability", "" + failureProbability);
			Element y = doc
					.createElement("communicationLinkResourceType_CommunicationLinkResourceSpecification");
			y.setAttribute("href", type.getPathmap());
			x.appendChild(y);
			y = doc.createElement("latency_CommunicationLinkResourceSpecification");
			y.setAttribute("specification", "" + latency);
			x.appendChild(y);
			y = doc.createElement("throughput_CommunicationLinkResourceSpecification");
			y.setAttribute("specification", "" + throughput);
			x.appendChild(y);
			el.appendChild(x);
			initialize(el);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Initialize.
	 *
	 * @param e the e
	 */
	private void initialize(Element e) {
		linkingResourceElement = e;
		id = e.getAttribute("id");
		name = e.getAttribute("entityName");
		String[] tokens = e.getAttribute(
				"connectedResourceContainers_LinkingResource").split(" ");
		for(String s:tokens){
			connectedElements.add(s);
		}
		
		try{
		failureProbability = Double
				.parseDouble(((Element) e
						.getElementsByTagName(
								"communicationLinkResourceSpecifications_LinkingResource")
						.item(0)).getAttribute("failureProbability"));
		}catch (Exception exc) {
			failureProbability = 0.0;
		}
		latency = Double.parseDouble(((Element) e.getElementsByTagName(
				"latency_CommunicationLinkResourceSpecification").item(0))
				.getAttribute("specification"));
		throughput = Integer.parseInt(((Element) e.getElementsByTagName(
				"throughput_CommunicationLinkResourceSpecification").item(0))
				.getAttribute("specification"));
		String str = ((Element) e
				.getElementsByTagName(
						"communicationLinkResourceType_CommunicationLinkResourceSpecification")
				.item(0)).getAttribute("href");
		if (str.equals(LinkT.LAN.getPathmap()))
			type = LinkT.LAN;
	}

	/**
	 * Gets the linking resource element.
	 *
	 * @return the linking resource element
	 */
	public Element getLinkingResourceElement() {
		return linkingResourceElement;
	}

	/**
	 * Sets the linking resource element.
	 *
	 * @param linkingResourceElement the new linking resource element
	 */
	public void setLinkingResourceElement(Element linkingResourceElement) {
		initialize(linkingResourceElement);
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
		linkingResourceElement.setAttribute("entityName", name);
	}

	/**
	 * Gets the latency.
	 *
	 * @return the latency
	 */
	public double getLatency() {
		return latency;
	}

	/**
	 * Sets the latency.
	 *
	 * @param latency the new latency
	 */
	public void setLatency(double latency) {
		this.latency = latency;
		Element oldC = (Element) linkingResourceElement.getElementsByTagName(
				"latency_CommunicationLinkResourceSpecification").item(0);
		oldC.setAttribute("specification", "" + latency);
	}

	/**
	 * Gets the throughput.
	 *
	 * @return the throughput
	 */
	public int getThroughput() {
		return throughput;
	}

	/**
	 * Sets the throughput.
	 *
	 * @param throughput the new throughput
	 */
	public void setThroughput(int throughput) {
		this.throughput = throughput;
		Element oldC = (Element) linkingResourceElement.getElementsByTagName(
				"throughput_CommunicationLinkResourceSpecification").item(0);
		oldC.setAttribute("specification", "" + throughput);
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public LinkT getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(LinkT type) {
		this.type = type;
		Element oldC = (Element) linkingResourceElement
				.getElementsByTagName(
						"communicationLinkResourceType_CommunicationLinkResourceSpecification")
				.item(0);
		oldC.setAttribute("href", type.getPathmap());
	}

	/**
	 * Gets the rc1.
	 *
	 * @return the rc1
	 */
	public ArrayList<String> getConnectedElements() {
		return connectedElements;
	}

	/**
	 * Sets the rc1.
	 *
	 * @param rc1 the new rc1
	 */
	public void addConnectedElement(String elementID) {
		connectedElements.add(id);
		generateArttribute();
	}


	private void generateArttribute() {
		String temp = "";
		for(String s:connectedElements)
			temp = temp + s+" ";
		//TODO: check here, may be necessary to remove the last space.  
		linkingResourceElement.setAttribute(
				"connectedResourceContainers_LinkingResource", temp);
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the failure probability.
	 *
	 * @return the failure probability
	 */
	public double getFailureProbability() {
		return failureProbability;
	}

	/**
	 * Sets the failure probability.
	 *
	 * @param failureProbability the new failure probability
	 */
	public void setFailureProbability(double failureProbability) {
		this.failureProbability = failureProbability;
		((Element) linkingResourceElement.getElementsByTagName(
				"communicationLinkResourceSpecifications_LinkingResource")
				.item(0)).setAttribute("failureProbability", ""
				+ failureProbability);

	}

	public void substitueConnection(String id1, String id2) {
		int index = connectedElements.indexOf(id1);
		connectedElements.remove(index);
		connectedElements.add(index, id2);	
		generateArttribute();
	}

}
