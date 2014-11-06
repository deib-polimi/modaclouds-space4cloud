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
import it.polimi.modaclouds.space4cloud.utils.Configuration;

import java.awt.Dimension;
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

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptimizationProgressWindow extends WindowAdapter implements PropertyChangeListener, ActionListener {

	private JFrame frmOptimizationProgress;

	JProgressBar progressBar;
	private JPanel upperPanel;
	private JPanel middlePanel;

	private JPanel vmPanel;
	private JLabel vmLabel;
	private JPanel costPanel;
	private JLabel costLabel;
	private JPanel constraintPanel;
	private JLabel constraintLabel;

	private Logger2JFreeChartImage costLogger;

	private Logger2JFreeChartImage vmLogger;

	private Logger2JFreeChartImage constraintsLogger;
	private static final Logger logger = LoggerFactory.getLogger(OptimizationProgressWindow.class);

	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private JPanel lowerPane;
	private JButton btnStop;
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
		frmOptimizationProgress.setMinimumSize(new Dimension(400, 300));
		frmOptimizationProgress.setLocationRelativeTo(null);
		frmOptimizationProgress
		.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		frmOptimizationProgress.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frmOptimizationProgress.addWindowListener(this);
		frmOptimizationProgress.setExtendedState(frmOptimizationProgress.getExtendedState() | JFrame.MAXIMIZED_BOTH);
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
		middlePanel.setLayout(new GridLayout(0, 3, 0, 0));

		// vmPanel for VM image
		vmPanel = new JPanel();
		vmPanel.setBorder(new TitledBorder(null, "Total Number of VMs",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		middlePanel.add(vmPanel);
		vmPanel.setLayout(new GridLayout(0, 1, 0, 0));

		// vmLabel for VM image
		vmLabel = new JLabel();
		vmLabel.setIcon(null);
		vmPanel.add(vmLabel);

		// Cost panel for cost image
		costPanel = new JPanel();
		costPanel.setBorder(new TitledBorder(null, "Solution Cost",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		middlePanel.add(costPanel);
		costPanel.setLayout(new GridLayout(0, 1, 0, 0));

		// Cost label for cost image
		costLabel = new JLabel();
		costLabel.setIcon(null);
		costPanel.add(costLabel);

		// Constraint panel for constraint image
		constraintPanel = new JPanel();
		constraintPanel.setBorder(new TitledBorder(null,
				"Violated Constraints", TitledBorder.LEADING, TitledBorder.TOP,
				null, null));
		middlePanel.add(constraintPanel);
		constraintPanel.setLayout(new GridLayout(0, 1, 0, 0));

		// Constraint label for constraint image
		constraintLabel = new JLabel();
		constraintLabel.setIcon(null);
		constraintPanel.add(constraintLabel);

		lowerPane = new JPanel();
		GridBagConstraints gbc_lowerPane = new GridBagConstraints();
		gbc_lowerPane.anchor = GridBagConstraints.NORTH;
		gbc_lowerPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_lowerPane.gridx = 0;
		gbc_lowerPane.gridy = 2;
		frmOptimizationProgress.getContentPane().add(lowerPane, gbc_lowerPane);

		btnStop = new JButton("Pause");
		btnStop.addActionListener(this);
		lowerPane.add(btnStop);

		btnInspectSolution = new JButton("Inspect Solution");
		btnInspectSolution.setEnabled(false);
		btnInspectSolution.addActionListener(this);
		lowerPane.add(btnInspectSolution);

		// listener to resize images
		frmOptimizationProgress.addComponentListener(new ComponentListener() {

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
				// TODO Auto-generated method stub
			}
		});


	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("progress")) {
			updateProgressBar((int) evt.getNewValue());
			updateImages();
		} else if (evt.getPropertyName().equals("totalNumberOfEvaluations")) {
			updateImages();
		} 	
		//forward stopped status to listeners (windows)
		else if (evt.getPropertyName().equals("Stopped")){
			if((boolean) evt.getNewValue()){
				btnInspectSolution.setEnabled(true);
				btnStop.setText("Resume");
				btnStop.setEnabled(true);
				progressBar.setEnabled(false);
			}else{
				btnInspectSolution.setEnabled(false);
				btnStop.setText("Pause");
				btnStop.setEnabled(true);
				progressBar.setEnabled(true);
			}

		}else {
			logger.debug("property: " + evt.getPropertyName());
		}

	}

	public void setConstraintsLogger(Logger2JFreeChartImage constraintsLogger) {
		this.constraintsLogger = constraintsLogger;
	}

	public void setCostLogger(Logger2JFreeChartImage costLogger) {
		this.costLogger = costLogger;
	}

	public void setMax(int max) {
		progressBar.setMaximum(max);
		frmOptimizationProgress.setVisible(true);
	}

	public void setVMLogger(Logger2JFreeChartImage vmLogger) {
		this.vmLogger = vmLogger;

	}

	private void updateImages() {
		if (costLogger != null) {
			ImageIcon icon;
			try {
				icon = new ImageIcon(costLogger.save2buffer(costPanel.getSize()));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
			}
			costLabel.setIcon(icon);
			costLabel.setVisible(true);
			//costPanel.setPreferredSize(costLabel.getPreferredSize());
		}

		if (vmLogger != null) {
			ImageIcon icon;
			try {
				icon = new ImageIcon(vmLogger.save2buffer(vmPanel.getSize()));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
			}
			vmLabel.setIcon(icon);
			vmLabel.setVisible(true);
			//vmPanel.setPreferredSize(vmLabel.getPreferredSize());
		}

		if (constraintsLogger != null) {
			ImageIcon icon;
			try {
				icon = new ImageIcon(constraintsLogger.save2buffer(constraintPanel.getSize()));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
			}
			constraintLabel.setIcon(icon);
			constraintLabel.setVisible(true);
			//constraintPanel.setPreferredSize(constraintLabel.getPreferredSize());
		}
	}

	private void updateProgressBar(int progress) {
		progressBar.setValue(progress);
	}

	public void signalCompletion() {
		JOptionPane.showMessageDialog(frmOptimizationProgress, "Optimization process compleated");	
		btnStop.setText("Finished");
		btnStop.setEnabled(false);
		btnInspectSolution.setEnabled(false);
	}

	public void signalError(String message) {
		JOptionPane.showMessageDialog(frmOptimizationProgress, message, "Error",  JOptionPane.ERROR_MESSAGE);		
	}

	public void addPropertyChangeListener(PropertyChangeListener listener){
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void windowClosing(WindowEvent e) {		
		super.windowClosing(e);
		frmOptimizationProgress.dispose();
		pcs.firePropertyChange("WindowClosed", false, true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(btnStop))
			if(Configuration.isPaused()){
				Configuration.resume();
				btnStop.setText("Resuming..");
				btnStop.setEnabled(false);;				
			}
			else{
				Configuration.pause();
				btnStop.setText("Pausing..");
				btnStop.setEnabled(false);
			}
		else if(e.getSource().equals(btnInspectSolution)){
			pcs.firePropertyChange("InspectSolution",false,true);
		}		
	}

}
