package it.polimi.modaclouds.space4cloud.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Rounder {
	
	public static final String precision = "%.2f";
	
	public static double round(double value){
		return Double.parseDouble(doubleFormatter.format(value));
	}
	
	public static float round(float value){
		return Float.parseFloat(doubleFormatter.format(value));
	}
	
	private static DecimalFormat doubleFormatter = doubleFormatter();
	
	// When not using this, the conversion is locale-dependent: that is, it works on
	// US/UK/etc. locales where the decimal separator is '.', but it fails for example
	// with the italian locale where the decimal separator is ','.
	private static DecimalFormat doubleFormatter() {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
		otherSymbols.setDecimalSeparator('.');
		DecimalFormat myFormatter = new DecimalFormat(String.format(precision, 0.0f), otherSymbols);
		return myFormatter;
	}

}
