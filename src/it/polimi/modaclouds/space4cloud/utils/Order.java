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
	 * Order.
	 * 
	 * @param list
	 *            the list
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
				res[index] = list[i];
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Instantiates a new order.
	 */
	private Order() {
	}
}
