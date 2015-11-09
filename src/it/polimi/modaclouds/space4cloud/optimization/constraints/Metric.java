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
	REPLICATION("Replication"), RAM("RAM"), HDD("HardDisk"), CORES("Cores"), CPU(
			"CPUUtilization"), MACHINETYPE("MachineType"), SERVICETYPE(
			"ServiceType"), RESPONSETIME("ResponseTime"), AVAILABILITY(
			"Availability"), RELIABILITY("Reliability"), WORKLOADPERCENTAGE("WorkloadPercentage"),
			PROGRAMMINGLANGUAGE("ProgrammingLanguage"), DBTYPE("DBType"), DBTECHNOLOGY("DBTechnology"),
			NUMBERPROVIDERS("NumberProviders");

	public static Metric getMetricFromTag(String tag) {
		switch (tag) {
		case "Replication":
			return REPLICATION;
		case "RAM":
			return RAM;
		//TODO:not supported
		case"HardDisk":
			return HDD;
		//TODO:not supported
		case "Cores":
			return CORES;
		case "CPUUtilization":
			return CPU;
		case "CPU":
			return CPU;
		//TODO:not supported
		case "MachineType":
			return MACHINETYPE;
		//TODO:not supported
		case "ServiceType":
			return SERVICETYPE;
		case "ResponseTime":
			return RESPONSETIME;
		//TODO:not supported
		case "Availability":
			return AVAILABILITY;
		//TODO:not supported
		case "Reliability":
			return RELIABILITY;
		case "WorkloadPercentage":
			return WORKLOADPERCENTAGE;
		case "ProgrammingLanguage":
			return PROGRAMMINGLANGUAGE;
		case "DBType":
			return DBTYPE;
		case "DBTechnology":
			return DBTECHNOLOGY;
		case "NumberProviders":
			return NUMBERPROVIDERS;
		default:
			return null;
		}
	}

	private String xmlTag; // the tag of the type attribute in the xml file

	private Metric(String xmlTag) {
		this.xmlTag = xmlTag;
	}

	public String getXmlTag() {
		return xmlTag;
	}
	
	public static String getSupportedMetricNames() {
		String value="";
		for (Metric m : Metric.values()) {
			value 	+= m.getXmlTag()+" ";
		}
		return value;
	}
}
