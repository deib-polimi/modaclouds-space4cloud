package it.polimi.modaclouds.space4cloud.chart;

import it.polimi.modaclouds.space4cloud.utils.Configuration;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

public class GenericChart<E> extends JPanel {
	private static final long serialVersionUID = -7208222758877437049L;
	
	private JFreeChart graph;
	private JLabel label;
	public E dataset;
	
	private String title;
	private String x, y;
	public boolean pointsVisible = true;
	public boolean labelsVisible = true;
	
	public double shownValsBelowMin = 1.0;
	public double shownValsAboveMax = 1.0;
	
	public Double exactMin = null;
	public Double exactMax = null;
	
	public boolean defaultRange = false;
	
	public static class Marker {
		double position;
		String label;
		
		public Marker(double position, String label) {
			this.position = position;
			this.label = label;
		}
		
		public Marker(double position) {
			this(position, "");
		}
	}
	
	public List<Marker> markers;
	
	public GenericChart(String title, String x, String y) {
		super();
		
		this.title = title;
		this.x = x;
		this.y = y;
		
		label = new JLabel();
		label.setIcon(null);
		add(label);
		
		markers = new ArrayList<Marker>();
	}
	
	public GenericChart(String x, String y) {
		this(null, x, y);
	}
	
	public GenericChart() {
		this(null, "X", "Y");
	}
	
	@SuppressWarnings("unchecked")
	public void initDataset(Class<?> type) throws InstantiationException, IllegalAccessException {
		dataset = (E) type.newInstance();
	}
	
	public void initDataset() {
		try {
			initDataset(DefaultCategoryDataset.class);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public void add(String series, double x, double y) {
		if (dataset instanceof DefaultCategoryDataset)
			((DefaultCategoryDataset) dataset).addValue(y, series, x + "");
		
		updateGraph();
		updateImage();
	}
	
	@Deprecated
	public void addPoint2Series(SeriesHandle series, double x, double y) {
		add("Series " + series.getPosition(), x, y);
	}
	
	public void clear() {
		if (dataset instanceof DefaultCategoryDataset)
			((DefaultCategoryDataset) dataset).clear();
		
		markers.clear();
	}
	
	public void updateImage() {
		if (alreadyUpdating)
			return;
		
		alreadyUpdating = true;
		
		if (dataset != null && graph != null) {
			ImageIcon icon;
			int width = getSize().width;
			if (width == 0)
				width = 400;
			int height = getSize().height;
			if (height == 0)
				height = 300;
			try {
				icon = new ImageIcon(graph.createBufferedImage(
						width,
						height));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
			}
			label.setIcon(icon);
			label.setVisible(true);
			setPreferredSize(label.getPreferredSize());

			label.validate();
		}
		
		alreadyUpdating = false;
	}
	
	private boolean alreadyUpdating = false;
	
	@SuppressWarnings("deprecation")
	public void updateGraph() {
		if (alreadyUpdating)
			return;
		
		alreadyUpdating = true;
		
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
		Font font2 = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		BasicStroke stroke = new BasicStroke(2.0f,                     // Line width
				BasicStroke.CAP_ROUND,     // End-cap style
				BasicStroke.JOIN_ROUND);   // Vertex join style
		
		float dash1[] = {10.0f};
		BasicStroke dotted = new BasicStroke(2.0f,                     // Line width
				BasicStroke.CAP_ROUND,     // End-cap style
				BasicStroke.JOIN_ROUND,   // Vertex join style
				10.0f, dash1, 0.0f);
		
		if (dataset instanceof DefaultCategoryDataset) {
			DefaultCategoryDataset dataset = (DefaultCategoryDataset) this.dataset;
			
			graph = ChartFactory.createLineChart(title, x, 
					y, dataset, PlotOrientation.VERTICAL, true, true, false);
			{
				CategoryPlot plot = (CategoryPlot) graph.getPlot();
				LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
						.getRenderer();
				
				graph.setAntiAlias(true);
				graph.setTextAntiAlias(RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
				
				if (pointsVisible) {
					renderer.setShapesVisible(true);
					renderer.setDrawOutlines(true);
					renderer.setUseFillPaint(true);
					renderer.setFillPaint(Color.white);
				}
				
				renderer.setStroke(stroke);
				
				double maxMarker = Double.MIN_VALUE, minMarker = Double.MAX_VALUE;
				
				for (Marker m : markers) {
					ValueMarker marker = new ValueMarker(m.position);  // position is the value on the axis
					marker.setLabel(m.label);
					marker.setLabelFont(font2);
					marker.setStroke(dotted);
					marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
					marker.setLabelAnchor(RectangleAnchor.BOTTOM_LEFT);
//					marker.setLabelOffset(new RectangleInsets(10.0, 10.0, 3.0, 3.0));
					
					if (!m.label.equals("")) {
						Paint p = renderer.lookupSeriesPaint(dataset.getRowIndex(m.label));
						if (p != null)
							marker.setPaint(p);
					}
					
					plot.addRangeMarker(marker);
					
					if (maxMarker < m.position)
						maxMarker = m.position;
					if (minMarker > m.position)
						minMarker = m.position;
				}
	
				NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
				rangeAxis.setTickLabelFont(font);
				
				if (!defaultRange) {
					double min = Double.MAX_VALUE, max = Double.MIN_VALUE, tmp;
					for (int i = 0; i < dataset.getColumnCount(); ++i)
						for (int j = 0; j < dataset.getRowCount(); ++j) {
							tmp = dataset.getValue(j, i).doubleValue();
							if (tmp < min)
								min = tmp;
							if (tmp > max)
								max = tmp;
						}
					
					if (min > minMarker)
						min = minMarker;
					if (max < maxMarker)
						max = maxMarker;
					
					if (min == Double.MAX_VALUE)
						min = 0;
					if (max == Double.MIN_VALUE)
						max = (int) rangeAxis.getRange().getUpperBound() + 1;
		
					if (exactMin != null)
						min = exactMin;
					else
						min = min - shownValsBelowMin;
					
					if (exactMax != null)
						max = exactMax;
					else
						max = max + shownValsAboveMax;
					
					if (min == max) {
						min = min - 1;
						max = max + 1;
					}
					
					rangeAxis.setRange(min, max);
				} else {
					double min = rangeAxis.getRange().getLowerBound();
					double max = rangeAxis.getRange().getUpperBound();
					
					if (min > minMarker)
						min = minMarker;
					if (max < maxMarker)
						max = maxMarker;
					
					rangeAxis.setRange(min, max);
				}
				
				CategoryAxis categoryAxis = plot.getDomainAxis();
				categoryAxis.setLowerMargin(0.02);
				categoryAxis.setUpperMargin(0.02);
				categoryAxis.setTickLabelFont(font);
	
				if (labelsVisible) {
					CategoryItemRenderer renderer2 = plot
							.getRenderer();
					CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
							"{2}", new DecimalFormat("0") {
		
								private static final long serialVersionUID = 1L;
		
								@Override
								public StringBuffer format(double number,
										StringBuffer result, FieldPosition fieldPosition) {
									result = new StringBuffer(getFormattedValue(number));
									return result;
								}
		
							});
					renderer2.setItemLabelGenerator(generator);
					renderer2.setItemLabelsVisible(true);
					renderer2.setItemLabelFont(font);
				}
			}
		} else {
			graph = null;
		}
		
		alreadyUpdating = false;
	}
	
	public String getFormattedValue(double number) {
		return (int)Math.round(number) + "";
	}
	
	public void save2png(String path, String fileName, int width, int height) throws IOException {
		File f = Paths.get(path, fileName).toFile();
		if (f.isDirectory() || !f.getParentFile().mkdirs())
			return;
		ChartUtilities.writeChartAsPNG(
				new FileOutputStream(f), graph, width, height);
	}
	
	public void save2png(String path, String fileName) throws IOException {
		this.save2png(path, fileName, 1350, 700);
	}
	
	public void save2png(String fileName) throws IOException {
		this.save2png(
				Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY).toString(),
				fileName, 1350, 700);
	}
	
	public void save2png() throws IOException {
		this.save2png(
				Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY).toString(),
				path2save, imageWidth, imageHeight);
	}
	
	private String path2save;
	private int imageWidth;
	private int imageHeight;
	
	/**
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public void initByProperties(String propertiesFileName)
			throws NumberFormatException, IOException {

		InputStream fileInput = this.getClass().getResourceAsStream(
				propertiesFileName);
		Properties properties = new Properties();
		properties.load(fileInput);
		fileInput.close();
		title = properties.getProperty("ImageTitle");
		path2save = properties.getProperty("path2save");
		imageWidth = Integer.parseInt(properties.getProperty("ImageWidth"));
		imageHeight = Integer.parseInt(properties.getProperty("ImageHeight"));
	}
	
	public static GenericChart<DefaultCategoryDataset> createConstraintsLogger() throws NumberFormatException, IOException {
		GenericChart<DefaultCategoryDataset> constraintsLogger = new GenericChart<DefaultCategoryDataset>("Iteration", "Y");
		constraintsLogger.dataset = new DefaultCategoryDataset();
		constraintsLogger.defaultRange = true;
		constraintsLogger.labelsVisible = false;
		constraintsLogger.pointsVisible = false;
		
		constraintsLogger.initByProperties("constraints.properties");
		constraintsLogger.title = "Violated Constraints";
		
		return constraintsLogger;
	}
	
	public static GenericChart<DefaultCategoryDataset> createVmLogger() throws NumberFormatException, IOException {
		GenericChart<DefaultCategoryDataset> vmLogger = new GenericChart<DefaultCategoryDataset>("Iteration", "Y");
		vmLogger.dataset = new DefaultCategoryDataset();
		vmLogger.defaultRange = true;
		vmLogger.labelsVisible = false;
		vmLogger.pointsVisible = false;
		
		vmLogger.initByProperties("vmCount.properties");
		vmLogger.title = "Total Number of VMs";
		
		return vmLogger;
	}
	
	public static GenericChart<DefaultCategoryDataset> createCostLogger() throws NumberFormatException, IOException {
		GenericChart<DefaultCategoryDataset> costLogger = new GenericChart<DefaultCategoryDataset>("Iteration", "Y");
		costLogger.dataset = new DefaultCategoryDataset();
		costLogger.defaultRange = true;
		costLogger.labelsVisible = false;
		costLogger.pointsVisible = false;
		
		costLogger.initByProperties("conf.properties");
		costLogger.title = "Solution Cost";
		
		return costLogger;
	}
	
	public static GenericChart<DefaultCategoryDataset> createResponseTimeLogger() throws NumberFormatException, IOException {
		GenericChart<DefaultCategoryDataset> costLogger = new GenericChart<DefaultCategoryDataset>("Iteration", "Y");
		costLogger.dataset = new DefaultCategoryDataset();
		costLogger.defaultRange = true;
		costLogger.labelsVisible = false;
		costLogger.pointsVisible = false;
		
		costLogger.initByProperties("responseTime.properties");
		costLogger.title = "Average Response Times";
		
		return costLogger;
	}
	
	public static GenericChart<DefaultCategoryDataset> createUtilizationLogger() throws NumberFormatException, IOException {
		GenericChart<DefaultCategoryDataset> costLogger = new GenericChart<DefaultCategoryDataset>("Iteration", "Y");
		costLogger.dataset = new DefaultCategoryDataset();
		costLogger.defaultRange = true;
		costLogger.labelsVisible = false;
		costLogger.pointsVisible = false;
		
		costLogger.initByProperties("utilization.properties");
		costLogger.title = "CPU Utilization";
		
		return costLogger;
	}
	
}
