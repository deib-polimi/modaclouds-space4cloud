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
/**
 * 
 */
package it.polimi.modaclouds.space4cloud.optimization.constraints;

/**
 * @author Michele Ciavotta
 * Possible units for a metric
 *
 */
public enum Unit {
	MILLISECONDS ("ms"),
	SECONDS("s"),
	PERCENT("%"),
	UNITS ("units"),
	MEGABYTES("MB"),
	GIGABYTES ("GB"),
	STRING("string");
	
	public static final int GB2MB = 1024; 
	public static final int MB2GB = 1/GB2MB; 
	
	private String xmlTag; //the tag of the type attribute in the xml file

	private Unit(String xmlTag){
		this.xmlTag = xmlTag;
	}
	
	public String getXmlTag() {
		return xmlTag;
	}

public static Unit getUnitFromTag(String tag){
		switch (tag) {
		case "ms":
			return MILLISECONDS;
		case "s":
			return SECONDS;
		case "%":
			return PERCENT;
		case "units":
			return UNITS;
		case "MB":
			return MEGABYTES;
		case "GB":
			return GIGABYTES;
		case "string":
			return STRING;
		default:
			return null;
		}
	}
	
	
	
	}
