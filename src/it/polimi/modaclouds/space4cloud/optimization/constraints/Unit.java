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
