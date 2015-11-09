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
package it.polimi.modaclouds.space4cloud.lqn;

import java.util.HashMap;
import java.util.Map;

public abstract class LqnResultParser {

	static Map<String,String> idSubstitutionMap = new HashMap<String,String>();
	
	public static Map<String, String> getIdSubstitutionMap() {
		return idSubstitutionMap;
	}

	public static void setIdSubstitutionMap(Map<String, String> idSubstitutionMap) {
		LqnResultParser.idSubstitutionMap = idSubstitutionMap;
	}

	public abstract double getResponseTime(String resourceID);

	public abstract Map<String, Double> getResponseTimes();

	public abstract double getUtilization(String resourceID);

	public abstract Map<String, Double> getUtilizations();

}
