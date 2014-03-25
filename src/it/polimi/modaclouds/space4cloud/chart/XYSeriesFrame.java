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

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

public class XYSeriesFrame extends ApplicationFrame {

	private static final long serialVersionUID = 1L;

	private JFreeChart chart;

	private ChartPanel chartPanel;

	/**
	 * @param frameTitle
	 * @param charTitle
	 * @param chart
	 */
	public XYSeriesFrame(final String frameTitle, final String charTitle,
			final JFreeChart chart) {

		super(frameTitle);

		this.chart = chart;

		chartPanel = new ChartPanel(this.chart);

		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);

	}

}
