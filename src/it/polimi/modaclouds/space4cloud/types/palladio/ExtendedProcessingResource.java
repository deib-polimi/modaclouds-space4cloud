/*
 * 
 */
package it.polimi.modaclouds.space4cloud.types.palladio;

import it.polimi.modaclouds.space4cloud.types.ProcessingResourceT;
import it.polimi.modaclouds.space4cloud.types.SchedulingT;

import java.io.File;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * Represents a Palladio Processing Resource extended with an Efficiency
 * Profile.
 * 
 * @author Davide Franceschelli
 * @see ProcessingResource
 * @see EfficiencyProfile
 */
public class ExtendedProcessingResource extends ProcessingResource {

	/** The efficiency profile. */
	private EfficiencyProfile efficiencyProfile;

	/**
	 * Creates a new Extended Processing Resource starting from a Processing
	 * Resource Model and an Efficiency Profile.
	 * 
	 * @param e
	 *            is the Element representing the Processing Resource Model.
	 * @param ep
	 *            is the Efficiency Profile.
	 * @see EfficiencyProfile
	 * @see ProcessingResource
	 */
	public ExtendedProcessingResource(Element e, EfficiencyProfile ep) {
		super(e);
		if (ep != null)
			setEfficiencyProfile(ep);
	}

	/**
	 * Creates a new Extended Processing Resource.
	 * 
	 * @param type
	 *            is the Processing Resource Type.
	 * @param schedulingPolicy
	 *            is the Scheduling Policy Type.
	 * @param processingRate
	 *            is the processing rate.
	 * @param numberOfReplicas
	 *            is the number of processing cores.
	 * @param MTTF
	 *            is the Mean Time To Failure.
	 * @param MTTR
	 *            is the Mean Time To Repair.
	 * @param ep
	 *            is the Efficiency Profile.
	 * @see ProcessingResourceT
	 * @see SchedulingT
	 * @see EfficiencyProfile
	 */
	public ExtendedProcessingResource(ProcessingResourceT type,
			SchedulingT schedulingPolicy, double processingRate,
			int numberOfReplicas, double MTTF, double MTTR, EfficiencyProfile ep) {
		super(type, schedulingPolicy, processingRate, numberOfReplicas, MTTF,
				MTTR);
		if (ep != null)
			setEfficiencyProfile(ep);
	}

	/**
	 * Creates a new Extended Processing Resource without the Efficiency
	 * Profile, starting from an existing Processing Resource.
	 * 
	 * @param pr
	 *            is the input Processing Resource.
	 * @see ProcessingResource
	 */
	public ExtendedProcessingResource(ProcessingResource pr) {
		super(pr.getProcessingResourceElement());
	}

	/**
	 * Creates a new Extended Processing Resource, starting from an existing
	 * Processing Resource, with the specified Efficiency Profile.
	 * 
	 * @param pr
	 *            is the input Processing Resource.
	 * @param ep
	 *            is the input Efficiency Profile.
	 * @see ProcessingResource
	 * @see EfficiencyProfile
	 */
	public ExtendedProcessingResource(ProcessingResource pr,
			EfficiencyProfile ep) {
		super(pr.getProcessingResourceElement());
		if (ep != null)
			setEfficiencyProfile(ep);
	}

	/**
	 * Retrieves the Efficiency Profile.
	 * 
	 * @return the Efficiency Profile associated to the Extended Processing
	 *         Resource.
	 * @see EfficiencyProfile
	 */
	public EfficiencyProfile getEfficiencyProfile() {
		return efficiencyProfile;
	}

	/**
	 * Sets the Efficiency Profile of the Extended Processing Resource. Only an
	 * Efficiency Profile is allowed. The existing Efficiency Profile is
	 * replaced with the new one.
	 * 
	 * @param efficiencyProfile
	 *            is the Efficiency Profile to set.
	 * @see EfficiencyProfile
	 */
	public void setEfficiencyProfile(EfficiencyProfile efficiencyProfile) {
		this.efficiencyProfile = efficiencyProfile;
		Element pr = getProcessingResourceElement();
		NodeList nl = pr.getElementsByTagName("Efficiency_Profile");
		if (nl != null)
			for (int i = 0; i < nl.getLength(); i++)
				pr.removeChild(nl.item(i));
		Element x = (Element) getDocument().importNode(
				efficiencyProfile.getEfficiencyProfileElement(), true);
		pr.appendChild(x);
		setProcessingResourceElement(pr);
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		ProcessingResource pr = new ProcessingResource(ProcessingResourceT.CPU,
				SchedulingT.PS, 1000, 4, 10000, 200);
		EfficiencyProfile ep = new EfficiencyProfile();
		ExtendedProcessingResource extpr = new ExtendedProcessingResource(pr,
				ep);
		ResourceContainer rc = new ResourceContainer("AppServer");
		rc.addProcessingResource(extpr);
		ResourceEnvironment renv = new ResourceEnvironment();
		renv.addResourceContainer(rc);
		renv.serialize(new File(System.getProperty("user.dir")
				+ "\\Palladio\\newRE.xml"));
		ExtendedProcessingResource extpr1 = new ExtendedProcessingResource(
				pr.getProcessingResourceElement(), ep);
		rc = new ResourceContainer("AppServer");
		rc.addProcessingResource(extpr1);
		renv = new ResourceEnvironment();
		renv.addResourceContainer(rc);
		renv.serialize(new File(System.getProperty("user.dir")
				+ "\\Palladio\\newRE1.xml"));
		ExtendedProcessingResource extpr2 = new ExtendedProcessingResource(
				ProcessingResourceT.CPU, SchedulingT.PS, 1000, 4, 500000, 200,
				ep);
		rc = new ResourceContainer("AppServer");
		rc.addProcessingResource(extpr2);
		renv = new ResourceEnvironment();
		renv.addResourceContainer(rc);
		renv.serialize(new File(System.getProperty("user.dir")
				+ "\\Palladio\\newRE2.xml"));
	}
}
