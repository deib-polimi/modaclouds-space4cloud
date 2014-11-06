package it.polimi.modaclouds.space4cloud.gui;

import it.polimi.modaclouds.qos_models.schema.ClosedWorkload;
import it.polimi.modaclouds.qos_models.schema.ClosedWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.OpenWorkload;
import it.polimi.modaclouds.qos_models.schema.OpenWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtensions;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.mainProgram.Space4Cloud;
import it.polimi.modaclouds.space4cloud.optimization.bursting.PrivateCloud;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.privatecloud.Configuration;
import it.polimi.modaclouds.space4cloud.utils.DOM;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.xml.bind.JAXBException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
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
import org.xml.sax.SAXException;

public class RobustnessProgressWindow extends WindowAdapter implements PropertyChangeListener {

	public static enum Size {

		zero("zero", 0),
		
		// Amazon
		t1micro("micro", 1), m1small("small", 2),
		m1medium("medium", 3), c1medium("medium", 3),
		m1large("large", 4), c3large("large", 4),
		m1xlarge("xlarge", 5), m2xlarge("xlarge", 5), m3xlarge("xlarge", 5), c1xlarge("xlarge", 5), c3xlarge("xlarge", 5),
		m22xlarge("xxlarge", 6), m32xlarge("xxlarge", 6), c32xlarge("xxlarge", 6),
		cg14xlarge("xxxxlarge", 7),

		// Microsoft
		GeneralAvailabilityExtraSmallInstance("ExtraSmall", 8),	PreviewExtraSmallInstance("ExtraSmall", 8),
		GeneralAvailabilitySmallInstance("Small", 9), PreviewSmallInstance("Small", 9),
		GeneralAvailabilityMediumInstance("Medium", 10), PreviewMediumInstance("Medium", 10),
		GeneralAvailabilityLargeInstance("Large", 11), PreviewLargeInstance("Large", 11),
		GeneralAvailabilityExtraLargeInstance("ExtraLarge", 12), PreviewExtraLargeInstance("ExtraLarge", 12),

		// Flexiscale
		Flexiscale512MB1CPUServer("512 MB/1 CPU", 13), Flexiscale512MB1CPUServerWindows("512 MB/1 CPU", 13),
		Flexiscale1GB1CPUServer("1 GB/1 CPU", 14), Flexiscale1GB1CPUServerWindows("1 GB/1 CPU", 14),
		Flexiscale2GB1CPUServer("2 GB/1 CPU", 15), Flexiscale2GB1CPUServerWindows("2 GB/1 CPU", 15),
		Flexiscale2GB2CPUServer("2 GB/2 CPU", 16), Flexiscale2GB2CPUServerWindows("2 GB/2 CPU", 16),
		Flexiscale4GB2CPUServer("4 GB/2 CPU", 17), Flexiscale4GB2CPUServerWindows("4 GB/2 CPU", 17),
		Flexiscale4GB3CPUServer("4 GB/3 CPU", 18), Flexiscale4GB3CPUServerWindows("4 GB/3 CPU", 18),
		Flexiscale4GB4CPUServer("4 GB/4 CPU", 19), Flexiscale4GB4CPUServerWindows("4 GB/4 CPU", 19),
		Flexiscale6GB3CPUServer("6 GB/3 CPU", 20), Flexiscale6GB3CPUServerWindows("6 GB/3 CPU", 20),
		Flexiscale6GB4CPUServer("6 GB/4 CPU", 21), Flexiscale6GB4CPUServerWindows("6 GB/4 CPU", 21),
		Flexiscale6GB5CPUServer("6 GB/5 CPU", 22), Flexiscale6GB5CPUServerWindows("6 GB/5 CPU", 22),
		Flexiscale6GB6CPUServer("6 GB/6 CPU", 23), Flexiscale6GB6CPUServerWindows("6 GB/6 CPU", 23),
		Flexiscale8GB4CPUServer("8 GB/4 CPU", 24), Flexiscale8GB4CPUServerWindows("8 GB/4 CPU", 24),
		Flexiscale8GB5CPUServer("8 GB/5 CPU", 25), Flexiscale8GB5CPUServerWindows("8 GB/5 CPU", 25),
		Flexiscale8GB6CPUServer("8 GB/6 CPU", 26), Flexiscale8GB6CPUServerWindows("8 GB/6 CPU", 26),
		Flexiscale8GB7CPUServer("8 GB/7 CPU", 27), Flexiscale8GB7CPUServerWindows("8 GB/7 CPU", 27),
		Flexiscale8GB8CPUServer("8 GB/8 CPU", 28), Flexiscale8GB8CPUServerWindows("8 GB/8 CPU", 28);

		private static final int lastAmazonId = 7, lastMicrosoftId = 12, lastFlexiscaleId = 28;

		public static Size getSizeByBasicId(int basicId) {
			for (Size s : Size.values())
				if (s.basicId == basicId)
					return s;
			return Size.zero;
		}
		public static Size parse(String size) {
			Size s;
			try {
				s = Size.valueOf(size.replace('.', '-').replaceAll("-", "")
						.replaceAll(" ", "").replaceAll("/", ""));
			} catch (Exception e) {
				e.printStackTrace();
				s = Size.zero;
			}
			return s;
		}

		public String basicName;

		public int basicId;

		private Size(String basicName, int basicId) {
			this.basicName = basicName;
			this.basicId = basicId;
		}

		public boolean biggerThan(Size s) {
			if (basicId == 0 || s.basicId == 0)
				return false;
			if ((basicId <= lastAmazonId && s.basicId <= lastAmazonId)
					|| (basicId > lastAmazonId && s.basicId > lastAmazonId
							&& basicId <= lastMicrosoftId && s.basicId <= lastMicrosoftId)
					|| (basicId > lastMicrosoftId
							&& s.basicId > lastMicrosoftId
							&& basicId <= lastFlexiscaleId && s.basicId <= lastFlexiscaleId))
				return basicId > s.basicId;
			return false;
		}
	}
	@SuppressWarnings("deprecation")
	public static void compare(RobustnessProgressWindow rpw1,
			RobustnessProgressWindow rpw2, String pathFile) {
		DefaultCategoryDataset costs1 = rpw1.costs, costs2 = rpw2.costs;
		DefaultCategoryDataset costs3 = new DefaultCategoryDataset();

		if (costs1.getColumnCount() != costs2.getColumnCount())
			return;

		float total = 0, max = Float.MIN_VALUE, min = Float.MAX_VALUE;

		for (int i = 0; i < costs1.getColumnCount(); ++i) {
			float diff = (float) ((Double) costs1.getValue(0, i) - (Double) costs2
					.getValue(0, i));
			total += diff;
			if (diff < min)
				min = diff;
			if (diff > max)
				max = diff;
			costs3.addValue(diff, "Diff", costs1.getColumnKey(i));
		}

		JFreeChart costsGraph = ChartFactory.createLineChart("Total: " + total
				+ ", Min: " + min + ", Max: " + max, "Max Population",
				"Difference", costs3, PlotOrientation.VERTICAL, true, true,
				false);
		{
			CategoryPlot plot = (CategoryPlot) costsGraph.getPlot();
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
					.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.white);
			Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

			CategoryAxis categoryAxis = plot.getDomainAxis();
			categoryAxis.setLowerMargin(0.02);
			categoryAxis.setUpperMargin(0.02);
			categoryAxis.setTickLabelFont(font);

			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setTickLabelFont(font);

			CategoryItemRenderer renderer2 = plot
					.getRenderer();
			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
					"{2}", new DecimalFormat("0") {

						/**
                 *
                 */
						private static final long serialVersionUID = 1L;

						@Override
						public StringBuffer format(double number,
								StringBuffer result, FieldPosition fieldPosition) {
							result = new StringBuffer("" + (float) number);
							return result;
						}

					});
			renderer2.setItemLabelGenerator(generator);
			renderer2.setItemLabelsVisible(true);
			renderer2.setItemLabelFont(font);
		}

		try {
			ChartUtilities.writeChartAsPNG(new FileOutputStream(new File(
					pathFile)), costsGraph, 1350, 700);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static RobustnessProgressWindow redraw(String basePath) {
		RobustnessProgressWindow rpw = null;
		
		Path p = Paths.get(basePath);
		if (Files.exists(p) && Files.isDirectory(p)) {
			File[] umes = p.toFile().listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return (name.indexOf("ume-") == 0) && (name.indexOf(".xml") == name.length() - 4);
				}
			});
			
			File[] solutions = p.toFile().listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return (name.indexOf("solution-") == 0) && (name.indexOf(".xml") == name.length() - 4);
				}
			});
			
			rpw = new RobustnessProgressWindow(solutions.length);
			
			int j = 0;
			for (int i = 0; i < umes.length; ++i) {
				String ume = umes[i].getName();
				String solution = solutions[j].getName();
				
				String test = ume.substring(ume.indexOf("-"), ume.indexOf("xml"));
				
				if (solution.indexOf(test) != -1) {
					try {
						rpw.add(umes[i], solutions[j++]);
					} catch (MalformedURLException | JAXBException
							| SAXException e) {
						e.printStackTrace();
					}
					rpw.setValue(rpw.getValue() + 1);
				}
				
			}
			
			try {
				rpw.save2png(basePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return rpw;
	}
	@SuppressWarnings("unchecked")
	public static void sortDataset(DefaultCategoryDataset dataset) {
		for (int i = 0; i < dataset.getColumnCount() - 1; ++i) {
			for (int j = i + 1; j < dataset.getColumnCount(); ++j) {
				if (i < 0)
					continue;
				
				boolean bigger = false;
				try {
					bigger = (Integer
							.parseInt((String) dataset.getColumnKey(i))) > (Integer
							.parseInt((String) dataset.getColumnKey(j)));
				} catch (Exception e) {
					bigger = dataset.getColumnKey(i).compareTo(
							dataset.getColumnKey(j)) == 1;
				}

				if (bigger) {
					Comparable<?> key = dataset.getColumnKey(i);

					for (Object o : dataset.getRowKeys()) {
						dataset.addValue(
								dataset.getValue((Comparable<?>) o, key),
								(Comparable<?>) o, "tmp");
					}
					dataset.removeColumn(i--);

					for (Object o : dataset.getRowKeys()) {
						dataset.addValue(
								dataset.getValue((Comparable<?>) o, "tmp"),
								(Comparable<?>) o, key);
					}
					dataset.removeColumn("tmp");
				}
			}
		}
	}

	private JFrame gui;
	private JProgressBar progressBar;
	
	private JFreeChart populationsGraph;
	private JPanel populationsPanel;
	private JLabel populationsLabel;
	private DefaultCategoryDataset populations = new DefaultCategoryDataset();

	private JFreeChart solutionsGraph;
	private JPanel solutionsPanel;
	private JLabel solutionsLabel;
	private JPanel solutionsPanel2;
	private JLabel solutionsLabel2;
	private DefaultCategoryDataset solutions = new DefaultCategoryDataset();

	private JFreeChart tiersGraph;
	private JPanel tiersPanel;
	private JLabel tiersLabel;
	private JPanel tiersPanel2;
	private JLabel tiersLabel2;
	private DefaultCategoryDataset tiers = new DefaultCategoryDataset();
	
	private JFreeChart tiersBasicGraph;
	private JPanel tiersBasicPanel;
	private JLabel tiersBasicLabel;
	private DefaultCategoryDataset tiersBasic = new DefaultCategoryDataset();
	
	private JFreeChart feasibilitiesGraph;
	private JPanel feasibilitiesPanel;
	private JLabel feasibilitiesLabel;
	private DefaultCategoryDataset feasibilities = new DefaultCategoryDataset();
	
	private JFreeChart costsGraph;
	private JPanel costsPanel;
	private JLabel costsLabel;
	private DefaultCategoryDataset costs = new DefaultCategoryDataset();
	
	private JFreeChart durationsGraph;
	private JPanel durationsPanel;
	private JLabel durationsLabel;
	private DefaultCategoryDataset durations = new DefaultCategoryDataset();
	
//	private JFreeChart privateHostsGraph;
//	private JPanel privateHostsPanel;
//	private JLabel privateHostsLabel;
//	private DefaultCategoryDataset privateHosts = new DefaultCategoryDataset();
//	
//	private JFreeChart privateMachinesGraph;
//	private JPanel privateMachinesPanel;
//	private JLabel privateMachinesLabel;
//	private DefaultCategoryDataset privateMachines = new DefaultCategoryDataset();
//	
//	private JFreeChart hourlySolutionsGraph;
//	private JPanel hourlySolutionsPanel;
//	private JLabel hourlySolutionsLabel;
//	private DefaultCategoryDataset hourlySolutions = new DefaultCategoryDataset();
	
	private JTabbedPane privateHostsTabs;
	private JTabbedPane publicVsPrivateTabs;
	
	private HashMap<Integer, JFreeChart> privateHostsGraphs = new HashMap<Integer, JFreeChart>();
	private HashMap<Integer, JPanel> privateHostsPanels = new HashMap<Integer, JPanel>();
	private HashMap<Integer, JLabel> privateHostsLabels = new HashMap<Integer, JLabel>();
	private HashMap<Integer, DefaultCategoryDataset> privateHostsMap = new HashMap<Integer, DefaultCategoryDataset>();
	
	private HashMap<Integer, JFreeChart> privateMachinesGraphs = new HashMap<Integer, JFreeChart>();
	private HashMap<Integer, JPanel> privateMachinesPanels = new HashMap<Integer, JPanel>();
	private HashMap<Integer, JLabel> privateMachinesLabels = new HashMap<Integer, JLabel>();
	private HashMap<Integer, DefaultCategoryDataset>  privateMachinesMap = new HashMap<Integer, DefaultCategoryDataset>();
	
	private HashMap<Integer, JFreeChart> hourlySolutionsGraphs = new HashMap<Integer, JFreeChart>();
	private HashMap<Integer, JPanel> hourlySolutionsPanels = new HashMap<Integer, JPanel>();
	private HashMap<Integer, JLabel> hourlySolutionsLabels = new HashMap<Integer, JLabel>();
	private HashMap<Integer, DefaultCategoryDataset>  hourlySolutionsMap = new HashMap<Integer, DefaultCategoryDataset>();
	
	private int total;

	private boolean alreadyUpdating = false;
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public RobustnessProgressWindow(int total) {
		this.total = total;

		updateGraph();

		initialize();
	}

	public void add(File usageModelExtension, File solution)
			throws MalformedURLException, JAXBException, SAXException {
		int maxPopulation = Space4Cloud.getMaxPopulation(usageModelExtension);
		int maxHour = -1;
		
		DefaultCategoryDataset privateHosts = new DefaultCategoryDataset();
		DefaultCategoryDataset privateMachines = new DefaultCategoryDataset();
		DefaultCategoryDataset hourlySolutions = new DefaultCategoryDataset();
		
		
		String name = "Var " + maxPopulation;

		{
			// Add the workload to the graph
			
			UsageModelExtensions umes = XMLHelper.deserialize(usageModelExtension
					.toURI().toURL(), UsageModelExtensions.class);
			
			ClosedWorkload cw = umes.getUsageModelExtension().getClosedWorkload();
			if (cw != null) {
				for (ClosedWorkloadElement we : cw.getWorkloadElement()) {
					if (maxPopulation == we.getPopulation())
						maxHour = we.getHour();
					
					populations.addValue(we.getPopulation(), name,
							"" + we.getHour());
				}
			} else {
	
				OpenWorkload ow = umes.getUsageModelExtension().getOpenWorkload();
				if (ow != null) {
					for (OpenWorkloadElement we : ow.getWorkloadElement()) {
						if (maxPopulation == we.getPopulation())
							maxHour = we.getHour();
						
						populations.addValue(we.getPopulation(), name,
								"" + we.getHour());
					}
				} else {
					return;
				}
			}
		}

		Document doc = DOM.getDocument(solution);

		NodeList nl = doc.getElementsByTagName("Tier");
		
		LinkedHashMap<String, Integer[]> usageHosts = new LinkedHashMap<String, Integer[]>();
		
		LinkedHashMap<String, Integer[]> machinesOnPrivate = new LinkedHashMap<String, Integer[]>();

		for (int i = 0; i < nl.getLength(); i++) {
			Node tier = nl.item(i);

			String size = tier.getAttributes().getNamedItem("resourceName")
					.getNodeValue();
			
			String provider = tier.getAttributes().getNamedItem("providerName")
					.getNodeValue();
			
			if (provider.indexOf(PrivateCloud.BASE_PROVIDER_NAME) == -1) {
				Size s = Size.parse(size);
				
				String tierName = null;
				try {
					tierName = tier.getAttributes().getNamedItem("name").getNodeValue() + "@" + provider;
				} catch (Exception e) {
					tierName = null;
				}
				if (tierName == null || tierName.length() == 0)
					tierName = "Tier " + i;
				
	
				tiers.addValue(s.ordinal(), /*"Tier " + i*/ tierName, "" + maxPopulation);
				tiersBasic.addValue(s.basicId, /*"Tier " + i*/ tierName, "" + maxPopulation);
	
				Element tierEl = (Element) tier;
				NodeList hours = tierEl.getElementsByTagName("HourAllocation");
	
				for (int j = 0; j < hours.getLength(); j++) {
					Node hour = hours.item(j);
					int valHour = Integer.valueOf(hour.getAttributes()
							.getNamedItem("hour").getNodeValue()) + 1;
	
					if (valHour == maxHour) {
						solutions
								.addValue(
										Integer.valueOf(hour.getAttributes()
												.getNamedItem("allocation")
												.getNodeValue()), /*"Tier " + i*/ tierName, ""
												+ maxPopulation);
					}
	
				}
			} else {
				Integer[] usage = usageHosts.get(provider);
				if (usage == null) {
					usage = new Integer[24];
					for (int h = 0; h < 24; ++h)
						usage[h] = 0;
				}
				
				String key = tier.getAttributes().getNamedItem("name").getNodeValue() + "@" + size;
				
				Integer[] machines = machinesOnPrivate.get(key);
				if (machines == null) {
					machines = new Integer[24];
					for (int h = 0; h < 24; ++h)
						machines[h] = 0;
				}
				
				Element tierEl = (Element) tier;
				NodeList hours = tierEl.getElementsByTagName("HourAllocation");
	
				for (int j = 0; j < hours.getLength(); j++) {
					Node hour = hours.item(j);
					int valHour = Integer.valueOf(hour.getAttributes()
							.getNamedItem("hour").getNodeValue());
					int valAllocation = Integer.valueOf(hour.getAttributes()
							.getNamedItem("allocation")
							.getNodeValue());
					
					usage[valHour] += valAllocation;
					machines[valHour] += valAllocation;
				}
				
				usageHosts.put(provider, usage);
				machinesOnPrivate.put(key, machines);
			}
		}
		
		if (machinesOnPrivate.size() == 0) {
			nl = doc.getElementsByTagName("Solution");
			
			Node selected = null;
			double maxCost = Double.MIN_VALUE;
			
			for (int i = 0; i < nl.getLength(); i++) {
				Node solutionResult = nl.item(i);

				double cost = Double.parseDouble(solutionResult.getAttributes()
						.getNamedItem("cost").getNodeValue());
				
				String provider =
						((Element)solutionResult).getElementsByTagName("Tier").item(0).getAttributes().getNamedItem("providerName").getNodeValue();
				
				if (cost > maxCost && provider.indexOf(PrivateCloud.BASE_PROVIDER_NAME) == -1) {
					maxCost = cost;
					selected = solutionResult;
				}
			}
			
			
			nl = ((Element)selected).getElementsByTagName("Tier");
			
			for (int i = 0; i < nl.getLength(); i++) {
				Node tier = nl.item(i);
				
				String tierName = tier.getAttributes().getNamedItem("name").getNodeValue();
					
				Element tierEl = (Element) tier;
				NodeList hours = tierEl.getElementsByTagName("HourAllocation");
				
				Integer[] machines = new Integer[24];
	
				for (int j = 0; j < hours.getLength(); j++) {
					Node hour = hours.item(j);
					int valHour = Integer.valueOf(hour.getAttributes()
							.getNamedItem("hour").getNodeValue()) + 1;
					
					hourlySolutions.addValue(
							Integer.valueOf(hour.getAttributes()
									.getNamedItem("allocation")
									.getNodeValue()), tierName, ""
									+ valHour);
					
					machines[j] = 0;
				}
				
				machinesOnPrivate.put(tierName + "@null", machines);
			}
			
			
		} else {
			nl = doc.getElementsByTagName("Tier");
			
			for (int i = 0; i < nl.getLength(); i++) {
				Node tier = nl.item(i);

				String size = tier.getAttributes().getNamedItem("resourceName")
						.getNodeValue();
				
				String provider = tier.getAttributes().getNamedItem("providerName")
						.getNodeValue();
				
				String tierName = tier.getAttributes().getNamedItem("name").getNodeValue();
				
				if (provider.indexOf(PrivateCloud.BASE_PROVIDER_NAME) == -1 &&
						machinesOnPrivate.containsKey(tierName + "@" + size)) {
					
					Element tierEl = (Element) tier;
					NodeList hours = tierEl.getElementsByTagName("HourAllocation");
		
					for (int j = 0; j < hours.getLength(); j++) {
						Node hour = hours.item(j);
						int valHour = Integer.valueOf(hour.getAttributes()
								.getNamedItem("hour").getNodeValue()) + 1;
						
						hourlySolutions.addValue(
								Integer.valueOf(hour.getAttributes()
										.getNamedItem("allocation")
										.getNodeValue()), tierName, ""
										+ valHour);
						
						if (valHour == maxHour) {
							int allocation = Integer.valueOf(hour.getAttributes()
									.getNamedItem("allocation")
									.getNodeValue()) +
									machinesOnPrivate.get(tierName + "@" + size)[valHour];
							
							solutions.setValue(
										allocation, /*"Tier " + i*/ tierName + "@" + provider, ""
												+ maxPopulation);
						}
					}
				}
			}
		}
		
		for (int h = 0; h < 24; ++h) {
			int hourlyValue = 0;
			for (String provider : usageHosts.keySet()) {
				if (usageHosts.get(provider)[h] > 0)
					hourlyValue++;
			}
			privateHosts.addValue(hourlyValue, name, "" + h);
			
			for (String tier : machinesOnPrivate.keySet())
				privateMachines.addValue(machinesOnPrivate.get(tier)[h], tier.substring(0, tier.indexOf('@')), "" + h);
		}

		nl = doc.getElementsByTagName("SolutionResult");

		if (nl.getLength() == 0)
			nl = doc.getElementsByTagName("SolutionMultiResult");

		if (nl.getLength() == 1) {
			Node solutionResult = nl.item(0);

			double cost = Double.parseDouble(solutionResult.getAttributes()
					.getNamedItem("cost").getNodeValue());
			boolean feasibility = Boolean
					.parseBoolean(solutionResult.getAttributes()
							.getNamedItem("feasibility").getNodeValue());
			long duration = Long.parseLong(solutionResult.getAttributes()
					.getNamedItem("time").getNodeValue());

			costs.addValue(cost, "Solution"/* name */, "" + "" + maxPopulation);
			feasibilities.addValue(feasibility ? 1 : 0, "Solution"/* name */, "" + maxPopulation);
			durations.addValue(duration, "Solution"/* name */, "" + "" + maxPopulation);
		}

		sortDataset(solutions);
		sortDataset(tiers);
		sortDataset(costs);
		sortDataset(feasibilities);
		sortDataset(durations);
		sortDataset(tiersBasic);
		
		privateHostsMap.put(maxPopulation, privateHosts);
		privateMachinesMap.put(maxPopulation, privateMachines);
		hourlySolutionsMap.put(maxPopulation, hourlySolutions);
		
		privateHostsGraphs.put(maxPopulation, null);
		privateMachinesGraphs.put(maxPopulation, null);
		hourlySolutionsGraphs.put(maxPopulation, null);
		
		JPanel privateHostsPanel = new JPanel();
		privateHostsTabs.addTab("Var " + maxPopulation, privateHostsPanel);
		JLabel privateHostsLabel = new JLabel();
		privateHostsLabel.setIcon(null);
		privateHostsPanel.add(privateHostsLabel);
		
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(2, 1, 0, 0));
		publicVsPrivateTabs.addTab("Var " + maxPopulation, lowerPanel);
		JPanel hourlySolutionsPanel = new JPanel();
		lowerPanel.add(hourlySolutionsPanel);
		JLabel hourlySolutionsLabel = new JLabel();
		hourlySolutionsLabel.setIcon(null);
		hourlySolutionsPanel.add(hourlySolutionsLabel);
		JPanel privateMachinesPanel = new JPanel();
		lowerPanel.add(privateMachinesPanel);
		JLabel privateMachinesLabel = new JLabel();
		privateMachinesLabel.setIcon(null);
		privateMachinesPanel.add(privateMachinesLabel);
		
		gui.validate();
		
		privateHostsPanels.put(maxPopulation, privateHostsPanel);
		privateHostsLabels.put(maxPopulation, privateHostsLabel);
		
		privateMachinesPanels.put(maxPopulation, privateMachinesPanel);
		privateMachinesLabels.put(maxPopulation, privateMachinesLabel);
		
		hourlySolutionsPanels.put(maxPopulation, hourlySolutionsPanel);
		hourlySolutionsLabels.put(maxPopulation, hourlySolutionsLabel);
		
		updateGraph();
		updateImages();
	}

	public int getValue() {
		return progressBar.getValue();
	}

	public void initialize() {
		gui = new JFrame();
		gui.setTitle("Robustness Progress");
		gui.setMinimumSize(new Dimension(900, 600));
		gui.setLocationRelativeTo(null);
//		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // .DISPOSE_ON_CLOSE);
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		gui.addWindowListener(this);
		gui.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel upperPanel = new JPanel();
		gui.getContentPane().add(upperPanel, BorderLayout.NORTH);
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));

		progressBar = new JProgressBar(0, total);
//		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		setValue(0);
		upperPanel.add(progressBar);

		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(/* 3 */2, 1, 0, 0));
		// gui.getContentPane().add(lowerPanel, BorderLayout.CENTER);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Solutions", lowerPanel);
		gui.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		// populationsPanel = new JPanel();
		// lowerPanel.add(populationsPanel);
		//
		// populationsLabel = new JLabel();
		// populationsLabel.setIcon(null);
		// populationsPanel.add(populationsLabel);

		// costsPanel = new JPanel();
		// lowerPanel.add(costsPanel);
		//
		// costsLabel = new JLabel();
		// costsLabel.setIcon(null);
		// costsPanel.add(costsLabel);

		solutionsPanel = new JPanel();
		lowerPanel.add(solutionsPanel);
		solutionsLabel = new JLabel();
		solutionsLabel.setIcon(null);
		solutionsPanel.add(solutionsLabel);

		tiersPanel = new JPanel();
		lowerPanel.add(tiersPanel);
		tiersLabel = new JLabel();
		tiersLabel.setIcon(null);
		tiersPanel.add(tiersLabel);

		lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(1, 1, 0, 0));
		tabbedPane.addTab("Allocations", lowerPanel);
		solutionsPanel2 = new JPanel();
		lowerPanel.add(solutionsPanel2);
		solutionsLabel2 = new JLabel();
		solutionsLabel2.setIcon(null);
		solutionsPanel2.add(solutionsLabel2);

		lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(1, 1, 0, 0));
		tabbedPane.addTab("Sizes", lowerPanel);
		tiersPanel2 = new JPanel();
		lowerPanel.add(tiersPanel2);
		tiersLabel2 = new JLabel();
		tiersLabel2.setIcon(null);
		tiersPanel2.add(tiersLabel2);

		lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(1, 1, 0, 0));
		tabbedPane.addTab("Basic Sizes", lowerPanel);
		tiersBasicPanel = new JPanel();
		lowerPanel.add(tiersBasicPanel);
		tiersBasicLabel = new JLabel();
		tiersBasicLabel.setIcon(null);
		tiersBasicPanel.add(tiersBasicLabel);

		lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(1, 1, 0, 0));
		tabbedPane.addTab("Costs", lowerPanel);
		costsPanel = new JPanel();
		lowerPanel.add(costsPanel);
		costsLabel = new JLabel();
		costsLabel.setIcon(null);
		costsPanel.add(costsLabel);

		lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(1, 1, 0, 0));
		tabbedPane.addTab("Workloads", lowerPanel);
		populationsPanel = new JPanel();
		lowerPanel.add(populationsPanel);
		populationsLabel = new JLabel();
		populationsLabel.setIcon(null);
		populationsPanel.add(populationsLabel);

		lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(1, 1, 0, 0));
		tabbedPane.addTab("Feasibilities", lowerPanel);
		feasibilitiesPanel = new JPanel();
		lowerPanel.add(feasibilitiesPanel);
		feasibilitiesLabel = new JLabel();
		feasibilitiesLabel.setIcon(null);
		feasibilitiesPanel.add(feasibilitiesLabel);
		
		lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(1, 1, 0, 0));
		tabbedPane.addTab("Durations", lowerPanel);
		durationsPanel = new JPanel();
		lowerPanel.add(durationsPanel);
		durationsLabel = new JLabel();
		durationsLabel.setIcon(null);
		durationsPanel.add(durationsLabel);
		
		lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(1, 1, 0, 0));
		tabbedPane.addTab("Private Hosts Used", lowerPanel);
		privateHostsTabs = new JTabbedPane();
		lowerPanel.add(privateHostsTabs);
		int tabNumber = tabbedPane.indexOfComponent(lowerPanel);
		tabbedPane.setEnabledAt(tabNumber, Configuration.USE_PRIVATE_CLOUD); // TODO: here
//		tabbedPane.setEnabledAt(tabNumber, true);
		
		lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(1, 1, 0, 0));
		tabbedPane.addTab("Private vs Public", lowerPanel);
		publicVsPrivateTabs = new JTabbedPane();
		lowerPanel.add(publicVsPrivateTabs);
		tabNumber = tabbedPane.indexOfComponent(lowerPanel);
		tabbedPane.setEnabledAt(tabNumber, Configuration.USE_PRIVATE_CLOUD); // TODO: here
//		tabbedPane.setEnabledAt(tabNumber, true);
		
//		lowerPanel = new JPanel();
//		lowerPanel.setLayout(new GridLayout(1, 1, 0, 0));
//		tabbedPane.addTab("Private Hosts Used", lowerPanel);
//		privateHostsPanel = new JPanel();
//		lowerPanel.add(privateHostsPanel);
//		privateHostsLabel = new JLabel();
//		privateHostsLabel.setIcon(null);
//		privateHostsPanel.add(privateHostsLabel);
//		int tabNumber = tabbedPane.indexOfComponent(lowerPanel);
//		tabbedPane.setEnabledAt(tabNumber, Configuration.USE_PRIVATE_CLOUD);
//		
//		lowerPanel = new JPanel();
//		lowerPanel.setLayout(new GridLayout(2, 1, 0, 0));
//		tabbedPane.addTab("Private vs Public", lowerPanel);
//		hourlySolutionsPanel = new JPanel();
//		lowerPanel.add(hourlySolutionsPanel);
//		hourlySolutionsLabel = new JLabel();
//		hourlySolutionsLabel.setIcon(null);
//		hourlySolutionsPanel.add(hourlySolutionsLabel);
//		privateMachinesPanel = new JPanel();
//		lowerPanel.add(privateMachinesPanel);
//		privateMachinesLabel = new JLabel();
//		privateMachinesLabel.setIcon(null);
//		privateMachinesPanel.add(privateMachinesLabel);
//		tabNumber = tabbedPane.indexOfComponent(lowerPanel);
//		tabbedPane.setEnabledAt(tabNumber, Configuration.USE_PRIVATE_CLOUD);
		
		// listener to resize images
		gui.addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent e) { }

			@Override
			public void componentMoved(ComponentEvent e) { }

			@Override
			public void componentResized(ComponentEvent e) {
				updateImages();
			}

			@Override
			public void componentShown(ComponentEvent e) {
				updateImages();
			}
		});

		gui.setVisible(true);
		updateImages();
	}

	public void save2png(String path) throws IOException {
		ChartUtilities.writeChartAsPNG(
				new FileOutputStream(Paths.get(path, "allocations.png")
						.toFile()), solutionsGraph, 1350, 700);
		ChartUtilities.writeChartAsPNG(
				new FileOutputStream(Paths.get(path, "populations.png")
						.toFile()), populationsGraph, 1350, 700);
		ChartUtilities.writeChartAsPNG(
				new FileOutputStream(Paths.get(path, "sizes.png").toFile()),
				tiersGraph, 1350, 700);
		ChartUtilities
				.writeChartAsPNG(
						new FileOutputStream(Paths.get(path, "basicSizes.png")
								.toFile()), tiersBasicGraph, 1350, 700);
		ChartUtilities.writeChartAsPNG(
				new FileOutputStream(Paths.get(path, "costs.png").toFile()),
				costsGraph, 1350, 700);
		ChartUtilities.writeChartAsPNG(
				new FileOutputStream(Paths.get(path, "feasibilities.png")
						.toFile()), feasibilitiesGraph, 1350, 700);
		ChartUtilities.writeChartAsPNG(
				new FileOutputStream(Paths.get(path, "durations.png")
						.toFile()), durationsGraph, 1350, 700);
		
		for (Integer key : privateHostsGraphs.keySet()) {
			JFreeChart privateHostsGraph = privateHostsGraphs.get(key);
			JFreeChart privateMachinesGraph = privateMachinesGraphs.get(key);
			JFreeChart hourlySolutionsGraph = hourlySolutionsGraphs.get(key);
			
			ChartUtilities.writeChartAsPNG(
					new FileOutputStream(Paths.get(path, key + "-privateHosts.png")
							.toFile()), privateHostsGraph, 1350, 700);
			ChartUtilities.writeChartAsPNG(
					new FileOutputStream(Paths.get(path, key + "-machinesOnPrivate.png")
							.toFile()), privateMachinesGraph, 1350, 700);
			ChartUtilities.writeChartAsPNG(
					new FileOutputStream(Paths.get(path, key + "-machinesOnPublic.png")
							.toFile()), hourlySolutionsGraph, 1350, 700);
		}
		
//		ChartUtilities.writeChartAsPNG(
//				new FileOutputStream(Paths.get(path, "privateHosts.png")
//						.toFile()), privateHostsGraph, 1350, 700);
//		ChartUtilities.writeChartAsPNG(
//				new FileOutputStream(Paths.get(path, "machinesOnPrivate.png")
//						.toFile()), privateMachinesGraph, 1350, 700);
//		ChartUtilities.writeChartAsPNG(
//				new FileOutputStream(Paths.get(path, "machinesOnPublic.png")
//						.toFile()), hourlySolutionsGraph, 1350, 700);
	}

	public void setValue(int value) {
		if (value > total)
			value = total;
		else if (value < 0)
			value = 0;
		progressBar.setValue(value);
		progressBar.setString(value + " out of " + total + " completed (" + Math.round(progressBar.getPercentComplete() * 100) + "%)");
	}

	@SuppressWarnings({ "deprecation" })
	public void updateGraph() {
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
		BasicStroke stroke = new BasicStroke(2.0f,                     // Line width
                							BasicStroke.CAP_ROUND,     // End-cap style
                							BasicStroke.JOIN_ROUND);   // Vertex join style

		populationsGraph = ChartFactory.createLineChart(null, "Hour",
				"Population", populations);
		{
			CategoryPlot plot = (CategoryPlot) populationsGraph.getPlot();
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
					.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.white);
			renderer.setStroke(stroke);

			CategoryAxis categoryAxis = plot.getDomainAxis();
			categoryAxis.setLowerMargin(0.02);
			categoryAxis.setUpperMargin(0.02);
			categoryAxis.setTickLabelFont(font);

			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setTickLabelFont(font);

		}

		solutionsGraph = ChartFactory.createLineChart(null, "Max Population",
				"Allocation", solutions, PlotOrientation.VERTICAL, true, true,
				false);
		{
			CategoryPlot plot = (CategoryPlot) solutionsGraph.getPlot();
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
					.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.white);
			renderer.setStroke(stroke);
			
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//			rangeAxis.setRange(0, rangeAxis.getRange().getUpperBound() * 1.1);
			rangeAxis.setTickLabelFont(font);
			
			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, tmp;
            for (int i = 0; i < solutions.getColumnCount(); ++i)
                for (int j = 0; j < solutions.getRowCount(); ++j) {
                    tmp = solutions.getValue(j, i).intValue();
                    if (tmp < min)
                        min = tmp;
                    if (tmp > max)
                        max = tmp;
                }
            if (min == Integer.MAX_VALUE)
                min = 0;
            if (max == Integer.MIN_VALUE)
                max = (int) rangeAxis.getRange().getUpperBound() + 1;

            rangeAxis.setRange(/* 0 */min - 0.5, /*
                                                 * rangeAxis.getRange().
                                                 * getUpperBound() + 1
                                                 */max + 0.5);

			CategoryAxis categoryAxis = plot.getDomainAxis();
			categoryAxis.setLowerMargin(0.02);
			categoryAxis.setUpperMargin(0.02);
			categoryAxis.setTickLabelFont(font);

			CategoryItemRenderer renderer2 = plot
					.getRenderer();
			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
					"{2}", new DecimalFormat("0"));
			renderer2.setItemLabelGenerator(generator);
			renderer2.setItemLabelsVisible(true);
			renderer2.setItemLabelFont(font);
		}

		tiersGraph = ChartFactory.createLineChart(null, "Max Population",
				"Size", tiers, PlotOrientation.VERTICAL, true, true, false);
		{
			CategoryPlot plot = (CategoryPlot) tiersGraph.getPlot();
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
					.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.white);
			renderer.setStroke(stroke);
			
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setTickLabelFont(font);

			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, tmp;
			for (int i = 0; i < tiers.getColumnCount(); ++i)
				for (int j = 0; j < tiers.getRowCount(); ++j) {
					tmp = tiers.getValue(j, i).intValue();
					if (tmp < min)
						min = tmp;
					if (tmp > max)
						max = tmp;
				}
			if (min == Integer.MAX_VALUE)
				min = 0;
			if (max == Integer.MIN_VALUE)
				max = (int) rangeAxis.getRange().getUpperBound() + 1;

			rangeAxis.setRange(/* 0 */min - 0.5, /*
												 * rangeAxis.getRange().
												 * getUpperBound() + 1
												 */max + 0.5);

			CategoryAxis categoryAxis = plot.getDomainAxis();
			categoryAxis.setLowerMargin(0.02);
			categoryAxis.setUpperMargin(0.02);
			categoryAxis.setTickLabelFont(font);

			CategoryItemRenderer renderer2 = plot
					.getRenderer();
			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
					"{2}", new DecimalFormat("0") {

						/**
                 *
                 */
						private static final long serialVersionUID = 1L;

						@Override
						public StringBuffer format(double number,
								StringBuffer result, FieldPosition fieldPosition) {
							// result = new
							// StringBuffer(Size.values()[(int)number].toString());
							result = new StringBuffer(
									Size.values()[(int) number].toString());
							return result;
						}

					});
			renderer2.setItemLabelGenerator(generator);
			renderer2.setItemLabelsVisible(true);
			renderer2.setItemLabelFont(font);
		}

		tiersBasicGraph = ChartFactory
				.createLineChart(null, "Max Population", "Size", tiersBasic,
						PlotOrientation.VERTICAL, true, true, false);
		{
			CategoryPlot plot = (CategoryPlot) tiersBasicGraph.getPlot();
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
					.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.white);
			renderer.setStroke(stroke);
			
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setTickLabelFont(font);

			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, tmp;
			for (int i = 0; i < tiersBasic.getColumnCount(); ++i)
				for (int j = 0; j < tiersBasic.getRowCount(); ++j) {
					tmp = tiersBasic.getValue(j, i).intValue();
					if (tmp < min)
						min = tmp;
					if (tmp > max)
						max = tmp;
				}
			if (min == Integer.MAX_VALUE)
				min = 0;
			if (max == Integer.MIN_VALUE)
				max = (int) rangeAxis.getRange().getUpperBound() + 1;

			rangeAxis.setRange(/* 0 */min - 0.5, /*
												 * rangeAxis.getRange().
												 * getUpperBound() + 1
												 */max + 0.5);

			CategoryAxis categoryAxis = plot.getDomainAxis();
			categoryAxis.setLowerMargin(0.02);
			categoryAxis.setUpperMargin(0.02);
			categoryAxis.setTickLabelFont(font);

			CategoryItemRenderer renderer2 = plot
					.getRenderer();
			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
					"{2}", new DecimalFormat("0") {

						/**
                 *
                 */
						private static final long serialVersionUID = 1L;

						@Override
						public StringBuffer format(double number,
								StringBuffer result, FieldPosition fieldPosition) {
							// result = new
							// StringBuffer(BasicSize.values()[(int)number].toString());
							result = new StringBuffer(Size
									.getSizeByBasicId((int) number).basicName);
							return result;
						}

					});
			renderer2.setItemLabelGenerator(generator);
			renderer2.setItemLabelsVisible(true);
			renderer2.setItemLabelFont(font);
		}

		costsGraph = ChartFactory.createLineChart(null, "Max Population",
				"Cost", costs, PlotOrientation.VERTICAL, true, true, false);
		{
			CategoryPlot plot = (CategoryPlot) costsGraph.getPlot();
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
					.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.white);
			renderer.setStroke(stroke);

			CategoryAxis categoryAxis = plot.getDomainAxis();
			categoryAxis.setLowerMargin(0.02);
			categoryAxis.setUpperMargin(0.02);
			categoryAxis.setTickLabelFont(font);

			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setTickLabelFont(font);

			CategoryItemRenderer renderer2 = plot
					.getRenderer();
			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
					"{2}", new DecimalFormat("0") {

						/**
                 *
                 */
						private static final long serialVersionUID = 1L;

						@Override
						public StringBuffer format(double number,
								StringBuffer result, FieldPosition fieldPosition) {
							result = new StringBuffer(SolutionMulti.costFormatter.format(number)); // "" + number);
							return result;
						}

					});
			renderer2.setItemLabelGenerator(generator);
			renderer2.setItemLabelsVisible(true);
			renderer2.setItemLabelFont(font);
		}

		feasibilitiesGraph = ChartFactory.createLineChart(null,
				"Max Population", "Feasible", feasibilities,
				PlotOrientation.VERTICAL, true, true, false);
		{
			CategoryPlot plot = (CategoryPlot) feasibilitiesGraph.getPlot();
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
					.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.white);
			renderer.setStroke(stroke);
			
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setTickLabelFont(font);
			rangeAxis.setRange(-0.5, 1.5);

			CategoryAxis categoryAxis = plot.getDomainAxis();
			categoryAxis.setLowerMargin(0.02);
			categoryAxis.setUpperMargin(0.02);
			categoryAxis.setTickLabelFont(font);

			CategoryItemRenderer renderer2 = plot
					.getRenderer();
			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
					"{2}", new DecimalFormat("0") {

						/**
                 *
                 */
						private static final long serialVersionUID = 1L;

						@Override
						public StringBuffer format(double number,
								StringBuffer result, FieldPosition fieldPosition) {
							result = new StringBuffer(number == 1 ? "true"
									: "false");
							return result;
						}

					});
			renderer2.setItemLabelGenerator(generator);
			renderer2.setItemLabelsVisible(true);
			renderer2.setItemLabelFont(font);
		}
		
		durationsGraph = ChartFactory.createLineChart(null, "Max Population",
				"Duration", durations, PlotOrientation.VERTICAL, true, true, false);
		{
			CategoryPlot plot = (CategoryPlot) durationsGraph.getPlot();
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
					.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.white);
			renderer.setStroke(stroke);

			CategoryAxis categoryAxis = plot.getDomainAxis();
			categoryAxis.setLowerMargin(0.02);
			categoryAxis.setUpperMargin(0.02);
			categoryAxis.setTickLabelFont(font);

			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setTickLabelFont(font);

			CategoryItemRenderer renderer2 = plot
					.getRenderer();
			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
					"{2}", new DecimalFormat("0") {

						/**
                 *
                 */
						private static final long serialVersionUID = 1L;

						@Override
						public StringBuffer format(double number,
								StringBuffer result, FieldPosition fieldPosition) {
							result = new StringBuffer(Space4Cloud.durationToString((long)number));  //"" + number);
							return result;
						}

					});
			renderer2.setItemLabelGenerator(generator);
			renderer2.setItemLabelsVisible(true);
			renderer2.setItemLabelFont(font);
		}
		
		for (Integer key : privateHostsMap.keySet()) {
			DefaultCategoryDataset privateHosts = privateHostsMap.get(key);
			JFreeChart privateHostsGraph = ChartFactory.createLineChart(null, "Hour",
					"Private Hosts", privateHosts, PlotOrientation.VERTICAL, true, true, false);
			{
				CategoryPlot plot = (CategoryPlot) privateHostsGraph.getPlot();
				LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
						.getRenderer();
				renderer.setShapesVisible(true);
				renderer.setDrawOutlines(true);
				renderer.setUseFillPaint(true);
				renderer.setFillPaint(Color.white);
				renderer.setStroke(stroke);

				CategoryAxis categoryAxis = plot.getDomainAxis();
				categoryAxis.setLowerMargin(0.02);
				categoryAxis.setUpperMargin(0.02);
				categoryAxis.setTickLabelFont(font);

				NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
				rangeAxis.setTickLabelFont(font);
				
				int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, tmp;
	            for (int i = 0; i < privateHosts.getColumnCount(); ++i)
	                for (int j = 0; j < privateHosts.getRowCount(); ++j) {
	                    tmp = privateHosts.getValue(j, i).intValue();
	                    if (tmp < min)
	                        min = tmp;
	                    if (tmp > max)
	                        max = tmp;
	                }
	            if (min == Integer.MAX_VALUE)
	                min = 0;
	            if (max == Integer.MIN_VALUE)
	                max = (int) rangeAxis.getRange().getUpperBound() + 1;

	            rangeAxis.setRange(/* 0 */min - 0.5, /*
	                                                 * rangeAxis.getRange().
	                                                 * getUpperBound() + 1
	                                                 */max + 0.5);

				CategoryItemRenderer renderer2 = plot
						.getRenderer();
				CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
						"{2}", new DecimalFormat("0") {

							/**
	                 *
	                 */
							private static final long serialVersionUID = 1L;

							@Override
							public StringBuffer format(double number,
									StringBuffer result, FieldPosition fieldPosition) {
								result = new StringBuffer((int)number + "");
								return result;
							}

						});
				renderer2.setItemLabelGenerator(generator);
				renderer2.setItemLabelsVisible(true);
				renderer2.setItemLabelFont(font);
			}
			privateHostsGraphs.put(key, privateHostsGraph);
			
			DefaultCategoryDataset hourlySolutions = hourlySolutionsMap.get(key);
			JFreeChart hourlySolutionsGraph = ChartFactory.createLineChart(null, "Hour",
					"Machines on Public", hourlySolutions);
			{
				CategoryPlot plot = (CategoryPlot) hourlySolutionsGraph.getPlot();
				LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
						.getRenderer();
				renderer.setShapesVisible(true);
				renderer.setDrawOutlines(true);
				renderer.setUseFillPaint(true);
				renderer.setFillPaint(Color.white);
				renderer.setStroke(stroke);

				CategoryAxis categoryAxis = plot.getDomainAxis();
				categoryAxis.setLowerMargin(0.02);
				categoryAxis.setUpperMargin(0.02);
				categoryAxis.setTickLabelFont(font);

				NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
				rangeAxis.setTickLabelFont(font);
				
				int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, tmp;
	            for (int i = 0; i < hourlySolutions.getColumnCount(); ++i)
	                for (int j = 0; j < hourlySolutions.getRowCount(); ++j) {
	                    tmp = hourlySolutions.getValue(j, i).intValue();
	                    if (tmp < min)
	                        min = tmp;
	                    if (tmp > max)
	                        max = tmp;
	                }
	            if (min == Integer.MAX_VALUE)
	                min = 0;
	            if (max == Integer.MIN_VALUE)
	                max = (int) rangeAxis.getRange().getUpperBound() + 1;

	            rangeAxis.setRange(/* 0 */min - 0.5, /*
	                                                 * rangeAxis.getRange().
	                                                 * getUpperBound() + 1
	                                                 */max + 0.5);

				CategoryItemRenderer renderer2 = plot
						.getRenderer();
				CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
						"{2}", new DecimalFormat("0"));
				renderer2.setItemLabelGenerator(generator);
				renderer2.setItemLabelsVisible(true);
				renderer2.setItemLabelFont(font);
			}
			hourlySolutionsGraphs.put(key, hourlySolutionsGraph);
			
			DefaultCategoryDataset privateMachines = privateMachinesMap.get(key);
			JFreeChart privateMachinesGraph = ChartFactory.createLineChart(null, "Hour",
					"Machines on Private", privateMachines);
			{
				CategoryPlot plot = (CategoryPlot) privateMachinesGraph.getPlot();
				LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
						.getRenderer();
				renderer.setShapesVisible(true);
				renderer.setDrawOutlines(true);
				renderer.setUseFillPaint(true);
				renderer.setFillPaint(Color.white);
				renderer.setStroke(stroke);

				CategoryAxis categoryAxis = plot.getDomainAxis();
				categoryAxis.setLowerMargin(0.02);
				categoryAxis.setUpperMargin(0.02);
				categoryAxis.setTickLabelFont(font);

				NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
				rangeAxis.setTickLabelFont(font);
				
				int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, tmp;
	            for (int i = 0; i < privateMachines.getColumnCount(); ++i)
	                for (int j = 0; j < privateMachines.getRowCount(); ++j) {
	                    tmp = privateMachines.getValue(j, i).intValue();
	                    if (tmp < min)
	                        min = tmp;
	                    if (tmp > max)
	                        max = tmp;
	                }
	            if (min == Integer.MAX_VALUE)
	                min = 0;
	            if (max == Integer.MIN_VALUE)
	                max = (int) rangeAxis.getRange().getUpperBound() + 1;

	            rangeAxis.setRange(/* 0 */min - 0.5, /*
	                                                 * rangeAxis.getRange().
	                                                 * getUpperBound() + 1
	                                                 */max + 0.5);

				CategoryItemRenderer renderer2 = plot
						.getRenderer();
				CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
						"{2}", new DecimalFormat("0"));
				renderer2.setItemLabelGenerator(generator);
				renderer2.setItemLabelsVisible(true);
				renderer2.setItemLabelFont(font);
			}
			privateMachinesGraphs.put(key, privateMachinesGraph);
		}
		
//		privateHostsGraph = ChartFactory.createLineChart(null, "Hour",
//				"Private Hosts", privateHosts, PlotOrientation.VERTICAL, true, true, false);
//		{
//			CategoryPlot plot = (CategoryPlot) privateHostsGraph.getPlot();
//			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
//					.getRenderer();
//			renderer.setShapesVisible(true);
//			renderer.setDrawOutlines(true);
//			renderer.setUseFillPaint(true);
//			renderer.setFillPaint(Color.white);
//
//			CategoryAxis categoryAxis = plot.getDomainAxis();
//			categoryAxis.setLowerMargin(0.02);
//			categoryAxis.setUpperMargin(0.02);
//			categoryAxis.setTickLabelFont(font);
//
//			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//			rangeAxis.setTickLabelFont(font);
//			
//			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, tmp;
//            for (int i = 0; i < privateHosts.getColumnCount(); ++i)
//                for (int j = 0; j < privateHosts.getRowCount(); ++j) {
//                    tmp = privateHosts.getValue(j, i).intValue();
//                    if (tmp < min)
//                        min = tmp;
//                    if (tmp > max)
//                        max = tmp;
//                }
//            if (min == Integer.MAX_VALUE)
//                min = 0;
//            if (max == Integer.MIN_VALUE)
//                max = (int) rangeAxis.getRange().getUpperBound() + 1;
//
//            rangeAxis.setRange(/* 0 */min - 0.5, /*
//                                                 * rangeAxis.getRange().
//                                                 * getUpperBound() + 1
//                                                 */max + 0.5);
//
//			CategoryItemRenderer renderer2 = plot
//					.getRenderer();
//			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
//					"{2}", new DecimalFormat("0") {
//
//						/**
//                 *
//                 */
//						private static final long serialVersionUID = 1L;
//
//						@Override
//						public StringBuffer format(double number,
//								StringBuffer result, FieldPosition fieldPosition) {
//							result = new StringBuffer((int)number + "");
//							return result;
//						}
//
//					});
//			renderer2.setItemLabelGenerator(generator);
//			renderer2.setItemLabelsVisible(true);
//			renderer2.setItemLabelFont(font);
//		}
//		
//		hourlySolutionsGraph = ChartFactory.createLineChart(null, "Hour",
//				"Machines on Public", hourlySolutions);
//		{
//			CategoryPlot plot = (CategoryPlot) hourlySolutionsGraph.getPlot();
//			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
//					.getRenderer();
//			renderer.setShapesVisible(true);
//			renderer.setDrawOutlines(true);
//			renderer.setUseFillPaint(true);
//			renderer.setFillPaint(Color.white);
//
//			CategoryAxis categoryAxis = plot.getDomainAxis();
//			categoryAxis.setLowerMargin(0.02);
//			categoryAxis.setUpperMargin(0.02);
//			categoryAxis.setTickLabelFont(font);
//
//			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//			rangeAxis.setTickLabelFont(font);
//			
//			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, tmp;
//            for (int i = 0; i < hourlySolutions.getColumnCount(); ++i)
//                for (int j = 0; j < hourlySolutions.getRowCount(); ++j) {
//                    tmp = hourlySolutions.getValue(j, i).intValue();
//                    if (tmp < min)
//                        min = tmp;
//                    if (tmp > max)
//                        max = tmp;
//                }
//            if (min == Integer.MAX_VALUE)
//                min = 0;
//            if (max == Integer.MIN_VALUE)
//                max = (int) rangeAxis.getRange().getUpperBound() + 1;
//
//            rangeAxis.setRange(/* 0 */min - 0.5, /*
//                                                 * rangeAxis.getRange().
//                                                 * getUpperBound() + 1
//                                                 */max + 0.5);
//
//			CategoryItemRenderer renderer2 = plot
//					.getRenderer();
//			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
//					"{2}", new DecimalFormat("0"));
//			renderer2.setItemLabelGenerator(generator);
//			renderer2.setItemLabelsVisible(true);
//			renderer2.setItemLabelFont(font);
//		}
//		
//		privateMachinesGraph = ChartFactory.createLineChart(null, "Hour",
//				"Machines on Private", privateMachines);
//		{
//			CategoryPlot plot = (CategoryPlot) privateMachinesGraph.getPlot();
//			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
//					.getRenderer();
//			renderer.setShapesVisible(true);
//			renderer.setDrawOutlines(true);
//			renderer.setUseFillPaint(true);
//			renderer.setFillPaint(Color.white);
//
//			CategoryAxis categoryAxis = plot.getDomainAxis();
//			categoryAxis.setLowerMargin(0.02);
//			categoryAxis.setUpperMargin(0.02);
//			categoryAxis.setTickLabelFont(font);
//
//			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//			rangeAxis.setTickLabelFont(font);
//			
//			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, tmp;
//            for (int i = 0; i < privateMachines.getColumnCount(); ++i)
//                for (int j = 0; j < privateMachines.getRowCount(); ++j) {
//                    tmp = privateMachines.getValue(j, i).intValue();
//                    if (tmp < min)
//                        min = tmp;
//                    if (tmp > max)
//                        max = tmp;
//                }
//            if (min == Integer.MAX_VALUE)
//                min = 0;
//            if (max == Integer.MIN_VALUE)
//                max = (int) rangeAxis.getRange().getUpperBound() + 1;
//
//            rangeAxis.setRange(/* 0 */min - 0.5, /*
//                                                 * rangeAxis.getRange().
//                                                 * getUpperBound() + 1
//                                                 */max + 0.5);
//
//			CategoryItemRenderer renderer2 = plot
//					.getRenderer();
//			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
//					"{2}", new DecimalFormat("0"));
//			renderer2.setItemLabelGenerator(generator);
//			renderer2.setItemLabelsVisible(true);
//			renderer2.setItemLabelFont(font);
//		}

	}

	private void updateImages() {
		if (alreadyUpdating)
			return;
		alreadyUpdating = true;

		if (populationsGraph != null) {
			ImageIcon icon;
			try {
				icon = new ImageIcon(populationsGraph.createBufferedImage(
						populationsPanel.getSize().width,
						populationsPanel.getSize().height));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
			}
			populationsLabel.setIcon(icon);
			populationsLabel.setVisible(true);
			populationsPanel.setPreferredSize(populationsLabel
					.getPreferredSize());

			populationsLabel.validate();
		}

		if (costsGraph != null) {
			ImageIcon icon;
			try {
				icon = new ImageIcon(
						costsGraph.createBufferedImage(
								costsPanel.getSize().width,
								costsPanel.getSize().height));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
			}
			costsLabel.setIcon(icon);
			costsLabel.setVisible(true);
			costsPanel.setPreferredSize(costsLabel.getPreferredSize());

			costsLabel.validate();
		}

		if (solutionsGraph != null) {
			ImageIcon icon, icon2;
			try {
				icon = new ImageIcon(solutionsGraph.createBufferedImage(
						solutionsPanel.getSize().width,
						solutionsPanel.getSize().height));
				icon2 = new ImageIcon(solutionsGraph.createBufferedImage(
						solutionsPanel2.getSize().width,
						solutionsPanel2.getSize().height));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
				icon2 = new ImageIcon();
			}
			solutionsLabel.setIcon(icon);
			solutionsLabel.setVisible(true);
			solutionsPanel.setPreferredSize(solutionsLabel.getPreferredSize());

			solutionsLabel.validate();

			solutionsLabel2.setIcon(icon2);
			solutionsLabel2.setVisible(true);
			solutionsPanel2
					.setPreferredSize(solutionsLabel2.getPreferredSize());

			solutionsLabel2.validate();
		}

		if (tiers != null) {
			ImageIcon icon, icon2;
			try {
				icon = new ImageIcon(
						tiersGraph.createBufferedImage(
								tiersPanel.getSize().width,
								tiersPanel.getSize().height));
				icon2 = new ImageIcon(tiersGraph.createBufferedImage(
						tiersPanel2.getSize().width,
						tiersPanel2.getSize().height));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
				icon2 = new ImageIcon();
			}
			tiersLabel.setIcon(icon);
			tiersLabel.setVisible(true);
			tiersPanel.setPreferredSize(tiersLabel.getPreferredSize());

			tiersLabel.validate();

			tiersLabel2.setIcon(icon2);
			tiersLabel2.setVisible(true);
			tiersPanel2.setPreferredSize(tiersLabel2.getPreferredSize());

			tiersLabel2.validate();
		}

		if (tiersBasic != null) {
			ImageIcon icon;
			try {
				icon = new ImageIcon(tiersBasicGraph.createBufferedImage(
						tiersBasicPanel.getSize().width,
						tiersBasicPanel.getSize().height));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
			}
			tiersBasicLabel.setIcon(icon);
			tiersBasicLabel.setVisible(true);
			tiersBasicPanel
					.setPreferredSize(tiersBasicLabel.getPreferredSize());

			tiersBasicLabel.validate();
		}

		if (feasibilities != null) {
			ImageIcon icon;
			try {
				icon = new ImageIcon(feasibilitiesGraph.createBufferedImage(
						feasibilitiesPanel.getSize().width,
						feasibilitiesPanel.getSize().height));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
			}
			feasibilitiesLabel.setIcon(icon);
			feasibilitiesLabel.setVisible(true);
			feasibilitiesPanel.setPreferredSize(feasibilitiesLabel
					.getPreferredSize());

			feasibilitiesLabel.validate();
		}
		
		if (durationsGraph != null) {
			ImageIcon icon;
			try {
				icon = new ImageIcon(
						durationsGraph.createBufferedImage(
								durationsPanel.getSize().width,
								durationsPanel.getSize().height));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
			}
			durationsLabel.setIcon(icon);
			durationsLabel.setVisible(true);
			durationsPanel.setPreferredSize(durationsLabel.getPreferredSize());

			durationsLabel.validate();
		}
		
		for (Integer key : privateHostsGraphs.keySet()) {
			JFreeChart privateHostsGraph = privateHostsGraphs.get(key);
			if (privateHostsGraph != null) {
				JPanel privateHostsPanel = privateHostsPanels.get(key);
				JLabel privateHostsLabel = privateHostsLabels.get(key);
				ImageIcon icon;
				try {
					icon = new ImageIcon(
							privateHostsGraph.createBufferedImage(
									privateHostsPanel.getSize().width,
									privateHostsPanel.getSize().height));
				} catch (NullPointerException e) {
					icon = new ImageIcon();
				}
				privateHostsLabel.setIcon(icon);
				privateHostsLabel.setVisible(true);
				privateHostsPanel.setPreferredSize(privateHostsLabel.getPreferredSize());

				privateHostsLabel.validate();
			}
			
			JFreeChart privateMachinesGraph = privateMachinesGraphs.get(key);
			if (privateMachinesGraph != null) {
				JPanel privateMachinesPanel = privateMachinesPanels.get(key);
				JLabel privateMachinesLabel = privateMachinesLabels.get(key);
				ImageIcon icon;
				try {
					icon = new ImageIcon(
							privateMachinesGraph.createBufferedImage(
									privateMachinesPanel.getSize().width,
									privateMachinesPanel.getSize().height));
				} catch (NullPointerException e) {
					icon = new ImageIcon();
				}
				privateMachinesLabel.setIcon(icon);
				privateMachinesLabel.setVisible(true);
				privateMachinesPanel.setPreferredSize(privateMachinesLabel.getPreferredSize());

				privateMachinesLabel.validate();
			}
			
			JFreeChart hourlySolutionsGraph = hourlySolutionsGraphs.get(key);
			if (hourlySolutionsGraph != null) {
				JPanel hourlySolutionsPanel = hourlySolutionsPanels.get(key);
				JLabel hourlySolutionsLabel = hourlySolutionsLabels.get(key);
				ImageIcon icon;
				try {
					icon = new ImageIcon(
							hourlySolutionsGraph.createBufferedImage(
									hourlySolutionsPanel.getSize().width,
									hourlySolutionsPanel.getSize().height));
				} catch (NullPointerException e) {
					icon = new ImageIcon();
				}
				hourlySolutionsLabel.setIcon(icon);
				hourlySolutionsLabel.setVisible(true);
				hourlySolutionsPanel.setPreferredSize(hourlySolutionsLabel.getPreferredSize());

				hourlySolutionsLabel.validate();
			}
		}
		
//		if (privateHostsGraph != null) {
//			ImageIcon icon;
//			try {
//				icon = new ImageIcon(
//						privateHostsGraph.createBufferedImage(
//								privateHostsPanel.getSize().width,
//								privateHostsPanel.getSize().height));
//			} catch (NullPointerException e) {
//				icon = new ImageIcon();
//			}
//			privateHostsLabel.setIcon(icon);
//			privateHostsLabel.setVisible(true);
//			privateHostsPanel.setPreferredSize(privateHostsLabel.getPreferredSize());
//
//			privateHostsLabel.validate();
//		}
//		
//		if (privateMachinesGraph != null) {
//			ImageIcon icon;
//			try {
//				icon = new ImageIcon(
//						privateMachinesGraph.createBufferedImage(
//								privateMachinesPanel.getSize().width,
//								privateMachinesPanel.getSize().height));
//			} catch (NullPointerException e) {
//				icon = new ImageIcon();
//			}
//			privateMachinesLabel.setIcon(icon);
//			privateMachinesLabel.setVisible(true);
//			privateMachinesPanel.setPreferredSize(privateMachinesLabel.getPreferredSize());
//
//			privateMachinesLabel.validate();
//		}
//		
//		if (hourlySolutionsGraph != null) {
//			ImageIcon icon;
//			try {
//				icon = new ImageIcon(
//						hourlySolutionsGraph.createBufferedImage(
//								hourlySolutionsPanel.getSize().width,
//								hourlySolutionsPanel.getSize().height));
//			} catch (NullPointerException e) {
//				icon = new ImageIcon();
//			}
//			hourlySolutionsLabel.setIcon(icon);
//			hourlySolutionsLabel.setVisible(true);
//			hourlySolutionsPanel.setPreferredSize(hourlySolutionsLabel.getPreferredSize());
//
//			hourlySolutionsLabel.validate();
//		}

		alreadyUpdating = false;
	}
	
	@Override
	public void windowClosing(WindowEvent e) {		
		super.windowClosing(e);
		gui.dispose();
		pcs.firePropertyChange("WindowClosed", false, true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) { }
	
	public void addPropertyChangeListener(PropertyChangeListener listener){
		pcs.addPropertyChangeListener(listener);
	}
	
	public void signalCompletion() {
		JOptionPane.showMessageDialog(gui, "Robustness process completed");
	}
	
	public void testEnded() {
		pcs.firePropertyChange("RobustnessEnded", false, true);
	}

}