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
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class XYSeriesFrame  extends ApplicationFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JFreeChart chart ;
	
	private ChartPanel chartPanel;
	

	/**
     * A demonstration application showing an XY series containing a null value.
     *
     * @param frameTitle  the frame title.
     */
    /**
     * @param frameTitle
     * @param charTitle
     * @param chart
     */
    public XYSeriesFrame(final String frameTitle, final String charTitle, final JFreeChart chart) {

        super(frameTitle);
        
        this.chart = chart;

        
        chartPanel = new ChartPanel(this.chart);

        
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);

    }

    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(final String[] args) {

    	final String charTitle = "XY Series Demo2";
    	
    	final ChartCreator chartCreator = new ChartCreator(charTitle);
    	
    	final String seriesTitle1 = "Random Data1";
    	final XYSeries series = new XYSeries(seriesTitle1);
        series.add(1.0, 500.2);
        series.add(5.0, 694.1);
        series.add(4.0, 100.0);
        series.add(12.5, 734.4);
        series.add(17.3, 453.2);
        series.add(21.2, 500.2);
        series.add(21.9, null);
        series.add(25.6, 734.4);
        series.add(30.0, 453.2);
        
        chartCreator.addSeries(series);
        
        final String seriesTitle2 = "Random Data2";
    	final XYSeries series2 = new XYSeries(seriesTitle2);
        series2.add(1.0, 200.2);
        series2.add(5.0, 394.1);
        series2.add(4.0, 500.0);
        series2.add(12.5, 134.4);
        series2.add(17.3, 553.2);
        series2.add(21.2, 200.2);
        series2.add(25.6, 234.4);
        series2.add(30.0, 753.2);
        
        chartCreator.addSeries(series2);
        
        JFreeChart chart1 = chartCreator.getChart();
        final XYSeriesFrame demo = new XYSeriesFrame("XY Series Demo", charTitle, chart1);
                
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

}


