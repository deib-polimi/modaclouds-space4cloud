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
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		IdHandler idh = new IdHandler();
		System.out.println(idh.hashCode());
		System.out.println(IdHandler.getId(idh));
	}

	/**
	 * Instantiates a new id handler.
	 */
	private IdHandler() {
	}
}
