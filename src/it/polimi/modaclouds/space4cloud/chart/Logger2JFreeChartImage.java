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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * @author Michele Ciavotta
 * The target of this class is to create a pseudo-logger able to create an image file using JfreeChart
 *
 */
public class Logger2JFreeChartImage {

	private String chartTitle = "";
	private String path2save = "";

	private JFreeChart chart;
	private int width = 500;
	private int height = 300;

	final ChartCreator chartCreator= new ChartCreator("");
	private List<XYSeries> seriesList = new ArrayList<XYSeries>();

	public String getChartTitle() {
		return chartTitle;
	}

	public void setChartTitle(String chartTitle) {
		this.chartTitle = chartTitle;
	}

	public Logger2JFreeChartImage() {
		initByProperties("conf.properties");
	}
	public Logger2JFreeChartImage(String propertiesFileName) {
		initByProperties(propertiesFileName);
	}


	/**
	 * @throws NumberFormatException
	 */
	private void initByProperties(String propertiesFileName) throws NumberFormatException {
		try {
			InputStream fileInput = this.getClass().getResourceAsStream(propertiesFileName);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();
			chartTitle = properties.getProperty("ImageTitle");
			path2save = properties.getProperty("path2save");
			width = Integer.parseInt( properties.getProperty("ImageWidth"));
			height = Integer.parseInt(properties.getProperty("ImageHeight"));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SeriesHandle newSeries(String seriesTitle){

		final XYSeries series = new XYSeries(seriesTitle);

		if(seriesList.add(series)){		
			final SeriesHandle h = new SeriesHandle(seriesList.size()-1, this);
			series.add(0.0,0.0);
			return h;
		}
		else
			return null;


	}

	public void addPoint2Series(SeriesHandle h, double x, double y){
		if(h != null){
			XYSeries series =seriesList.get(h.getPosition());
			series.add(x, y);
		}
	}

	/**
	 * @return the path2save
	 */
	public String getPath2save() {
		return path2save;
	}

	/**
	 * @param path2save the path2save to set
	 */
	public void setPath2save(String path2save) {
		this.path2save = path2save;
	}

	public BufferedImage save2buffer(Dimension dim){

		XYSeriesCollection dataSet = new XYSeriesCollection();		
		chartCreator.removeAllSeries();
		for (XYSeries s : seriesList) {
			dataSet.addSeries(s);
		} 

		chart = ChartFactory.createXYLineChart(chartTitle, "Iterations", "Y", dataSet, PlotOrientation.VERTICAL, true, true, false);
		if(dim == null)
			dim = new Dimension(100,100);

		return chart.createBufferedImage((int)(dim.getWidth()*0.9), (int)(dim.getHeight()*0.9));

	}

	public void save2png(){

		chartCreator.setCharTitle(chartTitle);
		chartCreator.removeAllSeries();
		for (XYSeries s : seriesList) 
			chartCreator.addSeries(s);
		try {
			ChartUtilities.saveChartAsPNG(new File(path2save), chartCreator.getChart(), width, height);
		} catch (Exception e) {
			System.out.println("Problem occurred creating chart.");
		}

	}


	public static void main(final String[] args) {

		//final String charTitle = "XY Series Demo2";

		//final ChartCreator chartCreator = new ChartCreator(charTitle);


		Logger2JFreeChartImage log = new Logger2JFreeChartImage();
		SeriesHandle h = log.newSeries("new series");

		log.addPoint2Series(h, 1.0, 500.2);  
		log.addPoint2Series(h, 5.0, 694.1);
		log.addPoint2Series(h,4.0, 100.0);
		log.addPoint2Series(h,12.5, 734.4);
		log.addPoint2Series(h,17.3, 453.2);
		log.addPoint2Series(h,21.2, 500.2);
		//log.addPoint2Series(h,21.9, null);
		log.addPoint2Series(h,25.6, 734.4);
		log.addPoint2Series(h, 30.0, 453.2);
		log.save2png();
		//        
		//        chartCreator.addSeries(series);
		//        
		//        final String seriesTitle2 = "Random Data2";
		//    	final XYSeries series2 = new XYSeries(seriesTitle2);
		//        series2.add(1.0, 200.2);
		//        series2.add(5.0, 394.1);
		//        series2.add(4.0, 500.0);
		//        series2.add(12.5, 134.4);
		//        series2.add(17.3, 553.2);
		//        series2.add(21.2, 200.2);
		//        series2.add(25.6, 234.4);
		//        series2.add(30.0, 753.2);
		//        
		//        chartCreator.addSeries(series2);
		//        
		//        JFreeChart chart1 = chartCreator.getChart();
		//        final XYSeriesFrame demo = new XYSeriesFrame("XY Series Demo", charTitle, chart1);
		//                
		//        demo.pack();
		//        RefineryUtilities.centerFrameOnScreen(demo);
		//        demo.setVisible(true);

	}
}


