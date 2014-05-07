package it.polimi.modaclouds.space4cloud.gui;

import it.polimi.modaclouds.qos_models.schema.ClosedWorkload;
import it.polimi.modaclouds.qos_models.schema.ClosedWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.OpenWorkload;
import it.polimi.modaclouds.qos_models.schema.OpenWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtensions;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.utils.DOM;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.FieldPosition;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.xml.bind.JAXBException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RobustnessProgressWindow {
	
	private JFrame gui;
	private JProgressBar progressBar;
	
	private JFreeChart graph;
	private JPanel graphPanel;	
	private JLabel graphLabel;
	
	private JFreeChart solution;
	private JPanel solutionPanel;	
	private JLabel solutionLabel;
	
	private JFreeChart tierg;
	private JPanel tiersPanel;	
	private JLabel tiersLabel;
	
	private JFreeChart tiergBasic;
	
	private int total;
	
	private DefaultCategoryDataset populations = new DefaultCategoryDataset();
	private DefaultCategoryDataset solutions = new DefaultCategoryDataset();
	private DefaultCategoryDataset tiers = new DefaultCategoryDataset();
	private DefaultCategoryDataset tiersBasic = new DefaultCategoryDataset();
	
	private static enum Size { zero, t1micro, m1small, m1medium, c1medium, c3large, m1xlarge, c3xlarge };
	private static enum BasicSize { zero, micro, small, medium, large, xlarge, xxlarge, xxxxlarge, xxxxxxxxlarge };
	
	public void add(String name, File usageModelExtension, File solution) throws MalformedURLException, JAXBException {
		UsageModelExtensions umes = XMLHelper.deserialize(usageModelExtension.toURI().toURL(),
				UsageModelExtensions.class);
		
		int maxPopulation = -1, maxHour = -1;
		
		ClosedWorkload cw = umes.getUsageModelExtension().getClosedWorkload();
		if (cw != null) {
			for (ClosedWorkloadElement we : cw.getWorkloadElement()) {
				if (maxPopulation < we.getPopulation()) {
					maxPopulation = we.getPopulation();
					maxHour = we.getHour();
				}
				populations.addValue(we.getPopulation(), name, "" + we.getHour());
			}
		}
		else {
			
			OpenWorkload ow = umes.getUsageModelExtension().getOpenWorkload();
			if (ow != null) {
				for (OpenWorkloadElement we : ow.getWorkloadElement()) {
					if (maxPopulation < we.getPopulation()) {
						maxPopulation = we.getPopulation();
						maxHour = we.getHour();
					}
					populations.addValue(we.getPopulation(), name, "" + we.getHour());
				}
			}
			else {
				return;
			}
		}
		
		Document doc = DOM.getDocument(solution);
		
		NodeList nl = doc.getElementsByTagName("Tier");
		
		for (int i = 0; i < nl.getLength(); i++) {
			Node tier = nl.item(i);
			
			String size = tier.getAttributes().getNamedItem("resourceName").getNodeValue();
			Size s;
			BasicSize bs;
			try {
				s = Size.valueOf(size.replace('.', '-').replaceAll("-", ""));
				
				String tmp = size.substring(size.indexOf('.') + 1);
				if (tmp.charAt(0) == '2' || tmp.charAt(0) == '4' || tmp.charAt(0) == '8')
					for (int u = 0; u < ((int)tmp.charAt(0))-1; ++u)
						tmp = "x" + tmp;
				bs = BasicSize.valueOf(tmp);
			} catch (Exception e) {
				s = Size.zero;
				bs = BasicSize.zero;
			}
			tiers.addValue(s.ordinal(), "Tier " + i, "" + maxPopulation);
			tiersBasic.addValue(bs.ordinal(), "Tier " + i, "" + maxPopulation);
			
			Element tierEl = (Element) tier;
			NodeList hours = tierEl.getElementsByTagName("HourAllocation");
			
			for (int j = 0; j < hours.getLength(); j++) {
				Node hour = hours.item(j);
				int valHour = Integer.valueOf(hour.getAttributes().getNamedItem("hour").getNodeValue()) + 1;
				
				if (valHour == maxHour) {
					solutions.addValue(Integer.valueOf(hour.getAttributes().getNamedItem("allocation").getNodeValue()), "Tier " + i, "" + maxPopulation);
				}
				
			}
		}
		
		sortDataset(solutions);
		sortDataset(tiers);
		
		updateGraph();
	}
	
	@SuppressWarnings("unchecked")
	public static void sortDataset(DefaultCategoryDataset dataset) {
		for (int i = 0; i < dataset.getColumnCount()-1; ++i) {
			for (int j = i+1; j < dataset.getColumnCount(); ++j) {
				boolean bigger = false;
				try {
					bigger = (Integer.parseInt((String)dataset.getColumnKey(i))) > (Integer.parseInt((String)dataset.getColumnKey(j)));
				} catch (Exception e) {
					bigger = dataset.getColumnKey(i).compareTo(dataset.getColumnKey(j)) == 1;
				}
				
				if (bigger) {
					Comparable<?> key = dataset.getColumnKey(i);
					
					for (Object o : dataset.getRowKeys()) {
						dataset.addValue(dataset.getValue((Comparable<?>)o, key), (Comparable<?>)o, "tmp");
					}
					dataset.removeColumn(i);
					
					for (Object o : dataset.getRowKeys()) {
						dataset.addValue(dataset.getValue((Comparable<?>)o, "tmp"), (Comparable<?>)o, key);
					}
					dataset.removeColumn("tmp");
				}
			}
		}
	}
	
	public RobustnessProgressWindow(int total) {
		this.total = total;
		
		updateGraph();
		
		
		initialize();
	}
	
	@SuppressWarnings({ "deprecation" })
	public void updateGraph() {
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
		
		graph = ChartFactory.createLineChart(null, "Hour", "Population", populations);
		{
			CategoryPlot plot = (CategoryPlot) graph.getPlot();
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.white);
			
			CategoryAxis categoryAxis = plot.getDomainAxis();
			categoryAxis.setLowerMargin(0.02);
			categoryAxis.setUpperMargin(0.02);
			categoryAxis.setTickLabelFont(font);
			
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setTickLabelFont(font);
			
			
		}
		
		solution = ChartFactory.createLineChart(null, "Max Population", "Allocation", solutions, PlotOrientation.VERTICAL, true, true, false);
		{
			CategoryPlot plot = (CategoryPlot) solution.getPlot();
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.white);
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setRange(0, rangeAxis.getRange().getUpperBound() * 1.1);
			rangeAxis.setTickLabelFont(font);
			
			CategoryAxis categoryAxis = plot.getDomainAxis();
			categoryAxis.setLowerMargin(0.02);
			categoryAxis.setUpperMargin(0.02);
			categoryAxis.setTickLabelFont(font);
			
			CategoryItemRenderer renderer2 = (CategoryItemRenderer) plot.getRenderer();
			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0"));
			renderer2.setItemLabelGenerator(generator);
			renderer2.setItemLabelsVisible(true);
			renderer2.setItemLabelFont(font);
		}
		
		tierg = ChartFactory.createLineChart(null, "Max Population", "Size", tiers, PlotOrientation.VERTICAL, true, true, false);
		{
			CategoryPlot plot = (CategoryPlot) tierg.getPlot();
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.white);
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setTickLabelFont(font);
			
			int min = 10, max = 0, tmp;
			for (int i = 0; i < tiers.getColumnCount(); ++i)
				for (int j = 0; j < tiers.getRowCount(); ++j) {
					tmp = tiers.getValue(j, i).intValue();
					if (tmp < min)
						min = tmp;
					if (tmp > max)
						max = tmp;
				}
			if (min == 10)
				min = 0;
			if (max == 0)
				max = (int)rangeAxis.getRange().getUpperBound() + 1;
			
			rangeAxis.setRange(/*0*/ min - 0.5, /*rangeAxis.getRange().getUpperBound() + 1*/ max + 0.5);
			
			CategoryAxis categoryAxis = plot.getDomainAxis();
			categoryAxis.setLowerMargin(0.02);
			categoryAxis.setUpperMargin(0.02);
			categoryAxis.setTickLabelFont(font);
			
			CategoryItemRenderer renderer2 = (CategoryItemRenderer) plot.getRenderer();
			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0") {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
				
				public StringBuffer format(double number,
		                  StringBuffer result,
		                  FieldPosition fieldPosition) {
					result = new StringBuffer(Size.values()[(int)number].toString());
					return result;
				}
				
			});
			renderer2.setItemLabelGenerator(generator);
			renderer2.setItemLabelsVisible(true);
			renderer2.setItemLabelFont(font);
		}
		
		tiergBasic = ChartFactory.createLineChart(null, "Max Population", "Size", tiersBasic, PlotOrientation.VERTICAL, true, true, false);
		{
			CategoryPlot plot = (CategoryPlot) tiergBasic.getPlot();
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.white);
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setTickLabelFont(font);
			
			int min = 10, max = 0, tmp;
			for (int i = 0; i < tiersBasic.getColumnCount(); ++i)
				for (int j = 0; j < tiersBasic.getRowCount(); ++j) {
					tmp = tiersBasic.getValue(j, i).intValue();
					if (tmp < min)
						min = tmp;
					if (tmp > max)
						max = tmp;
				}
			if (min == 10)
				min = 0;
			if (max == 0)
				max = (int)rangeAxis.getRange().getUpperBound() + 1;
			
			rangeAxis.setRange(/*0*/ min - 0.5, /*rangeAxis.getRange().getUpperBound() + 1*/ max + 0.5);
			
			CategoryAxis categoryAxis = plot.getDomainAxis();
			categoryAxis.setLowerMargin(0.02);
			categoryAxis.setUpperMargin(0.02);
			categoryAxis.setTickLabelFont(font);
			
			CategoryItemRenderer renderer2 = (CategoryItemRenderer) plot.getRenderer();
			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0") {
	
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
				
				public StringBuffer format(double number,
		                  StringBuffer result,
		                  FieldPosition fieldPosition) {
					result = new StringBuffer(BasicSize.values()[(int)number].toString());
					return result;
				}
				
			});
			renderer2.setItemLabelGenerator(generator);
			renderer2.setItemLabelsVisible(true);
			renderer2.setItemLabelFont(font);
		}
		
	}
	
	public void initialize() {
		gui = new JFrame();
		gui.setTitle("Robustness Progress");
		gui.setBounds(100, 100, 650, 400);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel upperPanel = new JPanel();
		gui.getContentPane().add(upperPanel, BorderLayout.NORTH);
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));
		
		progressBar = new JProgressBar(0, total);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		upperPanel.add(progressBar);
		
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(3, 1, 0, 0));
		gui.getContentPane().add(lowerPanel, BorderLayout.CENTER);
		
		graphPanel = new JPanel();
		lowerPanel.add(graphPanel);
		
		graphLabel = new JLabel();
		graphLabel.setIcon(null);
		graphPanel.add(graphLabel);
		
		solutionPanel = new JPanel();
		lowerPanel.add(solutionPanel);
		
		solutionLabel = new JLabel();
		solutionLabel.setIcon(null);
		solutionPanel.add(solutionLabel);
		
		tiersPanel = new JPanel();
		lowerPanel.add(tiersPanel);
		
		tiersLabel = new JLabel();
		tiersLabel.setIcon(null);
		tiersPanel.add(tiersLabel);
		
		//listener to resize images
		gui.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
				updateImages();		
			}

			@Override
			public void componentResized(ComponentEvent e) {
				updateImages();				
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub				
			}
		});
		
		gui.setVisible(true);
		updateImages();
	}
	
	public void setValue(int value) {
		if (value > total)
			value = total;
		progressBar.setValue(value);
	}
	
	public int getValue() {
		return progressBar.getValue();
	}
	
	private void updateImages() {
		if (graph != null) {
			ImageIcon icon;
			try{
				icon = new ImageIcon(graph.createBufferedImage(graphPanel.getSize().width, graphPanel.getSize().height)); 
			}catch (NullPointerException e){
				icon = new ImageIcon();
			}
			graphLabel.setIcon(icon);            
			graphLabel.setVisible(true);
			graphPanel.setPreferredSize(graphLabel.getPreferredSize());
		}
		
		if (solution != null) {
			ImageIcon icon;
			try{
				icon = new ImageIcon(solution.createBufferedImage(solutionPanel.getSize().width, solutionPanel.getSize().height)); 
			}catch (NullPointerException e){
				icon = new ImageIcon();
			}
			solutionLabel.setIcon(icon);            
			solutionLabel.setVisible(true);
			solutionPanel.setPreferredSize(solutionLabel.getPreferredSize());
		}
		
		if (tiers != null) {
			ImageIcon icon;
			try{
				icon = new ImageIcon(tierg.createBufferedImage(tiersPanel.getSize().width, tiersPanel.getSize().height)); 
			}catch (NullPointerException e){
				icon = new ImageIcon();
			}
			tiersLabel.setIcon(icon);            
			tiersLabel.setVisible(true);
			tiersPanel.setPreferredSize(tiersLabel.getPreferredSize());
		}
	}
	
	public void save2png(String path) throws IOException {
		ChartUtilities.writeChartAsPNG(new FileOutputStream(new File(path + File.separator + "allocations.png")), solution, 1350, 700);
		ChartUtilities.writeChartAsPNG(new FileOutputStream(new File(path + File.separator + "populations.png")), graph, 1350, 700);
		ChartUtilities.writeChartAsPNG(new FileOutputStream(new File(path + File.separator + "sizes.png")), tierg, 1350, 700);
		ChartUtilities.writeChartAsPNG(new FileOutputStream(new File(path + File.separator + "basicSizes.png")), tiergBasic, 1350, 700);
	}
	
	public static void main(String args[]) {
		int total = 3;
		
		RobustnessProgressWindow rpw = new RobustnessProgressWindow(total);
		
//		String basePath = "C:\\Users\\Riccardo\\Desktop\\SPACE4CLOUD\\workspace\\modaclouds-space4cloud\\src\\it\\polimi\\modaclouds\\space4cloud\\gui\\";
		String basePath = "C:\\Users\\Riccardo\\Desktop\\tmp\\partials\\bipicco";
		
		try {
//			rpw.add("Initial", new File(basePath + "usage_model_extensionOfBiz.xml"), new File(basePath + "solution-1.0.xml"));
//			rpw.add("Var 0", new File(basePath + "ume0-3-8991782702721664551.xml"), new File(basePath + "solution-0.3.xml"));
//			rpw.add("Var 1", new File(basePath + "ume0-5-7139252741102642498.xml"), new File(basePath + "solution-0.5.xml"));
//			rpw.add("Var 2", new File(basePath + "ume0-7-6198876986015331066.xml"), new File(basePath + "solution-0.7.xml"));
//			rpw.add("Var 3", new File(basePath + "ume0-9-5114252506963290152.xml"), new File(basePath + "solution-0.9.xml"));
//			rpw.add("Var 4", new File(basePath + "ume1-1-417778809152828686.xml"), new File(basePath + "solution-1.1.xml"));
//			rpw.add("Var 5", new File(basePath + "ume1-3-4388284814346709586.xml"), new File(basePath + "solution-1.3.xml"));
//			rpw.add("Var 6", new File(basePath + "ume1-5-2764229859526493739.xml"), new File(basePath + "solution-1.5.xml"));
//			rpw.add("Var 7", new File(basePath + "ume1-7-6585118236013770088.xml"), new File(basePath + "solution-1.7.xml"));
			
			for (int i = 100; i <= 10000; i += 300) {
				rpw.add("" + i, new File(basePath + File.separator + "ume-" + i + ".xml"), new File(basePath + File.separator + "solution-" + i + ".xml"));
			}
			
			rpw.save2png(basePath);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for (int value = 0; value <= total; ++value) {
			rpw.setValue(value);
			try {
				Thread.sleep(1000);
			} catch (Exception e) { }
		}
	}

}
