/*
 * 
 */
package it.polimi.modaclouds.space4cloud.utils;

import java.util.Random;

// TODO: Auto-generated Javadoc
/**
 * This class provides utilities to create new id for Palladio components.
 * 
 * @author Davide Franceschelli
 * 
 */
public class IdHandler {

	/**
	 * Instantiates a new id handler.
	 */
	private IdHandler() {
	}

	/**
	 * Returns a string representing the id of the object provided as parameter.
	 * The id is a string composed of 23 alpha-numeric characters.
	 * 
	 * @param o
	 *            is the input Object.
	 * @return a String representing the id of the input Object.
	 */
	public static String getId(Object o) {
		String out = "";
		String map = "0123456789aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ";
		Random r = new Random(o.hashCode());
		for (int i = 0; i < 23; i++)
			out += map.charAt(r.nextInt(62));
		return out;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		IdHandler idh = new IdHandler();
		System.out.println(idh.hashCode());
		System.out.println(IdHandler.getId(idh));
	}
}
