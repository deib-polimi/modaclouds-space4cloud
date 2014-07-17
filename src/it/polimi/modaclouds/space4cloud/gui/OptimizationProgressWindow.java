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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptimizationProgressWindow extends WindowAdapter implements PropertyChangeListener {

	private JFrame frmOptimizationProgress;

	JProgressBar progressBar;
	private JPanel upperPanel;
	private JPanel lowerPanel;

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
		frmOptimizationProgress.setBounds(100, 100, 450, 300);
		frmOptimizationProgress
				.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frmOptimizationProgress.getContentPane().setLayout(
				new BorderLayout(0, 0));

		frmOptimizationProgress.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frmOptimizationProgress.addWindowListener(this);
		// Upper panel (for progress bar)
		upperPanel = new JPanel();
		frmOptimizationProgress.getContentPane().add(upperPanel,
				BorderLayout.NORTH);
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));

		// progress bar
		progressBar = new JProgressBar(0, 100);
		upperPanel.add(progressBar);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		// Lower panel (for images)
		lowerPanel = new JPanel();
		frmOptimizationProgress.getContentPane().add(lowerPanel,
				BorderLayout.CENTER);
		lowerPanel.setLayout(new GridLayout(0, 3, 0, 0));

		// vmPanel for VM image
		vmPanel = new JPanel();
		vmPanel.setBorder(new TitledBorder(null, "Total Number of VMs",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		lowerPanel.add(vmPanel);

		// vmLabel for VM image
		vmLabel = new JLabel();
		vmLabel.setIcon(null);
		vmPanel.add(vmLabel);

		// Cost panel for cost image
		costPanel = new JPanel();
		costPanel.setBorder(new TitledBorder(null, "Solution Cost",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		lowerPanel.add(costPanel);

		// Cost label for cost image
		costLabel = new JLabel();
		costLabel.setIcon(null);
		costPanel.add(costLabel);

		// Constraint panel for constraint image
		constraintPanel = new JPanel();
		constraintPanel.setBorder(new TitledBorder(null,
				"Violated Constraints", TitledBorder.LEADING, TitledBorder.TOP,
				null, null));
		lowerPanel.add(constraintPanel);

		// Constraint label for constraint image
		constraintLabel = new JLabel();
		constraintLabel.setIcon(null);
		constraintPanel.add(constraintLabel);

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
		} else
		// TODO: add evaluation to the list of bound properties and add this
		// class as listener to the evaluation server
		if (evt.getPropertyName().equals("totalNumberOfEvaluations")) {
			updateImages();
		} else {
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
				icon = new ImageIcon(
						costLogger.save2buffer(costPanel.getSize()));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
			}
			costLabel.setIcon(icon);
			costLabel.setVisible(true);
			costPanel.setPreferredSize(costLabel.getPreferredSize());
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
			vmPanel.setPreferredSize(vmLabel.getPreferredSize());
		}

		if (constraintsLogger != null) {
			ImageIcon icon;
			try {
				icon = new ImageIcon(
						constraintsLogger.save2buffer(constraintPanel.getSize()));
			} catch (NullPointerException e) {
				icon = new ImageIcon();
			}
			constraintLabel.setIcon(icon);
			constraintLabel.setVisible(true);
			constraintPanel
					.setPreferredSize(constraintLabel.getPreferredSize());
		}
	}

	private void updateProgressBar(int progress) {
		progressBar.setValue(progress);
	}

	public void signalCompletion() {
		JOptionPane.showMessageDialog(frmOptimizationProgress, "Optimization process compleated");		
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
}
