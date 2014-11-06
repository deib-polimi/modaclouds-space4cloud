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

import it.polimi.modaclouds.space4cloud.utils.Configuration;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michele Ciavotta The target of this class is to create a
 *         pseudo-logger able to create an image file using JfreeChart
 * 
 */
public class Logger2JFreeChartImage {

	private String chartTitle = "";	


	private String path2save = "";
	private JFreeChart chart;
	private int width = 500;

	private int height = 300;
	final ChartCreator chartCreator = new ChartCreator("");

	private static final Logger logger = LoggerFactory.getLogger(Logger2JFreeChartImage.class);

	private static final int MAX_SHOW_SIZE = 500;

	private List<XYSeries> seriesList = new ArrayList<XYSeries>();

	public Logger2JFreeChartImage() throws NumberFormatException, IOException {
		initByProperties("conf.properties");
	}

	public Logger2JFreeChartImage(String propertiesFileName)
			throws NumberFormatException, IOException {
		initByProperties(propertiesFileName);
	}

	public void addPoint2Series(SeriesHandle h, double x, double y) {
		if (h != null) {
			XYSeries series = seriesList.get(h.getPosition());
			//if any series passes the maximum value clear all of them
			if(series.getItemCount()>MAX_SHOW_SIZE){
				for(XYSeries s:seriesList)
					s.clear();
			}
			series.add(x, y);
		}
	}

	public String getChartTitle() {
		return chartTitle;
	}

	/**
	 * @return the path2save
	 */
	public String getPath2save() {
		return path2save;
	}

	/**
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private void initByProperties(String propertiesFileName)
			throws NumberFormatException, IOException {

		InputStream fileInput = this.getClass().getResourceAsStream(
				propertiesFileName);
		Properties properties = new Properties();
		properties.load(fileInput);
		fileInput.close();
		chartTitle = properties.getProperty("ImageTitle");
		path2save = properties.getProperty("path2save");
		width = Integer.parseInt(properties.getProperty("ImageWidth"));
		height = Integer.parseInt(properties.getProperty("ImageHeight"));

	}

	public SeriesHandle newSeries(String seriesTitle) {

		final XYSeries series = new XYSeries(seriesTitle);

		if (seriesList.add(series)) {
			final SeriesHandle h = new SeriesHandle(seriesList.size() - 1, this);
			series.add(0.0, 0.0);
			return h;
		} else
			return null;

	}


	@SuppressWarnings("deprecation")
	public BufferedImage save2buffer(Dimension dim) {

		XYSeriesCollection dataSet = new XYSeriesCollection();
		chartCreator.removeAllSeries();
		for (XYSeries s : seriesList) {
			dataSet.addSeries(s);
		}

		chart = ChartFactory.createXYLineChart(chartTitle, "Iterations", "Y",
				dataSet, PlotOrientation.VERTICAL, true, true, false);
		if (dim == null)
			dim = new Dimension(100, 100);
		if(dim.getHeight()<=0)
			dim.height=1;
		if(dim.getWidth()<=0)
			dim.width=1;
		
		
		////////////////////////
		{
			Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

			BasicStroke stroke = new BasicStroke(2.0f,                     // Line width
												BasicStroke.CAP_ROUND,     // End-cap style
												BasicStroke.JOIN_ROUND);   // Vertex join style
			
			XYPlot plot = (XYPlot) chart.getPlot();
			
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot
					.getRenderer();
			renderer.setStroke(stroke);

			ValueAxis valueAxis = plot.getDomainAxis();
			valueAxis.setLowerMargin(0.02);
			valueAxis.setUpperMargin(0.02);
			valueAxis.setTickLabelFont(font);

			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setTickLabelFont(font);
		}
		////////////////////////
		


		return chart.createBufferedImage((int) (dim.getWidth() * 0.9),
				(int) (dim.getHeight() * 0.9));

	}

	public void save2png() throws IOException {

		chartCreator.setCharTitle(chartTitle);
		chartCreator.removeAllSeries();
		for (XYSeries s : seriesList)
			chartCreator.addSeries(s);
		try {

			// Path path = Paths.get(path2save);
			Path path = Paths.get(
					Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,
					path2save);
			if (Files.notExists(path.getParent()))
				Files.createDirectory(path.getParent());
			// Files.deleteIfExists(path);

			ChartUtilities.saveChartAsPNG(path.toFile(), // new File(path2save),
					chartCreator.getChart(), width, height);
		} catch (IOException e) {
			logger.error("Could not create cost image", e);
			throw e;
		}

	}


	/**
	 * @param path2save
	 *            the path2save to set
	 */
	public void setPath2save(String path2save) {
		this.path2save = path2save;
	}
}
