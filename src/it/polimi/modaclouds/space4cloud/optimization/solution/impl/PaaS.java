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
package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.optimization.solution.IResponseTimeConstrainable;

public abstract class PaaS extends CloudService implements
		IResponseTimeConstrainable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8088032094520247525L;

	public PaaS(String provider, String serviceType,
			String serviceName, String resourceName, int replicas) {
		super(provider, serviceType, serviceName, resourceName);
		
		this.replicas = replicas;
	}
	
	private int replicas;

	public int getReplicas() {
		return replicas;
	}

	public void setReplicas(int replicas) {
		this.replicas = replicas;
	}

	public static enum PaaSType {
		Frontend("frontend"), Middleware("middleware"), Backend("backend"), DataBase("database"), Storage("storage"), Queue("queue"), Cache("cache");
		
		private String name;
		
		private PaaSType(String name) {
			this.name = name;
		}
		
		public static PaaSType getById(int id) {
			PaaSType[] values = PaaSType.values();
			if (id < 0)
				id = 0;
			else if (id >= values.length)
				id = values.length - 1;
			return values[id];
		}
		
		public static PaaSType getByName(String name) {
			PaaSType[] values = PaaSType.values();
			for (PaaSType pt : values) {
				if (pt.name.equals(name))
					return pt;
			}
			return null;
		}

		public static int size() {
			return PaaSType.values().length;			
		}
	}

}
