package it.polimi.modaclouds.space4cloud.utils;

public class Rounder {
	
	public static final String precision = "%.2f";
	
	public static double round(double value){
		return Double.parseDouble(String.format(precision, value));
	}
	
	public static float round(float value){
		return Float.parseFloat(String.format(precision, value));
	}

}
