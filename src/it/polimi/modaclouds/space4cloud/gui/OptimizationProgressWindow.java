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

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.jfree.data.xy.XYSeriesCollection;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.modaclouds.space4cloud.chart.GenericChart;

public class OptimizationProgressWindow extends WindowAdapter implements PropertyChangeListener, ActionListener {

	private static JFrame frmOptimizationProgress;

	JProgressBar progressBar;
	private JPanel upperPanel;
	private JPanel middlePanel;

	private GenericChart<XYSeriesCollection> vmLogger;
	private GenericChart<XYSeriesCollection> costLogger;
	private GenericChart<XYSeriesCollection> constraintsLogger;

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(OptimizationProgressWindow.class);

	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private JPanel lowerPane;
//	private JButton btnStop;
	private JButton btnInspectSolution;

	/**
	 * Create the application.
	 */
	public OptimizationProgressWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frmOptimizationProgress = new JFrame();
		frmOptimizationProgress.setTitle("Optimization");
		//		frmOptimizationProgress.setBounds(100, 100, 450, 300);
		frmOptimizationProgress.setMinimumSize(new Dimension(900, 600));
		frmOptimizationProgress.setLocationRelativeTo(null);
		//		frmOptimizationProgress.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frmOptimizationProgress.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frmOptimizationProgress.addWindowListener(this);
		//		frmOptimizationProgress.setExtendedState(frmOptimizationProgress.getExtendedState() | JFrame.MAXIMIZED_BOTH);

//		frmOptimizationProgress.setExtendedState(frmOptimizationProgress.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		Image favicon = new ImageIcon(FrameworkUtil.getBundle(ConfigurationWindow.class).getEntry("icons/Cloud.png")).getImage();
		frmOptimizationProgress.setIconImage(favicon);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{100, 0};
		gridBagLayout.rowHeights = new int[]{17, 100, 10, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		frmOptimizationProgress.getContentPane().setLayout(gridBagLayout);
		// Upper panel (for progress bar)
		upperPanel = new JPanel();
		GridBagConstraints gbc_upperPanel = new GridBagConstraints();
		gbc_upperPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_upperPanel.insets = new Insets(0, 0, 5, 0);
		gbc_upperPanel.gridx = 0;
		gbc_upperPanel.gridy = 0;
		frmOptimizationProgress.getContentPane().add(upperPanel, gbc_upperPanel);
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));

		// progress bar
		progressBar = new JProgressBar(0, 100);
		upperPanel.add(progressBar);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		// Lower panel (for images)
		middlePanel = new JPanel();
		GridBagConstraints gbc_middlePanel = new GridBagConstraints();
		gbc_middlePanel.fill = GridBagConstraints.BOTH;
		gbc_middlePanel.insets = new Insets(0, 0, 5, 0);
		gbc_middlePanel.gridx = 0;
		gbc_middlePanel.gridy = 1;
		frmOptimizationProgress.getContentPane().add(middlePanel, gbc_middlePanel);
		middlePanel.setLayout(new GridLayout(1, 3));

		//		middlePanel.add(vmLogger);
		//		
		//		middlePanel.add(costLogger);
		//		
		//		middlePanel.add(constraintsLogger);

		initializeGraphs();

		lowerPane = new JPanel();
		GridBagConstraints gbc_lowerPane = new GridBagConstraints();
		gbc_lowerPane.anchor = GridBagConstraints.NORTH;
		gbc_lowerPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_lowerPane.gridx = 0;
		gbc_lowerPane.gridy = 2;
		frmOptimizationProgress.getContentPane().add(lowerPane, gbc_lowerPane);

//		btnStop = new JButton("Pause");
//		btnStop.addActionListener(this);
//		lowerPane.add(btnStop);

		btnInspectSolution = new JButton("Inspect Solution");
		btnInspectSolution.setEnabled(false);
//		btnInspectSolution.setEnabled(true);
		btnInspectSolution.addActionListener(this);
		lowerPane.add(btnInspectSolution);

		// listener to resize images
		frmOptimizationProgress.addComponentListener(new ComponentListener() {

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

	public void setConstraintsLogger(GenericChart<XYSeriesCollection> constraintsLogger) {
		this.constraintsLogger = constraintsLogger;

		initializeGraphs();
	}

	public void setCostLogger(GenericChart<XYSeriesCollection> costLogger) {
		this.costLogger = costLogger;

		initializeGraphs();
	}

	private void initializeGraphs() {
		if (middlePanel == null)
			return;
		middlePanel.removeAll();
		if (vmLogger != null)
			middlePanel.add(vmLogger);
		if (costLogger != null)
			middlePanel.add(costLogger);
		if (constraintsLogger != null)
			middlePanel.add(constraintsLogger);

		updateGraphs();
		updateImages();
	}

	public void setMax(int max) {
		progressBar.setMaximum(max);

		frmOptimizationProgress.setVisible(true);
	}

	public void setVMLogger(GenericChart<XYSeriesCollection> vmLogger) {
		this.vmLogger = vmLogger;

		initializeGraphs();
	}

	private boolean alreadyUpdating = false;

	private void updateImages() {
		if (alreadyUpdating)
			return;

		alreadyUpdating = true;

		if (costLogger != null)
			costLogger.updateImage();

		if (vmLogger != null)
			vmLogger.updateImage();

		if (constraintsLogger != null)
			constraintsLogger.updateImage();

		alreadyUpdating = false;
	}

	public void updateGraphs() {
		if (alreadyUpdating)
			return;

		alreadyUpdating = true;

		if (costLogger != null)
			costLogger.updateGraph();

		if (vmLogger != null)
			vmLogger.updateGraph();

		if (constraintsLogger != null)
			constraintsLogger.updateGraph();

		alreadyUpdating = false;
	}

	private void updateProgressBar(int progress) {
		progressBar.setValue(progress);
	}

	public void signalCompletion() {
		JOptionPane.showMessageDialog(frmOptimizationProgress, "Optimization process compleated");	
//		btnStop.setText("Finished");
//		btnStop.setEnabled(false);
//		btnInspectSolution.setEnabled(false);
	}

	public static void signalError(String message) {
		JOptionPane.showMessageDialog(frmOptimizationProgress, message, "Error",  JOptionPane.ERROR_MESSAGE);		
	}

	public void addPropertyChangeListener(PropertyChangeListener listener){
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void windowClosing(WindowEvent e) {		
		super.windowClosing(e);
		frmOptimizationProgress.dispose();
		BestSolutionExplorer.close();
		pcs.firePropertyChange("WindowClosed", false, true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
//		if(e.getSource().equals(btnStop))
//			if(Configuration.isPaused()){
//				Configuration.resume();
//				btnStop.setText("Resuming...");
//				btnStop.setEnabled(false);		
//				progressBar.setEnabled(true);
//				btnInspectSolution.setEnabled(false);
//			}
//			else{
//				Configuration.pause();
//				btnStop.setText("Pausing...");
//				btnStop.setEnabled(false);
//				btnInspectSolution.setEnabled(true);
//				progressBar.setEnabled(false);
//			}
		/*else*/ if(e.getSource().equals(btnInspectSolution)){
			pcs.firePropertyChange("InspectSolution",false,true);
			btnInspectSolution.setEnabled(false);
		}		
	}

	public static final String FIRST_SOLUTION_AVAILABLE = "FirstSolutionAvailable";

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("progress")) {
			updateProgressBar((int) evt.getNewValue());
			updateImages();
		} else if (evt.getPropertyName().equals("totalNumberOfEvaluations")) {
			updateImages();
		} 	else if (evt.getPropertyName().equals(BestSolutionExplorer.PROPERTY_WINDOW_CLOSED)) {
			btnInspectSolution.setEnabled(true);
		} 	else if (evt.getPropertyName().equals(FIRST_SOLUTION_AVAILABLE)) {
			btnInspectSolution.setEnabled(true);
		} 	
		//forward stopped status to listeners (windows)
//		else if (evt.getPropertyName().equals("Stopped")){
//			if((boolean) evt.getNewValue()){
//				btnStop.setText("Resume");
//				btnStop.setEnabled(true);				
//			}else{
//				btnInspectSolution.setEnabled(false);
//				btnStop.setText("Pause");
//				btnStop.setEnabled(true);
//				progressBar.setEnabled(true);
//			}
//
//		}else {
//			logger.debug("property: " + evt.getPropertyName());
//		}

	}


}
