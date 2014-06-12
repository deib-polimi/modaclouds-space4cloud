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


	public static String getSupportedMetricNames() {
		String value="";
		for(Metric m:Metric.values()){
			value 	+= m.getXmlTag()+" ";
		}
		return value;
	}
}

