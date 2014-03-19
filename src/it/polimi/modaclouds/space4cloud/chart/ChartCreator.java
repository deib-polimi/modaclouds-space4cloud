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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ChartCreator {

	private String charTitle;
	public String getCharTitle() {
		return charTitle;
	}

	public void setCharTitle(String charTitle) {
		this.charTitle = charTitle;
	}

	private XYSeriesCollection seriesCollection = new XYSeriesCollection();
	
	public JFreeChart getChart() {
		
		return ChartFactory.createXYLineChart(
                charTitle,
                "X", 
                "Y", 
                seriesCollection,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            );
	}

	public ChartCreator(String charTitle) {

		this.charTitle = charTitle;
		
	}
	
	public void addSeries(XYSeries s){
		seriesCollection.addSeries(s);
	}

	public void removeAllSeries(){
		seriesCollection.removeAllSeries();
	}

}
