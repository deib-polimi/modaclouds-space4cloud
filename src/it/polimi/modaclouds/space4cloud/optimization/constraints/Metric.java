package it.polimi.modaclouds.space4cloud.optimization.constraints;

public enum Metric {
	REPLICATION("Replication"), 
	RAM("RAM"), 
	HDD("HardDisk"), 
	CORES("Cores"), 
	CPU("CPUUtilization"),
	MACHINETYPE("MachineType"), 
	SERVICETYPE ("ServiceType"),
	RESPONSETIME("ResponseTime"), 
	AVAILABILITY("Availability"),	
	RELIABILITY("Reliability");
	
	private String xmlTag; //the tag of the type attribute in the xml file


	private Metric(String xmlTag){
		this.xmlTag = xmlTag;
	}

	
	public String getXmlTag() {
		return xmlTag;
	}
	
	public static Metric getMetricFromTag(String tag){
		switch (tag) {
		case "Replication":
			return REPLICATION;
		case "RAM":
			return RAM;
		case "HardDisk":
			return HDD;
		case "Cores":
			return CORES;
		case "CPUUtilization":
			return CPU;
		case "MachineType":
			return MACHINETYPE;
		case "ServiceType":
			return SERVICETYPE;
		case "ResponseTime":
			return RESPONSETIME;
		case "Availability":
			return AVAILABILITY;
		case "Reliability":
			return RELIABILITY;
		default:
			return null;
		}
	}
}

