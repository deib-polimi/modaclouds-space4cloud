package it.polimi.modaclouds.space4cloud.gui;

import it.polimi.modaclouds.qos_models.schema.ClosedWorkload;
import it.polimi.modaclouds.qos_models.schema.ClosedWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.OpenWorkload;
import it.polimi.modaclouds.qos_models.schema.OpenWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtensions;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.chart.GenericChart;
import it.polimi.modaclouds.space4cloud.mainProgram.Space4Cloud;
import it.polimi.modaclouds.space4cloud.optimization.bursting.PrivateCloud;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.DOM;

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
import javax.swing.JFrame;
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
		DefaultCategoryDataset costs1 = rpw1.costs.dataset, costs2 = rpw2.costs.dataset;
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
	
	private GenericChart<DefaultCategoryDataset> populations;
	private GenericChart<DefaultCategoryDataset> solutions;
	private GenericChart<DefaultCategoryDataset> solutions2;
	private GenericChart<DefaultCategoryDataset> tiers;
	private GenericChart<DefaultCategoryDataset> tiers2;
	private GenericChart<DefaultCategoryDataset> tiersBasic;
	private GenericChart<DefaultCategoryDataset> feasibilities;
	private GenericChart<DefaultCategoryDataset> costs;
	private GenericChart<DefaultCategoryDataset> durations;
	
	private JTabbedPane privateHostsTabs;
	private JTabbedPane publicVsPrivateTabs;
	
	private HashMap<Integer, GenericChart<DefaultCategoryDataset>> privateHostsMap = new HashMap<Integer, GenericChart<DefaultCategoryDataset>>();
	private HashMap<Integer, GenericChart<DefaultCategoryDataset>> privateMachinesMap = new HashMap<Integer, GenericChart<DefaultCategoryDataset>>();
	private HashMap<Integer, GenericChart<DefaultCategoryDataset>> hourlySolutionsMap = new HashMap<Integer, GenericChart<DefaultCategoryDataset>>();
	
	private int total;

	private boolean alreadyUpdating = false;
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public RobustnessProgressWindow(int total) {
		this.total = total;
		
		populations = new GenericChart<DefaultCategoryDataset>(null, "Hour", "Population");
		populations.dataset = new DefaultCategoryDataset();
		populations.defaultRange = true;
		populations.labelsVisible = false;
		
		solutions = new GenericChart<DefaultCategoryDataset>(null, "Max Population", "Allocation");
		solutions2 = new GenericChart<DefaultCategoryDataset>(null, "Max Population", "Allocation");
		solutions.dataset = new DefaultCategoryDataset();
		solutions2.dataset = new DefaultCategoryDataset();
		solutions.shownValsAboveMax = 0.5;
		solutions.shownValsBelowMin = 0.5;
		solutions2.shownValsAboveMax = 0.5;
		solutions2.shownValsBelowMin = 0.5;
		
		tiers = new GenericChart<DefaultCategoryDataset>(null, "Max Population", "Size") {
			private static final long serialVersionUID = -1343675992648561182L;
			@Override
			public String getFormattedValue(double number) {
				return Size.values()[(int) number].toString();
			}
		};
		tiers2 = new GenericChart<DefaultCategoryDataset>(null, "Max Population", "Size") {
			private static final long serialVersionUID = 8248502224039989846L;
			@Override
			public String getFormattedValue(double number) {
				return Size.values()[(int) number].toString();
			}
		};
		tiers.dataset = new DefaultCategoryDataset();
		tiers2.dataset = new DefaultCategoryDataset();
		tiers.shownValsAboveMax = 0.5;
		tiers.shownValsBelowMin = 0.5;
		tiers2.shownValsAboveMax = 0.5;
		tiers2.shownValsBelowMin = 0.5;
		
		tiersBasic = new GenericChart<DefaultCategoryDataset>(null, "Max Population", "Size") {
			private static final long serialVersionUID = -8479547878075995404L;
			@Override
			public String getFormattedValue(double number) {
				return Size.getSizeByBasicId((int) number).basicName;
			}
		};
		tiersBasic.dataset = new DefaultCategoryDataset();
		tiersBasic.shownValsAboveMax = 0.5;
		tiersBasic.shownValsBelowMin = 0.5;
		
		feasibilities = new GenericChart<DefaultCategoryDataset>(null, "Max Population", "Feasible") {
			private static final long serialVersionUID = 7630291006516941825L;
			@Override
			public String getFormattedValue(double number) {
				return number == 1 ? "true" : "false";
			}
		};
		feasibilities.dataset = new DefaultCategoryDataset();
		feasibilities.exactMax = 1.5;
		feasibilities.exactMin = -0.5;
		
		costs = new GenericChart<DefaultCategoryDataset>(null, "Max Population", "Cost") {
			private static final long serialVersionUID = 7630291006516941825L;
			@Override
			public String getFormattedValue(double number) {
				return SolutionMulti.costFormatter.format(number);
			}
		};
		costs.dataset = new DefaultCategoryDataset();
		costs.defaultRange = true;
		
		durations = new GenericChart<DefaultCategoryDataset>(null, "Max Population", "Duration") {
			private static final long serialVersionUID = 3746090624375537494L;
			@Override
			public String getFormattedValue(double number) {
				return Space4Cloud.durationToString((long)number);
			}
		};
		durations.dataset = new DefaultCategoryDataset();
		durations.defaultRange = true;

		updateGraph();

		initialize();
	}

	public void add(File usageModelExtension, File solution)
			throws MalformedURLException, JAXBException, SAXException {
		int maxPopulation = Space4Cloud.getMaxPopulation(usageModelExtension);
		int maxHour = -1;
		
		GenericChart<DefaultCategoryDataset> privateHosts = new GenericChart<DefaultCategoryDataset>(null, "Hour", "Private Hosts");
		privateHosts.dataset = new DefaultCategoryDataset();
		privateHosts.shownValsAboveMax = 0.5;
		privateHosts.shownValsBelowMin = 0.5;
		
		GenericChart<DefaultCategoryDataset> privateMachines = new GenericChart<DefaultCategoryDataset>(null, "Hour", "Machines on Private");
		privateMachines.dataset = new DefaultCategoryDataset();
		privateMachines.shownValsAboveMax = 0.5;
		privateMachines.shownValsBelowMin = 0.5;
		
		GenericChart<DefaultCategoryDataset> hourlySolutions = new GenericChart<DefaultCategoryDataset>(null, "Hour", "Machines on Public");
		hourlySolutions.dataset = new DefaultCategoryDataset();
		hourlySolutions.shownValsAboveMax = 0.5;
		hourlySolutions.shownValsBelowMin = 0.5;
		
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
					
					populations.dataset.addValue(we.getPopulation(), name,
							"" + we.getHour());
				}
			} else {
	
				OpenWorkload ow = umes.getUsageModelExtension().getOpenWorkload();
				if (ow != null) {
					for (OpenWorkloadElement we : ow.getWorkloadElement()) {
						if (maxPopulation == we.getPopulation())
							maxHour = we.getHour();
						
						populations.dataset.addValue(we.getPopulation(), name,
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
				
	
				tiers.dataset.addValue(s.ordinal(), /*"Tier " + i*/ tierName, "" + maxPopulation);
				tiers2.dataset.addValue(s.ordinal(), /*"Tier " + i*/ tierName, "" + maxPopulation);
				tiersBasic.dataset.addValue(s.basicId, /*"Tier " + i*/ tierName, "" + maxPopulation);
	
				Element tierEl = (Element) tier;
				NodeList hours = tierEl.getElementsByTagName("HourAllocation");
	
				for (int j = 0; j < hours.getLength(); j++) {
					Node hour = hours.item(j);
					int valHour = Integer.valueOf(hour.getAttributes()
							.getNamedItem("hour").getNodeValue()) + 1;
	
					if (valHour == maxHour) {
						solutions.dataset
								.addValue(
										Integer.valueOf(hour.getAttributes()
												.getNamedItem("allocation")
												.getNodeValue()), /*"Tier " + i*/ tierName, ""
												+ maxPopulation);
						solutions2.dataset
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
					
					hourlySolutions.dataset.addValue(
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
						
						hourlySolutions.dataset.addValue(
								Integer.valueOf(hour.getAttributes()
										.getNamedItem("allocation")
										.getNodeValue()), tierName, ""
										+ valHour);
						
						if (valHour == maxHour) {
							int allocation = Integer.valueOf(hour.getAttributes()
									.getNamedItem("allocation")
									.getNodeValue()) +
									machinesOnPrivate.get(tierName + "@" + size)[valHour];
							
							solutions.dataset.setValue(
										allocation, /*"Tier " + i*/ tierName + "@" + provider, ""
												+ maxPopulation);
							solutions2.dataset.setValue(
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
			privateHosts.dataset.addValue(hourlyValue, name, "" + h);
			
			for (String tier : machinesOnPrivate.keySet())
				privateMachines.dataset.addValue(machinesOnPrivate.get(tier)[h], tier.substring(0, tier.indexOf('@')), "" + h);
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

			costs.dataset.addValue(cost, "Solution"/* name */, "" + "" + maxPopulation);
			feasibilities.dataset.addValue(feasibility ? 1 : 0, "Solution"/* name */, "" + maxPopulation);
			durations.dataset.addValue(duration, "Solution"/* name */, "" + "" + maxPopulation);
		}

		sortDataset(solutions.dataset);
		sortDataset(tiers.dataset);
		sortDataset(costs.dataset);
		sortDataset(feasibilities.dataset);
		sortDataset(durations.dataset);
		sortDataset(tiersBasic.dataset);
		
		privateHostsMap.put(maxPopulation, privateHosts);
		privateMachinesMap.put(maxPopulation, privateMachines);
		hourlySolutionsMap.put(maxPopulation, hourlySolutions);
		
		privateHostsTabs.addTab("Var " + maxPopulation, privateHosts);
		
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(2, 1, 0, 0));
		publicVsPrivateTabs.addTab("Var " + maxPopulation, lowerPanel);
		lowerPanel.add(hourlySolutions);
		lowerPanel.add(privateMachines);
		
		gui.validate();
		
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

		JTabbedPane tabbedPane = new JTabbedPane();
		gui.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(2, 1, 0, 0));
		lowerPanel.add(solutions);
		lowerPanel.add(tiers);
		tabbedPane.addTab("Solutions", lowerPanel);

		tabbedPane.addTab("Allocations", solutions2);

		tabbedPane.addTab("Sizes", tiers2);

		tabbedPane.addTab("Basic Sizes", tiersBasic);

		tabbedPane.addTab("Costs", costs);

		tabbedPane.addTab("Workloads", populations);

		tabbedPane.addTab("Feasibilities", feasibilities);
		
		tabbedPane.addTab("Durations", durations);
		
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
		
		solutions.save2png(path, "allocations.png");
		populations.save2png(path, "populations.png");
		tiers.save2png(path, "sizes.png");
		tiersBasic.save2png(path, "basicSizes.png");
		costs.save2png(path, "costs.png");
		feasibilities.save2png(path, "feasibilities.png");
		durations.save2png(path, "durations.png");
		
		for (Integer key : privateHostsMap.keySet()) {
			GenericChart<DefaultCategoryDataset> privateHosts = privateHostsMap.get(key);
			GenericChart<DefaultCategoryDataset> privateMachines = privateMachinesMap.get(key);
			GenericChart<DefaultCategoryDataset> hourlySolutions = hourlySolutionsMap.get(key);
			
			privateHosts.save2png(path, key + "-privateHosts.png");
			privateMachines.save2png(path, key + "-machinesOnPrivate.png");
			hourlySolutions.save2png(path, key + "-machinesOnPublic.png");
		}
		
	}

	public void setValue(int value) {
		if (value > total)
			value = total;
		else if (value < 0)
			value = 0;
		progressBar.setValue(value);
		progressBar.setString(value + " out of " + total + " completed (" + Math.round(progressBar.getPercentComplete() * 100) + "%)");
	}

	public void updateGraph() {
		if (alreadyUpdating)
			return;
		alreadyUpdating = true;
		
		populations.updateGraph();
		costs.updateGraph();
		solutions.updateGraph();
		solutions2.updateGraph();
		tiers.updateGraph();
		tiers2.updateGraph();
		tiersBasic.updateGraph();
		feasibilities.updateGraph();
		durations.updateGraph();
		
		for (Integer key : privateHostsMap.keySet()) {
			GenericChart<DefaultCategoryDataset> privateHosts = privateHostsMap.get(key);
			GenericChart<DefaultCategoryDataset> privateMachines = privateMachinesMap.get(key);
			GenericChart<DefaultCategoryDataset> hourlySolutions = hourlySolutionsMap.get(key);
			
			if (privateHosts != null)
				privateHosts.updateGraph();
			if (privateMachines != null)
				privateMachines.updateGraph();
			if (hourlySolutions != null)
				hourlySolutions.updateGraph();
		}
		
		alreadyUpdating = false;
	}

	private void updateImages() {
		if (alreadyUpdating)
			return;
		alreadyUpdating = true;
		
		populations.updateImage();
		costs.updateImage();
		solutions.updateImage();
		solutions2.updateImage();
		tiers.updateImage();
		tiers2.updateImage();
		tiersBasic.updateImage();
		feasibilities.updateImage();
		durations.updateImage();
		
		for (Integer key : privateHostsMap.keySet()) {
			GenericChart<DefaultCategoryDataset> privateHosts = privateHostsMap.get(key);
			GenericChart<DefaultCategoryDataset> privateMachines = privateMachinesMap.get(key);
			GenericChart<DefaultCategoryDataset> hourlySolutions = hourlySolutionsMap.get(key);
			
			if (privateHosts != null)
				privateHosts.updateImage();
			if (privateMachines != null)
				privateMachines.updateImage();
			if (hourlySolutions != null)
				hourlySolutions.updateImage();
		}
		
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