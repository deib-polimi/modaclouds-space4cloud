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
package it.polimi.modaclouds.space4cloud.gui;

import it.polimi.modaclouds.space4cloud.chart.Logger2JFreeChartImage;
import it.polimi.modaclouds.space4cloud.chart.SeriesHandle;
import it.polimi.modaclouds.space4cloud.optimization.constraints.Constraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Component;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Functionality;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

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

public class AssessmentWindow extends WindowAdapter implements PropertyChangeListener, ActionListener {

	private JFrame frame;

	private JTabbedPane tab;

	private class InternalSolution {
		@SuppressWarnings("unused")
		String provider;

		Logger2JFreeChartImage vmLogger, rtLogger, utilLogger;
		JPanel vmPanel, rtPanel, utilPanel;
		JLabel vmImgLabel, rtImgLabel, utilImgLabel;

		DefaultListModel<String> sourcesModel;
		JList<String> sources;

		DefaultListModel<String> plotsModel;
		JList<String> plots;

		JButton addPlot;
		JButton remPlot;
		JButton addAllPlot;
		JButton remAllPlot;
		JButton update;

		boolean constrained(String resourceId) {
			if (constraintHandler == null || solutionMulti == null)
				return false;

			List<Constraint> constraints = constraintHandler.getConstraintByResourceId(resourceId); // .getConstraints();

			return (constraints.size() > 0);
		}

		boolean toBeShown(String name) {
			for (int i = 0; i < plotsModel.size(); ++i) {
				String key = plotsModel.getElementAt(i);
				String elemName = key.substring(0, key.indexOf(" ("));
				if (name.equals(elemName))
					return true;
			}
			return false;
		}
	}

	private JFreeChart workloadGraph;
	private JPanel workloadPanel;
	private JLabel workloadLabel;
	private DefaultCategoryDataset workload;

	private JFreeChart availabilityGraph;
	private JPanel availabilityPanel;
	private JLabel availabilityLabel;
	private DefaultCategoryDataset availability;

	private HashMap<String, InternalSolution> solutions = new HashMap<String, InternalSolution>();

	public final static String FRAME_NAME = "Assessment Results Window";

	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/**
	 * Create the application.
	 */
	public AssessmentWindow(ConstraintHandler constraintHandler) {
		this.constraintHandler = constraintHandler;
		initialize();
	}

	private SolutionMulti solutionMulti = null;

	private ConstraintHandler constraintHandler;

	public void considerSolution(SolutionMulti solution) throws NumberFormatException, IOException {
		frame.setVisible(false);

		solutions.clear();
		tab.removeAll();

		workload = new DefaultCategoryDataset();
		availability = new DefaultCategoryDataset();

		this.solutionMulti = solution;

		for (Solution providedSolution : solution.getAll()) {
			String provider = providedSolution.getProvider();

			InternalSolution is = new InternalSolution();
			is.provider = provider;

			solutions.put(provider, is);

			JPanel imageContainerPanel = new JPanel();
			imageContainerPanel.setLayout(new GridLayout(3, 1, 0, 0));

			//			tab.addTab(provider, imageContainerPanel);

			is.vmPanel = new JPanel();
			//			vmPanel.setBorder(new TitledBorder(null, "Number of VMs",
			//					TitledBorder.LEADING, TitledBorder.TOP, null, null));
			imageContainerPanel.add(is.vmPanel);
			//			vmPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); //, 5, 5));

			is.vmImgLabel = new JLabel();
			is.vmPanel.add(is.vmImgLabel);

			is.utilPanel = new JPanel();
			//			utilPanel.setBorder(new TitledBorder(null, "Utilization",
			//					TitledBorder.LEADING, TitledBorder.TOP, null, null));
			imageContainerPanel.add(is.utilPanel);
			//			utilPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); //, 5, 5));

			is.utilImgLabel = new JLabel();
			is.utilPanel.add(is.utilImgLabel);

			is.rtPanel = new JPanel();
			//			rtPanel.setBorder(new TitledBorder(null, "Response Times",
			//					TitledBorder.LEADING, TitledBorder.TOP, null, null));
			imageContainerPanel.add(is.rtPanel);
			//			rtPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); //, 5, 5));

			is.rtImgLabel = new JLabel();
			is.rtPanel.add(is.rtImgLabel);

			{
				GridBagLayout gridBagLayout = new GridBagLayout();
				gridBagLayout.columnWidths = new int[]{201, 0};
				gridBagLayout.rowHeights = new int[]{70, 40, 70, 40, 0};
				gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gridBagLayout.rowWeights = new double[]{1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
				GridBagConstraints c = new GridBagConstraints();

				JPanel configurationPan = new JPanel(gridBagLayout);

				c.fill = GridBagConstraints.BOTH;

				c.gridx = 0;
				c.gridy = 0;
				c.insets = new Insets(10, 10, 0, 10);

				is.sourcesModel = new DefaultListModel<String>();
				is.sources = new JList<String>(is.sourcesModel);
				is.sources.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				is.sources.setSelectedIndex(-1);
				is.sources.setVisibleRowCount(10);
				JScrollPane listScrollPane = new JScrollPane(is.sources);
				listScrollPane.setBorder(new TitledBorder(null, "Source",
						TitledBorder.LEADING, TitledBorder.TOP, null, null));

				//				c.gridheight = GridBagConstraints.REMAINDER;
				configurationPan.add(listScrollPane, c);

				c.fill = GridBagConstraints.HORIZONTAL;

				//				c.gridx++;
				c.gridy = 1;
				//				c.insets = new Insets(10, 0, 0, 10);
				is.addPlot = new JButton(((char)8615) + ""); // "\\/");
				is.addAllPlot = new JButton(((char)8615) + "" + ((char)8615) + "" + ((char)8615)); // "\\/\\/");
				is.remPlot = new JButton(((char)8613) + ""); // "/\\");
				is.remAllPlot = new JButton(((char)8613) + "" + ((char)8613) + "" + ((char)8613)); // "/\\/\\");
				is.update = new JButton("Update");

				JPanel pan = new JPanel(new GridLayout(1, 2));
				pan.add(is.addAllPlot);
				pan.add(is.addPlot);
				pan.add(is.remPlot);
				pan.add(is.remAllPlot);

				configurationPan.add(pan, c);

				//				configurationPan.add(is.addPlot, c);
				//				c.gridy = 1;
				//				configurationPan.add(is.remPlot, c);
				//				c.gridy = 3;
				//				configurationPan.add(is.update, c);

				is.addPlot.addActionListener(this);
				is.remPlot.addActionListener(this);
				is.addAllPlot.addActionListener(this);
				is.remAllPlot.addActionListener(this);
				is.update.addActionListener(this);

				c.fill = GridBagConstraints.BOTH;

				//				c.gridx++;
				c.gridy = 2;
				//				c.insets = new Insets(10, 0, 10, 10);

				is.plotsModel = new DefaultListModel<String>();
				is.plots = new JList<String>(is.plotsModel);
				is.plots.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				is.plots.setSelectedIndex(-1);
				is.plots.setVisibleRowCount(10);
				listScrollPane = new JScrollPane(is.plots);
				listScrollPane.setBorder(new TitledBorder(null, "Plot",
						TitledBorder.LEADING, TitledBorder.TOP, null, null));

				//				c.gridheight = GridBagConstraints.REMAINDER;
				configurationPan.add(listScrollPane, c);

				c.insets = new Insets(10, 10, 10, 10);
				c.gridy = 3;
				configurationPan.add(is.update, c);

				//Create a split pane with the two scroll panes in it.
				JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
						imageContainerPanel, configurationPan) {

					/**
					 * 
					 */
					private static final long serialVersionUID = 7718710732162710507L;

					@Override
					public int getDividerLocation() {
						int widthFrame = frame.getSize().width;
						int location = super.getDividerLocation();
						int diff = widthFrame - location;
						int border = (widthFrame - getSize().width) + getInsets().right + getDividerSize();

						if (location >= 0 && location < 400)
							return 400;
						if (diff < 350 && diff > border)
							return widthFrame - 350;

						return super.getDividerLocation();
					}

				};
				splitPane.setOneTouchExpandable(true);
				splitPane.setResizeWeight(1.0);
				splitPane.setDividerLocation(frame.getSize().width - 350);

				splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, 
						new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent pce) {
						updateImages();
					}
				});

				BasicSplitPaneUI ui = (BasicSplitPaneUI)splitPane.getUI();
				BasicSplitPaneDivider divider = ui.getDivider();
				JButton button = (JButton)divider.getComponent(1);
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						updateImages();
					}
				});

				//Provide minimum sizes for the two components in the split pane
				Dimension minimumSize = new Dimension(100, 50);
				imageContainerPanel.setMinimumSize(minimumSize);
				configurationPan.setMinimumSize(minimumSize);
				//				imageContainerPanel.setMinimumSize(new Dimension(700, 50));
				//				configurationPan.setMinimumSize(new Dimension());
				//				splitPane.setDividerLocation(1.0d);

				tab.addTab(provider, splitPane);
			}

			{
				for (Tier t : providedSolution.getApplication(0).getTiers()) {
					if (is.constrained(t.getId())) //t.getName()))
						continue;

					double sum = 0.0;
					for (int i = 0; i < 24; i++) {
						sum += ((Compute) providedSolution.getApplication(i).getTierById(t.getId()).getCloudService()).getUtilization();
					}

					is.plotsModel.addElement(t.getName() + " (" + Math.round(sum/24*100) + "%)");
				}

				HashMap<String, Double> sums = new HashMap<String, Double>();
				HashMap<String, String> ids = new HashMap<String, String>();

				for (Tier t : providedSolution.getApplication(0).getTiers()) {
					for (int i = 0; i < 24; i++)
						for (Component c : providedSolution.getApplication(i).getTierById(t.getId()).getComponents())
							for (Functionality f : c.getFunctionalities()) {
								if (f.isEvaluated()) {
									Double sum = sums.get(f.getName());
									if (sum == null)
										sum = 0.0;

									sum += f.getResponseTime();

									sums.put(f.getName(), sum);
									ids.put(f.getName(), f.getId());
								}
							}
				}

				DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
				otherSymbols.setDecimalSeparator('.');
				DecimalFormat formatter = new DecimalFormat("0.000", otherSymbols);

				for (String key : sums.keySet()) {
					if (!is.constrained(ids.get(key) /*key*/))
						is.plotsModel.addElement(key + " (" + formatter.format(sums.get(key)/24) + " ms)");
				}
			}

			//			{
			//				for (int hour = 0; hour < 24; ++hour) {
			//					workload.addValue(providedSolution.getPercentageWorkload(hour), provider, "" + hour);
			//				}
			//			}

		}

		{
			JPanel details = new JPanel(new GridLayout(2, 1));
			tab.addTab("Details", details);

			for (int hour = 0; hour < 24; ++hour) {
				boolean goOn = true;
				for (int i = 0; i < solution.size() && goOn; ++i) {
					Solution s = solution.get(i);
					double wp = s.getPercentageWorkload(hour);
					if (wp > 0) {
						int workload = (int)Math.round((double)s.getApplication(hour).getWorkload() / wp);
						this.workload.addValue(workload, "Workload", "" + hour);
						goOn = false;
					}
				}
				availability.addValue(0.95, "Availability", "" + hour);
			}

			workloadPanel = new JPanel();
			details.add(workloadPanel);
			workloadLabel = new JLabel();
			workloadLabel.setIcon(null);
			workloadPanel.add(workloadLabel);

			availabilityPanel = new JPanel();
			details.add(availabilityPanel);
			availabilityLabel = new JLabel();
			availabilityLabel.setIcon(null);
			availabilityPanel.add(availabilityLabel);
		}
		updateGraphs();

		frame.setVisible(true);
		frame.validate();

		updateImages();

		pcs.firePropertyChange("AssessmentEnded", false, true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle(FRAME_NAME);
		//		frame.setBounds(100, 100, 450, 300);
		frame.setMinimumSize(new Dimension(900, 600));
		frame.setLocationRelativeTo(null);
		//		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(this);

		tab = new JTabbedPane();
		frame.getContentPane().add(tab);

		// listener to resize images
		frame.addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent e) { }

			@Override
			public void componentMoved(ComponentEvent e) { }

			@Override
			public void componentResized(ComponentEvent e) {
				updateImages();
			}

			@Override
			public void componentShown(ComponentEvent e) { }
		});
	}

	public void show() {
		frame.setVisible(true);
	}

	private boolean alreadyUpdating;

	private void updateImages() {
		if (alreadyUpdating)
			return;

		alreadyUpdating = true;

		for (String provider : solutions.keySet()) {
			InternalSolution is = solutions.get(provider);

			if (is.rtLogger != null) {
				is.rtImgLabel.setIcon(new ImageIcon(is.rtLogger.save2buffer(is.rtPanel
						.getSize())));
				is.rtImgLabel.setVisible(true);
				is.rtPanel.setPreferredSize(is.rtImgLabel.getPreferredSize());
			}

			if (is.vmLogger != null) {
				is.vmImgLabel.setIcon(new ImageIcon(is.vmLogger.save2buffer(is.vmPanel
						.getSize())));
				is.vmImgLabel.setVisible(true);
				is.vmPanel.setPreferredSize(is.vmImgLabel.getPreferredSize());
			}

			if (is.utilLogger != null) {
				is.utilImgLabel.setIcon(new ImageIcon(is.utilLogger.save2buffer(is.utilPanel
						.getSize())));
				is.utilImgLabel.setVisible(true);
				is.utilPanel.setPreferredSize(is.utilImgLabel.getPreferredSize());
			}
		}

		if (workload != null) {
			ImageIcon icon;
			try {
				icon = new ImageIcon(workloadGraph.createBufferedImage(
						workloadPanel.getSize().width,
						workloadPanel.getSize().height));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
			}
			workloadLabel.setIcon(icon);
			workloadLabel.setVisible(true);
			workloadPanel
			.setPreferredSize(workloadLabel.getPreferredSize());

			workloadLabel.validate();
		}

		if (availability != null) {
			ImageIcon icon;
			try {
				icon = new ImageIcon(availabilityGraph.createBufferedImage(
						availabilityPanel.getSize().width,
						availabilityPanel.getSize().height));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
			}
			availabilityLabel.setIcon(icon);
			availabilityLabel.setVisible(true);
			availabilityPanel
			.setPreferredSize(availabilityLabel.getPreferredSize());

			availabilityLabel.validate();
		}

		alreadyUpdating = false;
	}

	@Override
	public void windowClosing(WindowEvent e) {		
		super.windowClosing(e);
		frame.dispose();
		pcs.firePropertyChange("WindowClosed", false, true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) { }

	public void addPropertyChangeListener(PropertyChangeListener listener){
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		for (String provider : solutions.keySet()) {
			InternalSolution is = solutions.get(provider);

			if (e.getSource().equals(is.addPlot)) {
				int[] val = is.sources.getSelectedIndices();
				for (int i = 0; i < val.length; ++i) {
					String el = is.sourcesModel.getElementAt(val[i]);
					is.plotsModel.addElement(el);
				}
				for (int i = val.length - 1; i >= 0; --i) {
					is.sourcesModel.removeElementAt(val[i]);
				}
			} else if (e.getSource().equals(is.remPlot)) {
				int[] val = is.plots.getSelectedIndices();
				for (int i = 0; i < val.length; ++i) {
					String el = is.plotsModel.getElementAt(val[i]);
					is.sourcesModel.addElement(el);
				}
				for (int i = val.length - 1; i >= 0; --i) {
					is.plotsModel.removeElementAt(val[i]);
				}
			} else if (e.getSource().equals(is.update)) {
				try {
					updateGraphs();
				} catch (NumberFormatException | IOException e1) {
					e1.printStackTrace();
				}

				updateImages();
			} else if (e.getSource().equals(is.addAllPlot)) {
				while (is.sourcesModel.size() > 0) {
					String el = is.sourcesModel.getElementAt(0);
					is.plotsModel.addElement(el);
					is.sourcesModel.removeElementAt(0);
				}
			} else if (e.getSource().equals(is.remAllPlot)) {
				while (is.plotsModel.size() > 0) {
					String el = is.plotsModel.getElementAt(0);
					is.sourcesModel.addElement(el);
					is.plotsModel.removeElementAt(0);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void updateGraphs() throws NumberFormatException, IOException {
		if (alreadyUpdating)
			return;

		alreadyUpdating = true;

		for (Solution providedSolution : solutionMulti.getAll()) {
			String provider = providedSolution.getProvider();
			InternalSolution is = solutions.get(provider);

			// plotting the number of VMs
			is.vmLogger = new Logger2JFreeChartImage(
					"vmCount.properties");
			Map<String, SeriesHandle> vmSeriesHandlers = new HashMap<>();
			for (Tier t : providedSolution.getApplication(0).getTiers()) {
				if (is.constrained(t.getId() /*t.getName()*/) || is.toBeShown(t.getName()))
					vmSeriesHandlers.put(t.getId(), is.vmLogger.newSeries(t.getName()));
			}
			for (int i = 0; i < 24; i++) {
				for (Tier t : providedSolution.getApplication(i).getTiers()) {
					if (is.constrained(t.getId() /*t.getName()*/) || is.toBeShown(t.getName()))
						is.vmLogger.addPoint2Series(vmSeriesHandlers.get(t.getId()), i,
								((IaaS) t.getCloudService()).getReplicas());
				}
			}

			// plotting the response Times
			is.rtLogger = new Logger2JFreeChartImage(
					"responseTime.properties");
			Map<String, SeriesHandle> rtSeriesHandlers = new HashMap<>();
			for (Tier t : providedSolution.getApplication(0).getTiers())
				for (Component c : t.getComponents())
					for (Functionality f : c.getFunctionalities())
						if (is.constrained(f.getId() /*f.getName()*/) || is.toBeShown(f.getName()))
							rtSeriesHandlers.put(f.getName(),
									is.rtLogger.newSeries(f.getName()));

			for (int i = 0; i < 24; i++)
				for (Tier t : providedSolution.getApplication(i).getTiers())
					for (Component c : t.getComponents())
						for (Functionality f : c.getFunctionalities()){
							if (f.isEvaluated() && (is.constrained(f.getId() /*f.getName()*/) || is.toBeShown(f.getName())))
								is.rtLogger.addPoint2Series(
										rtSeriesHandlers.get(f.getName()), i,
										f.getResponseTime());
						}

			// plotting the utilization
			is.utilLogger = new Logger2JFreeChartImage(
					"utilization.properties");
			Map<String, SeriesHandle> utilSeriesHandlers = new HashMap<>();
			for (Tier t : providedSolution.getApplication(0).getTiers())
				if (is.constrained(t.getId() /*t.getName()*/) || is.toBeShown(t.getName()))
					utilSeriesHandlers.put(t.getId(), is.utilLogger.newSeries(t.getName()));

			for (int i = 0; i < 24; i++)
				for (Tier t : providedSolution.getApplication(i).getTiers())
					if (is.constrained(t.getId() /*t.getName()*/) || is.toBeShown(t.getName()))
						is.utilLogger.addPoint2Series(utilSeriesHandlers.get(t.getId()),
								i, ((Compute) t.getCloudService()).getUtilization());
		}

		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
		BasicStroke stroke = new BasicStroke(2.0f,                     // Line width
				BasicStroke.CAP_ROUND,     // End-cap style
				BasicStroke.JOIN_ROUND);   // Vertex join style

		workloadGraph = ChartFactory.createLineChart("Workload", "Hour", 
				"Y", workload, PlotOrientation.VERTICAL, true, true, false);
		{
			CategoryPlot plot = (CategoryPlot) workloadGraph.getPlot();
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
					.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.white);
			renderer.setStroke(stroke);

			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setTickLabelFont(font);

			//			rangeAxis.setRange(-0.1, 1.1);

			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, tmp;
			for (int i = 0; i < workload.getColumnCount(); ++i)
				for (int j = 0; j < workload.getRowCount(); ++j) {
					tmp = workload.getValue(j, i).intValue();
					if (tmp < min)
						min = tmp;
					if (tmp > max)
						max = tmp;
				}
			if (min == Integer.MAX_VALUE)
				min = 0;
			if (max == Integer.MIN_VALUE)
				max = (int) rangeAxis.getRange().getUpperBound() + 1;

			rangeAxis.setRange(/* 0 */min - 100, /*
			 * rangeAxis.getRange().
			 * getUpperBound() + 1
			 */max + 100);



			CategoryAxis categoryAxis = plot.getDomainAxis();
			categoryAxis.setLowerMargin(0.02);
			categoryAxis.setUpperMargin(0.02);
			categoryAxis.setTickLabelFont(font);

			CategoryItemRenderer renderer2 = plot
					.getRenderer();
			CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
					"{2}", new DecimalFormat("0") {

						private static final long serialVersionUID = 1L;

						@Override
						public StringBuffer format(double number,
								StringBuffer result, FieldPosition fieldPosition) {
							result = new StringBuffer((int)Math.round(number) + "");
							return result;
						}

					});
			renderer2.setItemLabelGenerator(generator);
			renderer2.setItemLabelsVisible(true);
			renderer2.setItemLabelFont(font);
		}

		availabilityGraph = ChartFactory.createLineChart("Availability", "Hour", 
				"Y", availability, PlotOrientation.VERTICAL, true, true, false);
		{
			CategoryPlot plot = (CategoryPlot) availabilityGraph.getPlot();
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
					.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setDrawOutlines(true);
			renderer.setUseFillPaint(true);
			renderer.setFillPaint(Color.white);
			renderer.setStroke(stroke);

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

		alreadyUpdating = false;
	}

}