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
package it.polimi.modaclouds.space4cloud.chart;

public class SeriesHandle {

	private final int position;
	private final Logger2JFreeChartImage logger;

	public SeriesHandle(int pos, Logger2JFreeChartImage logger) {
		this.position = pos;
		this.logger = logger;
	}

	/**
	 * @return the logger
	 */
	public Logger2JFreeChartImage getLogger() {
		return logger;
	}

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

}
