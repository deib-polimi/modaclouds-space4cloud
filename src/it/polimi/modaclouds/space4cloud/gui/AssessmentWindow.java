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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
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
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.jfree.data.category.DefaultCategoryDataset;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.modaclouds.space4cloud.chart.GenericChart;
import it.polimi.modaclouds.space4cloud.db.DataHandlerFactory;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnectionFailureExteption;
import it.polimi.modaclouds.space4cloud.optimization.constraints.AvgRTConstraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.Constraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.constraints.UsageConstraint;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Component;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Functionality;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4cloud.utils.Configuration;

public class AssessmentWindow extends WindowAdapter implements
        PropertyChangeListener, ActionListener, ComponentListener {

    private class InternalSolution {
//        @SuppressWarnings("unused")
        String provider;

        GenericChart<DefaultCategoryDataset> vmLogger, rtLogger, utilLogger;

        DefaultListModel<String> sourcesModel;
        JList<String> sources;

        DefaultListModel<String> plotsModel;
        JList<String> plots;

        JButton addPlot;
        JButton remPlot;
        JButton addAllPlot;
        JButton remAllPlot;
        JButton update;
        JButton save;

        boolean isConstrained(String resourceId) {
            if (constraintHandler == null || solutionMulti == null)
                return false;

            List<Constraint> constraints = constraintHandler
                    .getConstraintByResourceId(resourceId);

            for (Constraint c : constraints) {
                if (c instanceof UsageConstraint) {
                    UsageConstraint constraint = (UsageConstraint) c;
                    if (constraint.getMax() == 1.0)
                    	return constraints.remove(constraint);
                }
            }
            
            return (constraints.size() > 0);
        }

        boolean toBeShown(String name) {
            for (int i = 0; i < plotsModel.size(); ++i) {
                String key = plotsModel.get(i);
                String elemName = key.substring(0, key.indexOf(" ("));
                if (name.equals(elemName))
                    return true;
            }
            return false;
        }

        Double constraint(String resourceId) {
            if (constraintHandler == null || solutionMulti == null)
                return null;

            List<Constraint> constraints = constraintHandler
                    .getConstraintByResourceId(resourceId);

            if (constraints.size() == 0)
                return null;

            for (Constraint c : constraints) {
                if (c instanceof AvgRTConstraint) {
                    AvgRTConstraint constraint = (AvgRTConstraint) c;
                    return constraint.getMax();
                } else if (c instanceof UsageConstraint) {
                    UsageConstraint constraint = (UsageConstraint) c;
                    if (constraint.getMax() != 1.0)
                    	return constraint.getMax();
                }
            }

            return null;
        }
    }

    private JFrame frame;

    private JTabbedPane tab;

    private static final Logger logger = LoggerFactory
            .getLogger(AssessmentWindow.class);

    private GenericChart<DefaultCategoryDataset> workload;
    private GenericChart<DefaultCategoryDataset> availability;

    private HashMap<String, InternalSolution> solutions = new HashMap<String, InternalSolution>();

    public final static String FRAME_NAME = "Assessment Results Window";

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Timer updateTimer;

    private SolutionMulti solutionMulti = null;

    private ConstraintHandler constraintHandler;
    private boolean alreadyUpdating = false;

    private boolean alreadyUpdatingImages = false;

    /**
     * Create the application.
     */
    public AssessmentWindow(ConstraintHandler constraintHandler) {
        this.constraintHandler = constraintHandler;
        updateTimer = new Timer(100, this);
        initialize();
        logger.trace("Assesment Window Created");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(updateTimer)) {
            logger.trace("Imgae update timeout ended");
            updateImages();
        } else {
            for (String provider : solutions.keySet()) {
                InternalSolution is = solutions.get(provider);
                if (e.getSource().equals(is.addPlot)) {
                    logger.trace("Add plot event");
                    int[] val = is.sources.getSelectedIndices();
                    for (int i = 0; i < val.length; ++i) {
                        String el = is.sourcesModel.get(val[i]);
                        is.plotsModel.addElement(el);
                    }
                    for (int i = val.length - 1; i >= 0; --i) {
                        is.sourcesModel.remove(val[i]);
                    }
                } else if (e.getSource().equals(is.remPlot)) {
                    logger.trace("Remove plot event");
                    int[] val = is.plots.getSelectedIndices();
                    for (int i = 0; i < val.length; ++i) {
                        String el = is.plotsModel.get(val[i]);
                        is.sourcesModel.addElement(el);
                    }
                    for (int i = val.length - 1; i >= 0; --i) {
                        is.plotsModel.remove(val[i]);
                    }
                } else if (e.getSource().equals(is.update)) {
                    logger.trace("Update Button Event");
                    try {
                        redrawGraphs(is);
                        updateGraphs(is);
                    } catch (NumberFormatException | IOException e1) {
                        logger.error("Exception updating graphs ",e);
                    }

                    updateImages(is);
                } else if (e.getSource().equals(is.addAllPlot)) {
                    logger.trace("Add all plot event");
                    while (is.sourcesModel.size() > 0) {
                        String el = is.sourcesModel.get(0);
                        is.plotsModel.addElement(el);
                        is.sourcesModel.remove(0);
                    }
                } else if (e.getSource().equals(is.remAllPlot)) {
                    logger.trace("Remove all plot event");
                    while (is.plotsModel.size() > 0) {
                        String el = is.plotsModel.get(0);
                        is.sourcesModel.addElement(el);
                        is.plotsModel.remove(0);
                    }
                } else if (e.getSource().equals(is.save)) {
                    logger.trace("Save Button Event");
                    try {
                        updateGraphs(is);
                        is.rtLogger.save2png("images/responseTimes-" + is.provider + ".png");
                        is.utilLogger.save2png("images/cpuUtilizations-" + is.provider + ".png");
                        is.vmLogger.save2png("images/vms-" + is.provider + ".png");
                        
                        JOptionPane.showMessageDialog(frame,
                        		"Images saved in " + Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY, "images").toString(),
                        		FRAME_NAME,
                        		JOptionPane.INFORMATION_MESSAGE);
                    } catch (NumberFormatException | IOException e1) {
                        logger.error("Exception saving graphs ",e);
                    }
                } 
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

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
        logger.trace("Compoenent Resized (picture update timer reset)");
        frame.setSize(e.getComponent().getSize());
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // TODO Auto-generated method stub

    }

    public void considerSolution(SolutionMulti solution)
            throws NumberFormatException, IOException {
        logger.trace("Considering new solution with generation time: "+solution.getGenerationTime());
        frame.setVisible(false);

        solutions.clear();
        tab.removeAll();

        workload = new GenericChart<DefaultCategoryDataset>("Workload", "Hour",
                "Population");
        workload.dataset = new DefaultCategoryDataset();
        workload.labelsVisible = false;
        workload.pointsVisible = false;
        workload.defaultRange = true;

        availability = new GenericChart<DefaultCategoryDataset>("Availability",
                "Hour", "Availability") {
            private static final long serialVersionUID = -1913979813319452034L;

            public String getFormattedValue(double number) {
                return (int) (number * 100) + "%";
            }
        };
        availability.dataset = new DefaultCategoryDataset();
        availability.exactMax = 1.1;
        availability.exactMin = 0.0;
        availability.labelsVisible = false;
        availability.pointsVisible = false;

        this.solutionMulti = solution;

        double unavailabilityTot = 1;

        for (Solution providedSolution : solution.getAll()) {
            logger.trace("Adding solution: "+providedSolution.getProvider());
            String provider = providedSolution.getProvider();

            InternalSolution is = new InternalSolution();
            is.provider = provider;

            solutions.put(provider, is);

            JPanel imageContainerPanel = new JPanel();
            imageContainerPanel.setLayout(new GridLayout(3, 1, 0, 0));

            is.vmLogger = new GenericChart<DefaultCategoryDataset>(
                    "Number of VMs", "Hour", "VMs");
            is.vmLogger.dataset = new DefaultCategoryDataset();
            is.vmLogger.labelsVisible = false;
            is.vmLogger.pointsVisible = false;
//            is.vmLogger.defaultRange = true;
            is.vmLogger.shownValsAboveMax = 0.5;
            is.vmLogger.shownValsBelowMin = 0.5;
            
            imageContainerPanel.add(is.vmLogger);

            is.utilLogger = new GenericChart<DefaultCategoryDataset>(
                    "CPU Utilization", "Hour", "Utilization");
            is.utilLogger.dataset = new DefaultCategoryDataset();
            is.utilLogger.labelsVisible = false;
            is.utilLogger.pointsVisible = false;
            is.utilLogger.defaultRange = true;
            imageContainerPanel.add(is.utilLogger);

            is.rtLogger = new GenericChart<DefaultCategoryDataset>(
                    "Average Response Times", "Hour", "Response Times");
            is.rtLogger.dataset = new DefaultCategoryDataset();
            is.rtLogger.labelsVisible = false;
            is.rtLogger.pointsVisible = false;
            is.rtLogger.defaultRange = true;
            imageContainerPanel.add(is.rtLogger);

            {
                GridBagLayout gridBagLayout = new GridBagLayout();
                gridBagLayout.columnWidths = new int[] { 201, 0 };
                gridBagLayout.rowHeights = new int[] { 70, 40, 70, 40, 40, 0 };
                gridBagLayout.columnWeights = new double[] { 1.0,
                        Double.MIN_VALUE };
                gridBagLayout.rowWeights = new double[] { 1.0, 0.0, 1.0, 0.0, 0.0, 
                        Double.MIN_VALUE };
                GridBagConstraints c = new GridBagConstraints();

                JPanel configurationPan = new JPanel(gridBagLayout);

                c.fill = GridBagConstraints.BOTH;

                c.gridx = 0;
                c.gridy = 0;
                c.insets = new Insets(10, 10, 0, 10);

                is.sourcesModel = new DefaultListModel<String>();
                is.sources = new JList<String>(is.sourcesModel);
                is.sources
                        .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                is.sources.setSelectedIndex(-1);
                is.sources.setVisibleRowCount(10);
                JScrollPane listScrollPane = new JScrollPane(is.sources);
                listScrollPane.setBorder(new TitledBorder(null, "Source",
                        TitledBorder.LEADING, TitledBorder.TOP, null, null));

                // c.gridheight = GridBagConstraints.REMAINDER;
                configurationPan.add(listScrollPane, c);

                c.fill = GridBagConstraints.HORIZONTAL;

                // c.gridx++;
                c.gridy = 1;
                // c.insets = new Insets(10, 0, 0, 10);
                is.addPlot = new JButton(((char) 8615) + ""); // "\\/");
                is.addAllPlot = new JButton(((char) 8615) + "" + ((char) 8615)
                        + "" + ((char) 8615)); // "\\/\\/");
                is.remPlot = new JButton(((char) 8613) + ""); // "/\\");
                is.remAllPlot = new JButton(((char) 8613) + "" + ((char) 8613)
                        + "" + ((char) 8613)); // "/\\/\\");
                is.update = new JButton("Update");
                is.save = new JButton("Save graphs as images");

                JPanel pan = new JPanel(new GridLayout(1, 2));
                pan.add(is.addAllPlot);
                pan.add(is.addPlot);
                pan.add(is.remPlot);
                pan.add(is.remAllPlot);

                configurationPan.add(pan, c);

                is.addPlot.addActionListener(this);
                is.remPlot.addActionListener(this);
                is.addAllPlot.addActionListener(this);
                is.remAllPlot.addActionListener(this);
                is.update.addActionListener(this);
                is.save.addActionListener(this);

                c.fill = GridBagConstraints.BOTH;

                // c.gridx++;
                c.gridy = 2;
                // c.insets = new Insets(10, 0, 10, 10);

                is.plotsModel = new DefaultListModel<String>();
                is.plots = new JList<String>(is.plotsModel);
                is.plots.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                is.plots.setSelectedIndex(-1);
                is.plots.setVisibleRowCount(10);
                listScrollPane = new JScrollPane(is.plots);
                listScrollPane.setBorder(new TitledBorder(null, "Plot",
                        TitledBorder.LEADING, TitledBorder.TOP, null, null));

                // c.gridheight = GridBagConstraints.REMAINDER;
                configurationPan.add(listScrollPane, c);

                c.insets = new Insets(10, 10, 0, 10);
                c.gridy = 3;
                configurationPan.add(is.update, c);
                
                c.insets = new Insets(0, 10, 10, 10);
                c.gridy = 4;
                configurationPan.add(is.save, c);

                // Create a split pane with the two scroll panes in it.
                JSplitPane splitPane = new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT, imageContainerPanel,
                        configurationPan) {

                    /**
                     *
                     */
                    private static final long serialVersionUID = 7718710732162710507L;

                    @Override
                    public int getDividerLocation() {
                        int widthFrame = frame.getSize().width;
                        int location = super.getDividerLocation();
                        int diff = widthFrame - location;
                        int border = (widthFrame - getSize().width)
                                + getInsets().right + getDividerSize();

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

                splitPane.addPropertyChangeListener(
                        JSplitPane.DIVIDER_LOCATION_PROPERTY,
                        new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent pce) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateImages();
                                    }
                                });
                            }
                        });

                BasicSplitPaneUI ui = (BasicSplitPaneUI) splitPane.getUI();
                BasicSplitPaneDivider divider = ui.getDivider();
                JButton button = (JButton) divider.getComponent(1);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateImages();
                    }
                });

                // Provide minimum sizes for the two components in the split
                // pane
                Dimension minimumSize = new Dimension(100, 50);
                imageContainerPanel.setMinimumSize(minimumSize);
                configurationPan.setMinimumSize(minimumSize);
                // imageContainerPanel.setMinimumSize(new Dimension(700, 50));
                // configurationPan.setMinimumSize(new Dimension());
                // splitPane.setDividerLocation(1.0d);

                tab.addTab(provider, splitPane);
            }

            {
                for (Tier t : providedSolution.getApplication(0).getTiers()) {
                    double sum = 0.0;
                    for (int i = 0; i < 24; i++) {
                    	Tier tmp = providedSolution.getApplication(i).getTierById(t.getId());
                        sum += tmp.getUtilization();
                    }

                    if (is.isConstrained(t.getId())) {
                        Double constraint = is.constraint(t.getId());
                        if (constraint != null)
                            is.plotsModel.addElement(t.getPcmName() + " ("
                                    + Math.round(sum / 24 * 100) + "%, c: "
                                    + Math.round(constraint * 100) + "%)");
                        else
                            is.plotsModel.addElement(t.getPcmName() + " ("
                                    + Math.round(sum / 24 * 100) + "%)");
                    } else
                        is.sourcesModel.addElement(t.getPcmName() + " ("
                                + Math.round(sum / 24 * 100) + "%)");
                }

                HashMap<String, Double> sums = new HashMap<String, Double>();
                HashMap<String, String> ids = new HashMap<String, String>();

                for (Tier t : providedSolution.getApplication(0).getTiers()) {
                    for (int i = 0; i < 24; i++)
                        for (Component c : providedSolution.getApplication(i)
                                .getTierById(t.getId()).getComponents())
                            for (Functionality f : c.getFunctionalities()) {
                                if (f.isEvaluated()) {
                                    Double sum = sums.get(c.getName() + ":" + f.getName());
                                    if (sum == null)
                                        sum = 0.0;

                                    sum += f.getResponseTime();

                                    sums.put(c.getName() + ":" + f.getName(),
                                            sum);
                                    ids.put(c.getName() + ":" + f.getName(),
                                            f.getId());
                                }
                            }
                }

                DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(
                        Locale.getDefault());
                otherSymbols.setDecimalSeparator('.');
                DecimalFormat formatter = new DecimalFormat("0.000",
                        otherSymbols);

                for (String key : sums.keySet()) {
                    if (is.isConstrained(ids.get(key))) {
                        Double constraint = is.constraint(ids.get(key));
                        if (constraint != null)
                            is.plotsModel.addElement(key + " ("
                                    + formatter.format(sums.get(key) / 24)
                                    + " s, c: " + constraint + " s)");
                        else
                            is.plotsModel.addElement(key + " ("
                                    + formatter.format(sums.get(key) / 24)
                                    + " s)");
                    } else
                        is.sourcesModel.addElement(key + " ("
                                + formatter.format(sums.get(key) / 24) + " s)");
                }
            }

            try {
                unavailabilityTot *= (1 - DataHandlerFactory.getHandler()
                        .getAvailability(provider));
            } catch (DatabaseConnectionFailureExteption e) {
                logger.error("Error computing unavailability: ",e);
            }

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
                        int workload = (int) Math.round((double) s
                                .getApplication(hour).getWorkload() / wp);
                        this.workload.dataset.addValue(workload, "Workload", ""
                                + hour);
                        goOn = false;
                    }
                }
                availability.dataset.addValue(1 - unavailabilityTot,
                        "Availability", "" + hour);
            }

            availability.markers.add(new GenericChart.Marker(1.0));

            details.add(workload);
            details.add(availability);
        }
        redrawGraphs();
        updateGraphs();

        frame.setVisible(true);
        frame.validate();

        updateTimer.restart();

        pcs.firePropertyChange("AssessmentEnded", false, true);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setTitle(FRAME_NAME);
        // frame.setBounds(100, 100, 450, 300);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);
        // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(this);
        Image favicon = new ImageIcon(FrameworkUtil.getBundle(
                ConfigurationWindow.class).getEntry("icons/Cloud.png"))
                .getImage();
        frame.setIconImage(favicon);
        logger.trace("Frame Created");
        tab = new JTabbedPane();
        frame.getContentPane().add(tab);

        // listener to resize images
        frame.addComponentListener(new ComponentListener() {

            @Override
            public void componentHidden(ComponentEvent e) {
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentResized(ComponentEvent e) {
                updateTimer.restart();
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }
        });
        logger.trace("Window Initialized");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        logger.trace("Property changed: "+evt.getPropertyName()+" value: "+evt.getNewValue());
    }

    private void redrawGraphs() throws NumberFormatException, IOException {
        logger.trace("Re-drawing graphs");
        if (alreadyUpdating)
            return;

        alreadyUpdating = true;

        for (Solution providedSolution : solutionMulti.getAll()) {
            String provider = providedSolution.getProvider();
            InternalSolution is = solutions.get(provider);

//            // plotting the number of VMs
//            is.vmLogger.clear();
//            for (int i = 0; i < 24; i++) {
//                for (Tier t : providedSolution.getApplication(i).getTiers()) {
//                    if (is.toBeShown(t.getPcmName())) {
//                        is.vmLogger.dataset.addValue(
//                                getReplicas(t),
//                                t.getPcmName(), "" + i);
//                    }
//                }
//            }
//
//            // plotting the response Times
//            is.rtLogger.clear();
//            for (int i = 0; i < 24; i++)
//                for (Tier t : providedSolution.getApplication(i).getTiers())
//                    for (Component c : t.getComponents())
//                        for (Functionality f : c.getFunctionalities()) {
//                            if (f.isEvaluated()
//                                    && is.toBeShown(c.getName() + ":"
//                                            + f.getName())) {
//                                if (is.isConstrained(f.getId())) {
//                                    Double constraint = is
//                                            .constraint(f.getId());
//                                    if (constraint != null)
//                                        is.rtLogger.markers
//                                                .add(new GenericChart.Marker(
//                                                        constraint, c.getName()
//                                                                + ":"
//                                                                + f.getName()));
//                                }
//                                is.rtLogger.dataset.addValue(
//                                        f.getResponseTime(), c.getName() + ":"
//                                                + f.getName(), "" + i);
//                            }
//                        }
//
//            // plotting the utilization
//            is.utilLogger.clear();
//            for (int i = 0; i < 24; i++)
//                for (Tier t : providedSolution.getApplication(i).getTiers())
//                    if (is.toBeShown(t.getPcmName())) {
//                        if (is.isConstrained(t.getId())) {
//                            Double constraint = is.constraint(t.getId());
//                            if (constraint != null)
//                                is.utilLogger.markers
//                                        .add(new GenericChart.Marker(
//                                                constraint, t.getPcmName()));
//                        }
//                        is.utilLogger.dataset.addValue(((Compute) t
//                                .getCloudService()).getUtilization(), t
//                                .getPcmName(), "" + i);
//                    }
            redrawGraphs(is);
        }

        alreadyUpdating = false;
    }
    
    private void redrawGraphs(InternalSolution is) throws NumberFormatException, IOException {
        Solution providedSolution = solutionMulti.get(is.provider);

        // plotting the number of VMs
        is.vmLogger.clear();
        for (int i = 0; i < 24; i++) {
            for (Tier t : providedSolution.getApplication(i).getTiers()) {
                if (is.toBeShown(t.getPcmName())) {
                    is.vmLogger.dataset.addValue(
                            getReplicas(t),
                            t.getPcmName(), "" + i);
                }
            }
        }

        // plotting the response Times
        is.rtLogger.clear();
        
        for (Tier t : providedSolution.getApplication(0).getTiers())
            for (Component c : t.getComponents())
                for (Functionality f : c.getFunctionalities()) {
                    if (f.isEvaluated()
                            && is.toBeShown(c.getName() + ":"
                                    + f.getName())) {
                        if (is.isConstrained(f.getId())) {
                            Double constraint = is
                                    .constraint(f.getId());
                            if (constraint != null)
//                                is.rtLogger.markers
//                                        .add(new GenericChart.Marker(
//                                                constraint, c.getName()
//                                                        + ":"
//                                                        + f.getName()));
                            	is.rtLogger.addMarker(constraint, c.getName()
                            							+ ":"
                            							+ f.getName());
                        }
                    }
                }
        
        
        for (int i = 0; i < 24; i++)
            for (Tier t : providedSolution.getApplication(i).getTiers())
                for (Component c : t.getComponents())
                    for (Functionality f : c.getFunctionalities()) {
                        if (f.isEvaluated()
                                && is.toBeShown(c.getName() + ":"
                                        + f.getName())) {
                            is.rtLogger.dataset.addValue(
                                    f.getResponseTime(), c.getName() + ":"
                                            + f.getName(), "" + i);
                        }
                    }

        // plotting the utilization
        is.utilLogger.clear();
        
        for (Tier t : providedSolution.getApplication(0).getTiers())
            if (is.toBeShown(t.getPcmName())) {
                if (is.isConstrained(t.getId())) {
                    Double constraint = is.constraint(t.getId());
                    if (constraint != null)
//                        is.utilLogger.markers
//                                .add(new GenericChart.Marker(
//                                        constraint, t.getPcmName()));
                    	is.utilLogger.addMarker(
                    			constraint, t.getPcmName());
                }
            }
        
        for (int i = 0; i < 24; i++)
            for (Tier t : providedSolution.getApplication(i).getTiers())
                if (is.toBeShown(t.getPcmName())) {
                    is.utilLogger.dataset.addValue(t.getUtilization(), t
                            .getPcmName(), "" + i);
                }
    }

    public void show() {
        frame.setVisible(true);
        logger.trace("Frame Visible!");
    }

    private void updateGraphs() throws NumberFormatException, IOException {
        logger.trace("Updating graphics");
        if (alreadyUpdating)
            return;

        alreadyUpdating = true;

        for (String key : solutions.keySet()) {
            InternalSolution is = solutions.get(key);

//            is.vmLogger.updateGraph();
//            is.rtLogger.updateGraph();
//            is.utilLogger.updateGraph();
            updateGraphs(is);
        }

        workload.updateGraph();
        availability.updateGraph();

        alreadyUpdating = false;
    }
    
    private void updateGraphs(InternalSolution is) throws NumberFormatException, IOException {
    	is.vmLogger.updateGraph();
        is.rtLogger.updateGraph();
        is.utilLogger.updateGraph();
    }

    private void updateImages() {
        logger.trace("Updating Images");
        if (alreadyUpdatingImages) {
            updateTimer.restart();
            return;
        }

        alreadyUpdatingImages = true;

        for (String provider : solutions.keySet()) {
        	
            InternalSolution is = solutions.get(provider);

//            if (is.rtLogger != null) {
//                is.rtLogger.updateImage();
//            }
//
//            if (is.vmLogger != null) {
//                is.vmLogger.updateImage();
//            }
//
//            if (is.utilLogger != null) {
//                is.utilLogger.updateImage();
//            }
            updateImages(is);	
        }

        if (workload != null) {
            workload.updateImage();
        }

        if (availability != null) {
            availability.updateImage();
        }

        alreadyUpdatingImages = false;
        updateTimer.stop();
    }
    
    private void updateImages(InternalSolution is) {
    	if (is.rtLogger != null) {
            is.rtLogger.updateImage();
        }

        if (is.vmLogger != null) {
            is.vmLogger.updateImage();
        }

        if (is.utilLogger != null) {
            is.utilLogger.updateImage();
        }
    }

    @Override
    public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        frame.dispose();
        pcs.firePropertyChange("WindowClosed", false, true);
        logger.trace("Window Closed");
    }

    private int getReplicas(Tier t) {
        return t.getCloudService().getReplicas();
    }

}
