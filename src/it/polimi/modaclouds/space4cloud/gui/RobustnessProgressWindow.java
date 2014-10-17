package it.polimi.modaclouds.space4cloud.gui;

import it.polimi.modaclouds.qos_models.schema.ClosedWorkload;
import it.polimi.modaclouds.qos_models.schema.ClosedWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.OpenWorkload;
import it.polimi.modaclouds.qos_models.schema.OpenWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtensions;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.mainProgram.Space4Cloud;
import it.polimi.modaclouds.space4cloud.utils.DOM;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

public class RobustnessProgressWindow {

	public static enum Size {

		// Amazon
		zero("zero", 0), t1micro("micro", 1), m1small("small", 2), m1medium(
				"medium", 3), c1medium("medium", 3), m1large("large", 4), c3large(
				"large", 4), m1xlarge("xlarge", 5), m2xlarge("xlarge", 5), m3xlarge(
				"xlarge", 5), c1xlarge("xlarge", 5), c3xlarge("xlarge", 5), m32xlarge(
				"xxlarge", 6), c32xlarge("xxlarge", 6),

		// Microsoft
		GeneralAvailabilityExtraSmallInstance("ExtraSmall", 7), PreviewExtraSmallInstance(
				"ExtraSmall", 7), GeneralAvailabilitySmallInstance("Small", 8), PreviewSmallInstance(
				"Small", 8), GeneralAvailabilityMediumInstance("Medium", 9), PreviewMediumInstance(
				"Medium", 9), GeneralAvailabilityLargeInstance("Large", 10), PreviewLargeInstance(
				"Large", 10), GeneralAvailabilityExtraLargeInstance(
				"ExtraLarge", 11), PreviewExtraLargeInstance("ExtraLarge", 11),

		// Flexiscale
		Flexiscale512MB1CPUServer("512 MB/1 CPU", 12), Flexiscale512MB1CPUServerWindows(
				"512 MB/1 CPU", 12), Flexiscale1GB1CPUServer("1 GB/1 CPU", 13), Flexiscale1GB1CPUServerWindows(
				"1 GB/1 CPU", 13), Flexiscale2GB1CPUServer("2 GB/1 CPU", 14), Flexiscale2GB1CPUServerWindows(
				"2 GB/1 CPU", 14), Flexiscale2GB2CPUServer("2 GB/2 CPU", 15), Flexiscale2GB2CPUServerWindows(
				"2 GB/2 CPU", 15), Flexiscale4GB2CPUServer("4 GB/2 CPU", 16), Flexiscale4GB2CPUServerWindows(
				"4 GB/2 CPU", 16), Flexiscale4GB3CPUServer("4 GB/3 CPU", 17), Flexiscale4GB3CPUServerWindows(
				"4 GB/3 CPU", 17), Flexiscale4GB4CPUServer("4 GB/4 CPU", 18), Flexiscale4GB4CPUServerWindows(
				"4 GB/4 CPU", 18), Flexiscale6GB3CPUServer("6 GB/3 CPU", 19), Flexiscale6GB3CPUServerWindows(
				"6 GB/3 CPU", 19), Flexiscale6GB4CPUServer("6 GB/4 CPU", 20), Flexiscale6GB4CPUServerWindows(
				"6 GB/4 CPU", 20), Flexiscale6GB5CPUServer("6 GB/5 CPU", 21), Flexiscale6GB5CPUServerWindows(
				"6 GB/5 CPU", 21), Flexiscale6GB6CPUServer("6 GB/6 CPU", 22), Flexiscale6GB6CPUServerWindows(
				"6 GB/6 CPU", 22), Flexiscale8GB4CPUServer("8 GB/4 CPU", 23), Flexiscale8GB4CPUServerWindows(
				"8 GB/4 CPU", 23), Flexiscale8GB5CPUServer("8 GB/5 CPU", 24), Flexiscale8GB5CPUServerWindows(
				"8 GB/5 CPU", 24), Flexiscale8GB6CPUServer("8 GB/6 CPU", 25), Flexiscale8GB6CPUServerWindows(
				"8 GB/6 CPU", 25), Flexiscale8GB7CPUServer("8 GB/7 CPU", 26), Flexiscale8GB7CPUServerWindows(
				"8 GB/7 CPU", 26), Flexiscale8GB8CPUServer("8 GB/8 CPU", 27), Flexiscale8GB8CPUServerWindows(
				"8 GB/8 CPU", 27);

		private static final int lastAmazonId = 6, lastMicrosoftId = 11,
				lastFlexiscaleId = 27;

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

	@SuppressWarnings("unused")
	public static void main(String args[]) {
		String basePath = "C:\\Users\\Riccardo\\Desktop\\tmp\\argh\\results";

		int testFrom = 100, testTo = 4300, step = 300;
		// int testFrom = 30, testTo = 3000, step = 90;

		if (args != null && args.length > 0) {
			basePath = args[0];

			if (args.length > 1) {
				try {
					testFrom = Integer.parseInt(args[1]);
				} catch (Exception e) {
				}

				if (args.length > 2) {
					try {
						testTo = Integer.parseInt(args[2]);
					} catch (Exception e) {
					}

					if (args.length > 3) {
						try {
							step = Integer.parseInt(args[3]);
						} catch (Exception e) {
						}

					}
				}
			}
		}

		// redraw(basePath, testFrom, testTo, step);

		RobustnessProgressWindow rpw1 = redraw(basePath); //, testFrom, testTo, step);
		// rpw1.gui.dispose();

		// RobustnessProgressWindow rpw2 =
		// redraw("C:\\Users\\Riccardo\\Desktop\\tmp\\russo\\bipicco3", 100,
		// 10000, 300);
		// rpw2.gui.dispose();
		//
		// RobustnessProgressWindow rpw3 =
		// redraw("C:\\Users\\Riccardo\\Desktop\\tmp\\russo\\results-amazon2",
		// 100, 10000, 300);
		// rpw3.gui.dispose();
		//
		// compare(rpw1, rpw2, basePath + File.separator +
		// "costsDiffs-generated-non2.png");
		// compare(rpw1, rpw3, basePath + File.separator +
		// "costsDiffs-generated-generated2.png");
		// compare(rpw3, rpw2, basePath + File.separator +
		// "costsDiffs-generated2-non2.png");

	}
	public static RobustnessProgressWindow redraw(String basePath) { //,	int testFrom, int testTo, int step) {
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
		
		
		/*
		int total = (testTo - testFrom) / step;

		RobustnessProgressWindow rpw = new RobustnessProgressWindow(total);

		try {
			for (int i = testFrom; i <= testTo; i += step) {
				rpw.add(Paths.get(basePath, "ume-" + i + ".xml").toFile(),
						Paths.get(basePath, "solution-" + i + ".xml").toFile());

				rpw.setValue(rpw.getValue() + 1);
			}

			rpw.save2png(basePath);
		} catch (Exception e) {
			e.printStackTrace();
		}

		rpw.setValue(total);
		*/

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

	private JFreeChart solutionsGraph;
	private JPanel solutionsPanel;
	private JLabel solutionsLabel;
	private JPanel solutionsPanel2;
	private JLabel solutionsLabel2;

	private JFreeChart tiersGraph;
	private JPanel tiersPanel;
	private JLabel tiersLabel;

	private JPanel tiersPanel2;
	private JLabel tiersLabel2;
	
	private JFreeChart tiersBasicGraph;
	private JPanel tiersBasicPanel;
	private JLabel tiersBasicLabel;
	
	private JFreeChart feasibilitiesGraph;
	private JPanel feasibilitiesPanel;
	private JLabel feasibilitiesLabel;
	
	private JFreeChart costsGraph;
	private JPanel costsPanel;
	private JLabel costsLabel;
	
	private JFreeChart durationsGraph;
	private JPanel durationsPanel;
	private JLabel durationsLabel;
	
	private int total;
	private DefaultCategoryDataset populations = new DefaultCategoryDataset();

	private DefaultCategoryDataset solutions = new DefaultCategoryDataset();

	private DefaultCategoryDataset tiers = new DefaultCategoryDataset();

	private DefaultCategoryDataset tiersBasic = new DefaultCategoryDataset();

	private DefaultCategoryDataset feasibilities = new DefaultCategoryDataset();

	private DefaultCategoryDataset costs = new DefaultCategoryDataset();
	
	private DefaultCategoryDataset durations = new DefaultCategoryDataset();

	private boolean alreadyUpdating = false;

	public RobustnessProgressWindow(int total) {
		this.total = total;

		updateGraph();

		initialize();
	}

	public void add(File usageModelExtension, File solution)
			throws MalformedURLException, JAXBException, SAXException {
		UsageModelExtensions umes = XMLHelper.deserialize(usageModelExtension
				.toURI().toURL(), UsageModelExtensions.class);

		int maxPopulation = -1, maxHour = -1;
		String name = "Var "
				+ Space4Cloud.getMaxPopulation(usageModelExtension);

		ClosedWorkload cw = umes.getUsageModelExtension().getClosedWorkload();
		if (cw != null) {
			for (ClosedWorkloadElement we : cw.getWorkloadElement()) {
				if (maxPopulation < we.getPopulation()) {
					maxPopulation = we.getPopulation();
					maxHour = we.getHour();
				}
				populations.addValue(we.getPopulation(), name,
						"" + we.getHour());
			}
		} else {

			OpenWorkload ow = umes.getUsageModelExtension().getOpenWorkload();
			if (ow != null) {
				for (OpenWorkloadElement we : ow.getWorkloadElement()) {
					if (maxPopulation < we.getPopulation()) {
						maxPopulation = we.getPopulation();
						maxHour = we.getHour();
					}
					populations.addValue(we.getPopulation(), name,
							"" + we.getHour());
				}
			} else {
				return;
			}
		}

		Document doc = DOM.getDocument(solution);

		NodeList nl = doc.getElementsByTagName("Tier");

		for (int i = 0; i < nl.getLength(); i++) {
			Node tier = nl.item(i);

			String size = tier.getAttributes().getNamedItem("resourceName")
					.getNodeValue();

			Size s = Size.parse(size);

			tiers.addValue(s.ordinal(), "Tier " + i, "" + maxPopulation);
			tiersBasic.addValue(s.basicId, "Tier " + i, "" + maxPopulation);

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
											.getNodeValue()), "Tier " + i, ""
											+ maxPopulation);
				}

			}
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
			feasibilities.addValue(feasibility ? 1 : 0, "Solution"/* name */, ""
					+ "" + maxPopulation);
			
			durations.addValue(duration, "Solution"/* name */, "" + "" + maxPopulation);
		}

		sortDataset(solutions);
		sortDataset(tiers);
		sortDataset(costs);
		sortDataset(feasibilities);
		sortDataset(durations);
		sortDataset(tiersBasic);

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
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // .DISPOSE_ON_CLOSE);
		gui.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel upperPanel = new JPanel();
		gui.getContentPane().add(upperPanel, BorderLayout.NORTH);
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));

		progressBar = new JProgressBar(0, total);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
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

		// listener to resize images
		gui.addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

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
	}

	public void setValue(int value) {
		if (value > total)
			value = total;
		progressBar.setValue(value);
	}

	@SuppressWarnings({ "deprecation" })
	public void updateGraph() {
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

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
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setRange(0, rangeAxis.getRange().getUpperBound() * 1.1);
			rangeAxis.setTickLabelFont(font);

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
							result = new StringBuffer("" + number);
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

		alreadyUpdating = false;
	}

}