package it.polimi.modaclouds.space4cloud.gui;

import it.polimi.modaclouds.qos_models.schema.ClosedWorkload;
import it.polimi.modaclouds.qos_models.schema.ClosedWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.OpenWorkload;
import it.polimi.modaclouds.qos_models.schema.OpenWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtensions;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4cloud.utils.Configuration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartFactory;
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

public class SolutionWindowPanel extends JTabbedPane{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4205611616209241366L;

	private SolutionMulti solutionMulti;	
	private JFreeChart populationsGraph;
	private JPanel populationsPanel;
	private JLabel populationsLabel;
	private DefaultCategoryDataset populations = new DefaultCategoryDataset();

	private HashMap<String, JFreeChart> workloadGraphs;
	private HashMap<String, JPanel> workloadPanels;
	private HashMap<String, JLabel> workloadLabels;
	private HashMap<String, DefaultCategoryDataset> workloads;

	private HashMap<String, JFreeChart> allocationGraphs;
	private HashMap<String, JPanel> allocationPanels;
	private HashMap<String, JLabel> allocationLabels;
	private HashMap<String, DefaultCategoryDataset> allocations;
	private File usageModelExtension;

	private boolean alreadyUpdating = false;

	private JPanel informationPanel;
	private JLabel informationLabel = new JLabel();

	private HashMap<String, JPanel> informationPanels;
	private HashMap<String, JLabel> informationLabels;

	public SolutionWindowPanel(SolutionMulti solutionMulti, File usageModelExtension) {
		super();

		this.solutionMulti = solutionMulti;
		this.usageModelExtension = usageModelExtension;

		workloadGraphs = new HashMap<String, JFreeChart>();
		workloadPanels = new HashMap<String, JPanel>();
		workloadLabels = new HashMap<String, JLabel>();
		workloads = new HashMap<String, DefaultCategoryDataset>();

		allocationGraphs = new HashMap<String, JFreeChart>();
		allocationPanels = new HashMap<String, JPanel>();
		allocationLabels = new HashMap<String, JLabel>();
		allocations = new HashMap<String, DefaultCategoryDataset>();

		informationPanels = new HashMap<String, JPanel>();
		informationLabels = new HashMap<String, JLabel>();

		setData(solutionMulti);


	}

	/**
	 * @wbp.parser.constructor
	 */
	public SolutionWindowPanel(SolutionMulti solutionMulti) {
		this(solutionMulti, null);
	}

	public void setData(SolutionMulti solutionMulti) {		
		if (!solutionMulti.isEvaluated())
			return;

		StringBuffer infoAll = new StringBuffer();

		infoAll.append("<html>\n<body style='font-size: 13;'>\n");
		infoAll.append("<table style='width: 500px; border: 1px solid black; margin: 30px; padding: 2px' border=1>\n");
		infoAll.append("<tr><th style='border: 0; background: black; color: white;'>Information</th><th style='border:0; background: black; color: white;'>Value</th></tr>\n");
		infoAll.append("<tr><td>Total Cost</td><td>" + solutionMulti.getCost() + "</td></tr>\n");
		infoAll.append("<tr><td>Generation Time</td><td>" + solutionMulti.getGenerationTime() + " ms</td></tr>\n");
		infoAll.append("<tr><td>Providers</td><td><ul>\n");
		for (int i = 0; i < solutionMulti.size(); ++i) {
			String provider = solutionMulti.get(i).getProvider();
			infoAll.append("<li>" +  provider + "</li>\n");
		}
		if (solutionMulti.getPrivateCloudSolution() != null)
			infoAll.append("<li>Private Cloud</li>\n");
		infoAll.append("</ul></td></tr>\n");
		infoAll.append("<tr><td>Feasible</td><td>" + String.valueOf(solutionMulti.isFeasible()) + "</td></tr>\n");

		boolean set = false;

		//		String[] providers = {"Amazon", "Microsoft"};
		//		for (String provider : providers) {
		for (int i = 0; i < solutionMulti.size(); ++i) {
			String provider = solutionMulti.get(i).getProvider();
			Solution solution = solutionMulti.get(provider);
			if (solution == null)
				continue;

			DefaultCategoryDataset workload = new DefaultCategoryDataset();
			DefaultCategoryDataset allocation = new DefaultCategoryDataset();

			for (int hour = 0; hour < 24; ++hour) {
				workload.addValue(solution.getPercentageWorkload(hour), provider, "" + hour);
				for (Tier t : solution.getApplication(hour).getTiers()) {
					allocation.addValue(((IaaS) t.getCloudService()).getReplicas(), t.getName(), "" + hour);
				}
			}

			workloads.put(provider, workload);
			allocations.put(provider, allocation);

			if (!set) {
				infoAll.append("<tr><td>Components</td><td><ul>\n");
				for (Tier t : solution.getApplication(0).getTiers()) {
					infoAll.append("<li>" + t.getName() + "</li>\n");
				}
				infoAll.append("</ul></td></tr>\n");
				infoAll.append("</table>\n</body>\n</html>\n");

				informationLabel.setText(infoAll.toString());
				set = true;
			}

			JLabel informationLabel = new JLabel();
			informationLabels.put(provider, informationLabel);

			StringBuffer info = new StringBuffer();
			info.append("<html>\n<body style='font-size: 13;'>\n");
			info.append("<table style='width: 500px; border: 1px solid black; margin: 30px; padding: 2px' border=1>\n");
			info.append("<tr><th style='border: 0; background: black; color: white;'>Information</th><th style='border:0; background: black; color: white;'>Value</th></tr>\n");
			info.append("<tr><td>Cost</td><td>" + solution.getCost() + "</td></tr>\n");
			info.append("<tr><td>Generation Time</td><td>" + solution.getGenerationIteration() + " ms</td></tr>\n");
			info.append("<tr><td>Provider</td><td>" + provider + "</td></tr>\n");
			if (solution.getRegion() != null)
				info.append("<tr><td>Region</td><td>" + solution.getRegion() + "</td></tr>\n");
			info.append("<tr><td>Feasible</td><td>" + String.valueOf(solution.isFeasible()) + "</td></tr>\n");
			info.append("<tr><td>Solution per Component</td><td><ul>\n");
			for (Tier t : solution.getApplication(0).getTiers()) {
				info.append("<li>" + t.getName() + "\n<ul>\n");
				info.append("<li>" + t.getCloudService().getServiceName() + "</li>\n");
				info.append("<li>" + t.getCloudService().getResourceName() + "</li>\n");
				info.append("</ul>\n</li>\n");
			}
			info.append("</ul></td></tr>\n");
			info.append("</table>\n</body>\n</html>\n");
			informationLabel.setText(info.toString());
		}

		if (solutionMulti.getPrivateCloudSolution() != null) {
			String provider = "Private Cloud";
			Solution solution = solutionMulti.getPrivateCloudSolution();

			DefaultCategoryDataset workload = new DefaultCategoryDataset();
			DefaultCategoryDataset allocation = new DefaultCategoryDataset();

			for (int hour = 0; hour < 24; ++hour) {
				workload.addValue(solution.getPercentageWorkload(hour), provider, "" + hour);
				for (Tier t : solution.getApplication(hour).getTiers()) {
					allocation.addValue(((IaaS) t.getCloudService()).getReplicas(), t.getName(), "" + hour);
				}
			}

			workloads.put(provider, workload);
			allocations.put(provider, allocation);

			if (!set) {
				infoAll.append("<tr><td>Components</td><td><ul>\n");
				for (Tier t : solution.getApplication(0).getTiers()) {
					infoAll.append("<li>" + t.getName() + "</li>\n");
				}
				infoAll.append("</ul></td></tr>\n");
				infoAll.append("</table>\n</body>\n</html>\n");

				informationLabel.setText(infoAll.toString());
				set = true;
			}

			JLabel informationLabel = new JLabel();
			informationLabels.put(provider, informationLabel);

			StringBuffer info = new StringBuffer();
			info.append("<html>\n<body style='font-size: 13;'>\n");
			info.append("<table style='width: 500px; border: 1px solid black; margin: 30px; padding: 2px' border=1>\n");
			info.append("<tr><th style='border: 0; background: black; color: white;'>Information</th><th style='border:0; background: black; color: white;'>Value</th></tr>\n");
			info.append("<tr><td>Feasible</td><td>" + String.valueOf(solution.isFeasible()) + "</td></tr>\n");
			info.append("<tr><td>Solution per Component</td><td><ul>\n");
			for (Tier t : solution.getApplication(0).getTiers()) {
				info.append("<li>" + t.getName() + "\n<ul>\n");
				info.append("<li>" + t.getCloudService().getServiceName() + "</li>\n");
				info.append("<li>" + t.getCloudService().getResourceName() + "</li>\n");
				info.append("</ul>\n</li>\n");
			}
			info.append("</ul></td></tr>\n");
			info.append("</table>\n</body>\n</html>\n");
			informationLabel.setText(info.toString());
		}

		if (usageModelExtension != null && usageModelExtension.exists())
			setPopulation(usageModelExtension);
		else if (Configuration.USAGE_MODEL_EXTENSION != null && Configuration.USAGE_MODEL_EXTENSION.length() > 0)
			setPopulation(new File(Configuration.USAGE_MODEL_EXTENSION));

		updateGraph();

		initialize();
	}

	public void setPopulation(File usageModelExtension) {
		populations.clear();

		try {
			UsageModelExtensions umes = XMLHelper.deserialize(usageModelExtension
					.toURI().toURL(), UsageModelExtensions.class);

			//		String name = "Var " + Space4Cloud.getMaxPopulation(usageModelExtension);

			ClosedWorkload cw = umes.getUsageModelExtension().getClosedWorkload();
			if (cw != null) {
				for (ClosedWorkloadElement we : cw.getWorkloadElement())
					populations.addValue(we.getPopulation(), "Population", "" + we.getHour());
			} else {

				OpenWorkload ow = umes.getUsageModelExtension().getOpenWorkload();
				if (ow != null) {
					for (OpenWorkloadElement we : ow.getWorkloadElement())
						populations.addValue(we.getPopulation(), "Population", "" + we.getHour());
				} else
					return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initialize() {





		// listener to resize images
		addComponentListener(new ComponentListener() {

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

		JPanel pan = new JPanel(new GridLayout(2, 1));
		addTab("Generic", pan);

		populationsPanel = new JPanel();
		populationsPanel.setPreferredSize(new Dimension(10,10));
		pan.add(populationsPanel);
		populationsLabel = new JLabel();
		populationsLabel.setIcon(null);
		populationsPanel.add(populationsLabel);

		populationsPanel.setBorder(new TitledBorder(null, "Population Graph",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));

		informationPanel = new JPanel();

		JScrollPane pane = new JScrollPane(informationPanel);

		pan.add(pane);

		//		pan.add(informationPanel);
		//		informationPanel.setBorder(new TitledBorder(null, "Information",
		//				TitledBorder.LEADING, TitledBorder.TOP, null, null));

		informationPanel.add(informationLabel);

		//		String[] providers = {"Amazon", "Microsoft"};
		//		for (String provider : providers) {
		for (int i = 0; i < solutionMulti.size(); ++i) {

			String provider = solutionMulti.get(i).getProvider();
			pan = new JPanel(new GridLayout(2, 1));
			addTab(provider, pan);
			if(i==0)
				setSelectedComponent(pan);
			JTabbedPane inTab = new JTabbedPane();
			pan.add(inTab);

			JPanel workloadPanel = new JPanel();
			//			pan.add(workloadPanel);
			inTab.addTab("Population Percentage", workloadPanel);
			JLabel workloadLabel = new JLabel();
			workloadLabel.setIcon(null);
			workloadPanel.add(workloadLabel);

			//			workloadPanel.setBorder(new TitledBorder(null, "Population Percentage",
			//					TitledBorder.LEADING, TitledBorder.TOP, null, null));

			workloadPanels.put(provider, workloadPanel);
			workloadLabels.put(provider, workloadLabel);


			JPanel allocationPanel = new JPanel();
			//			pan.add(allocationPanel);
			inTab.addTab("Allocations", allocationPanel);
			inTab.setSelectedComponent(allocationPanel);
			JLabel allocationLabel = new JLabel();
			allocationLabel.setIcon(null);
			allocationPanel.add(allocationLabel);

			//			allocationPanel.setBorder(new TitledBorder(null, "Population Percentage",
			//					TitledBorder.LEADING, TitledBorder.TOP, null, null));

			allocationPanels.put(provider, allocationPanel);
			allocationLabels.put(provider, allocationLabel);


			JPanel informationPanel = new JPanel();
			informationPanels.put(provider, informationPanel);
			JLabel informationLabel = informationLabels.get(provider);

			JScrollPane paneIn = new JScrollPane(informationPanel);

			pan.add(paneIn);

			//			pan.add(informationPanel);
			//			informationPanel.setBorder(new TitledBorder(null, "Information",
			//					TitledBorder.LEADING, TitledBorder.TOP, null, null));

			informationPanel.add(informationLabel);
		}

		if (solutionMulti.getPrivateCloudSolution() != null) {
			String provider = "Private Cloud";
			pan = new JPanel(new GridLayout(2, 1));
			addTab(provider, pan);

			JTabbedPane inTab = new JTabbedPane();
			pan.add(inTab);

			JPanel workloadPanel = new JPanel();
			//			pan.add(workloadPanel);
			inTab.addTab("Population Percentage", workloadPanel);
			JLabel workloadLabel = new JLabel();
			workloadLabel.setIcon(null);
			workloadPanel.add(workloadLabel);

			//			workloadPanel.setBorder(new TitledBorder(null, "Population Percentage",
			//					TitledBorder.LEADING, TitledBorder.TOP, null, null));

			workloadPanels.put(provider, workloadPanel);
			workloadLabels.put(provider, workloadLabel);


			JPanel allocationPanel = new JPanel();
			//			pan.add(allocationPanel);
			inTab.addTab("Allocations", allocationPanel);
			JLabel allocationLabel = new JLabel();
			allocationLabel.setIcon(null);
			allocationPanel.add(allocationLabel);

			//			allocationPanel.setBorder(new TitledBorder(null, "Population Percentage",
			//					TitledBorder.LEADING, TitledBorder.TOP, null, null));

			allocationPanels.put(provider, allocationPanel);
			allocationLabels.put(provider, allocationLabel);


			JPanel informationPanel = new JPanel();
			informationPanels.put(provider, informationPanel);
			JLabel informationLabel = informationLabels.get(provider);

			JScrollPane paneIn = new JScrollPane(informationPanel);

			pan.add(paneIn);

			//			pan.add(informationPanel);
			//			informationPanel.setBorder(new TitledBorder(null, "Information",
			//					TitledBorder.LEADING, TitledBorder.TOP, null, null));

			informationPanel.add(informationLabel);
		}

		updateImages();
	}

	@SuppressWarnings({ "deprecation" })
	private void updateGraph() {
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

		populationsGraph = ChartFactory.createLineChart(null, "Hour", "Population", populations);
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

		//		String[] providers = {"Amazon", "Microsoft"};
		//		for (String provider : providers) {
		for (int i = 0; i < solutionMulti.size(); ++i) {
			String provider = solutionMulti.get(i).getProvider();

			DefaultCategoryDataset workload = workloads.get(provider);

			JFreeChart workloadGraph = ChartFactory.createLineChart(null, "Hour", 
					"Workload Percentage", workload, PlotOrientation.VERTICAL, true, true, false);
			{
				CategoryPlot plot = (CategoryPlot) workloadGraph.getPlot();
				LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
						.getRenderer();
				renderer.setShapesVisible(true);
				renderer.setDrawOutlines(true);
				renderer.setUseFillPaint(true);
				renderer.setFillPaint(Color.white);
				NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
				rangeAxis.setTickLabelFont(font);

				rangeAxis.setRange(-0.1, 1.1);

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
								result = new StringBuffer((int)(number * 100) + "%");
								return result;
							}

						});
				renderer2.setItemLabelGenerator(generator);
				renderer2.setItemLabelsVisible(true);
				renderer2.setItemLabelFont(font);
			}

			workloadGraphs.put(provider, workloadGraph);



			DefaultCategoryDataset allocation = allocations.get(provider);

			JFreeChart allocationGraph = ChartFactory.createLineChart(null, "Hour", 
					"Allocation", allocation, PlotOrientation.VERTICAL, true, true, false);
			{
				CategoryPlot plot = (CategoryPlot) allocationGraph.getPlot();
				LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
						.getRenderer();
				renderer.setShapesVisible(true);
				renderer.setDrawOutlines(true);
				renderer.setUseFillPaint(true);
				renderer.setFillPaint(Color.white);
				NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
				rangeAxis.setTickLabelFont(font);

				int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, tmp;
				for (int l = 0; l < allocation.getColumnCount(); ++l)
					for (int j = 0; j < allocation.getRowCount(); ++j) {
						tmp = allocation.getValue(j, l).intValue();
						if (tmp < min)
							min = tmp;
						if (tmp > max)
							max = tmp;
					}
				if (min == Integer.MAX_VALUE)
					min = 0;
				if (max == Integer.MIN_VALUE)
					max = (int) rangeAxis.getRange().getUpperBound() + 1;

				rangeAxis.setRange(/* 0 */min - 1, /*
				 * rangeAxis.getRange().
				 * getUpperBound() + 1
				 */max + 1);

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

			allocationGraphs.put(provider, allocationGraph);

		}



		if (solutionMulti.getPrivateCloudSolution() != null) {
			String provider = "Private Cloud";

			DefaultCategoryDataset workload = workloads.get(provider);

			JFreeChart workloadGraph = ChartFactory.createLineChart(null, "Hour", 
					"Workload Percentage", workload, PlotOrientation.VERTICAL, true, true, false);
			{
				CategoryPlot plot = (CategoryPlot) workloadGraph.getPlot();
				LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
						.getRenderer();
				renderer.setShapesVisible(true);
				renderer.setDrawOutlines(true);
				renderer.setUseFillPaint(true);
				renderer.setFillPaint(Color.white);
				NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
				rangeAxis.setTickLabelFont(font);

				rangeAxis.setRange(-0.1, 1.1);

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
								result = new StringBuffer((int)(number * 100) + "%");
								return result;
							}

						});
				renderer2.setItemLabelGenerator(generator);
				renderer2.setItemLabelsVisible(true);
				renderer2.setItemLabelFont(font);
			}

			workloadGraphs.put(provider, workloadGraph);



			DefaultCategoryDataset allocation = allocations.get(provider);

			JFreeChart allocationGraph = ChartFactory.createLineChart(null, "Hour", 
					"Allocation", allocation, PlotOrientation.VERTICAL, true, true, false);
			{
				CategoryPlot plot = (CategoryPlot) allocationGraph.getPlot();
				LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
						.getRenderer();
				renderer.setShapesVisible(true);
				renderer.setDrawOutlines(true);
				renderer.setUseFillPaint(true);
				renderer.setFillPaint(Color.white);
				NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
				rangeAxis.setTickLabelFont(font);

				int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, tmp;
				for (int l = 0; l < allocation.getColumnCount(); ++l)
					for (int j = 0; j < allocation.getRowCount(); ++j) {
						tmp = allocation.getValue(j, l).intValue();
						if (tmp < min)
							min = tmp;
						if (tmp > max)
							max = tmp;
					}
				if (min == Integer.MAX_VALUE)
					min = 0;
				if (max == Integer.MIN_VALUE)
					max = (int) rangeAxis.getRange().getUpperBound() + 1;

				rangeAxis.setRange(/* 0 */min - 1, /*
				 * rangeAxis.getRange().
				 * getUpperBound() + 1
				 */max + 1);

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

			allocationGraphs.put(provider, allocationGraph);
		}

	}

	private void updateImages() {
		if (alreadyUpdating)
			return;
		alreadyUpdating = true;

		if (populationsGraph != null) {
			ImageIcon icon;
			try {
				int width = populationsPanel.getSize().width;
				if( width == 0)
					width  = populationsPanel.getPreferredSize().width;

				int height = populationsPanel.getSize().height;
				if( height == 0)
					height = populationsPanel.getPreferredSize().height;

				icon = new ImageIcon(populationsGraph.createBufferedImage(width,height));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
			}
			populationsLabel.setIcon(icon);
			populationsLabel.setVisible(true);
			populationsPanel.setPreferredSize(populationsLabel
					.getPreferredSize());

			populationsLabel.validate();
		}

		//		String[] providers = {"Amazon", "Microsoft"};
		//		for (String provider : providers) {
		for (int i = 0; i < solutionMulti.size(); ++i) {
			String provider = solutionMulti.get(i).getProvider();

			JFreeChart workloadGraph = workloadGraphs.get(provider);

			JPanel workloadPanel = workloadPanels.get(provider);
			JLabel workloadLabel = workloadLabels.get(provider);

			if (workloadGraph != null) {
				ImageIcon icon;
				try {
					int width = workloadPanel.getSize().width;
					if( width == 0)
						width  = workloadPanel.getPreferredSize().width;

					int height = workloadPanel.getSize().height;
					if( height == 0)
						height = workloadPanel.getPreferredSize().height;

					icon = new ImageIcon(workloadGraph.createBufferedImage(width,height));
				} catch (NullPointerException e) {
					icon = new ImageIcon();
				}
				workloadLabel.setIcon(icon);
				workloadLabel.setVisible(true);
				workloadPanel.setPreferredSize(workloadLabel
						.getPreferredSize());

				workloadLabel.validate();
			}

			JFreeChart allocationGraph = allocationGraphs.get(provider);

			JPanel allocationPanel = allocationPanels.get(provider);
			JLabel allocationLabel = allocationLabels.get(provider);

			if (allocationGraph != null) {
				ImageIcon icon;
				try {
					int width = allocationPanel.getSize().width;
					if( width == 0)
						width  = allocationPanel.getPreferredSize().width;

					int height = allocationPanel.getSize().height;
					if( height == 0)
						height = allocationPanel.getPreferredSize().height;
					icon = new ImageIcon(allocationGraph.createBufferedImage(width,height));
				} catch (NullPointerException e) {
					icon = new ImageIcon();
				}
				allocationLabel.setIcon(icon);
				allocationLabel.setVisible(true);
				allocationPanel.setPreferredSize(allocationLabel
						.getPreferredSize());

				allocationLabel.validate();
			}
		}

		if (solutionMulti.getPrivateCloudSolution() != null) {
			String provider = "Private Cloud";

			JFreeChart workloadGraph = workloadGraphs.get(provider);

			JPanel workloadPanel = workloadPanels.get(provider);
			JLabel workloadLabel = workloadLabels.get(provider);

			if (workloadGraph != null) {
				ImageIcon icon;
				try {
					int width = workloadPanel.getSize().width;
					if( width == 0)
						width  = workloadPanel.getPreferredSize().width;

					int height = workloadPanel.getSize().height;
					if( height == 0)
						height = workloadPanel.getPreferredSize().height;

					icon = new ImageIcon(workloadGraph.createBufferedImage(width,height));					
				} catch (NullPointerException e) {
					icon = new ImageIcon();
				}
				workloadLabel.setIcon(icon);
				workloadLabel.setVisible(true);
				workloadPanel.setPreferredSize(workloadLabel
						.getPreferredSize());

				workloadLabel.validate();
			}

			JFreeChart allocationGraph = allocationGraphs.get(provider);

			JPanel allocationPanel = allocationPanels.get(provider);
			JLabel allocationLabel = allocationLabels.get(provider);

			if (allocationGraph != null) {
				ImageIcon icon;
				try {
					int width = allocationPanel.getSize().width;
					if( width == 0)
						width  = allocationPanel.getPreferredSize().width;

					int height = allocationPanel.getSize().height;
					if( height == 0)
						height = allocationPanel.getPreferredSize().height;
					icon = new ImageIcon(allocationGraph.createBufferedImage(width,height));
				} catch (NullPointerException e) {
					icon = new ImageIcon();
				}
				allocationLabel.setIcon(icon);
				allocationLabel.setVisible(true);
				allocationPanel.setPreferredSize(allocationLabel
						.getPreferredSize());

				allocationLabel.validate();
			}
		}

		alreadyUpdating = false;
	}





}