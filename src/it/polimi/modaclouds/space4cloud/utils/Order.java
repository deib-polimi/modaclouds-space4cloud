/*
 * 
 */
package it.polimi.modaclouds.space4cloud.utils;

import java.io.File;

// TODO: Auto-generated Javadoc
/**
 * The Class Order.
 */
public class Order {

	/**
	 * Instantiates a new order.
	 */
	private Order() {
	}

	/**
	 * Order.
	 *
	 * @param list the list
	 * @return the file[]
	 */
	public static File[] order(File[] list) {
		if (list == null)
			return null;
		if (list.length != 24)
			return null;
		File[] res = new File[24];
		try {
			for (int i = 0; i < 24; i++) {
				String name = list[i].getName();
				int index1 = name.indexOf("_");
				int index2 = name.indexOf(".");
				int index = Integer
						.parseInt(name.substring(index1 + 1, index2));
				res[index]=list[i];
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
