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
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Component;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Functionality;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class AssessmentWindow extends WindowAdapter implements PropertyChangeListener {

	private JFrame frame;
	
	private HashMap<String, Logger2JFreeChartImage> vmLoggers = new HashMap<String, Logger2JFreeChartImage>();
	private HashMap<String, JPanel> vmPanels = new HashMap<String, JPanel>();
	private HashMap<String, JLabel> vmImgLabels = new HashMap<String, JLabel>();
	
	private HashMap<String, Logger2JFreeChartImage> rtLoggers = new HashMap<String, Logger2JFreeChartImage>();
	private HashMap<String, JPanel> rtPanels = new HashMap<String, JPanel>();
	private HashMap<String, JLabel> rtImgLabels = new HashMap<String, JLabel>();
	
	private HashMap<String, Logger2JFreeChartImage> utilLoggers = new HashMap<String, Logger2JFreeChartImage>();
	private HashMap<String, JPanel> utilPanels = new HashMap<String, JPanel>();
	private HashMap<String, JLabel> utilImgLabels = new HashMap<String, JLabel>();
	
	private JTabbedPane tab;
	
	public final static String FRAME_NAME = "Assessment Results Window";
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/**
	 * Create the application.
	 */
	public AssessmentWindow() {
		initialize();
	}
	
	public void considerSolution(SolutionMulti solution) throws NumberFormatException, IOException {
		frame.setVisible(false);
		
		vmLoggers.clear();
		vmPanels.clear();
		vmImgLabels.clear();
		rtLoggers.clear();
		rtPanels.clear();
		rtImgLabels.clear();
		utilLoggers.clear();
		utilPanels.clear();
		utilImgLabels.clear();
		
		tab.removeAll();
		
		for (Solution providedSolution : solution.getAll()) {
			String provider = providedSolution.getProvider();
			
			JPanel imageContainerPanel = new JPanel();
			imageContainerPanel.setLayout(new GridLayout(3, 1, 0, 0));
			
			tab.addTab(provider, imageContainerPanel);
			
			JPanel vmPanel = new JPanel();
//			vmPanel.setBorder(new TitledBorder(null, "Number of VMs",
//					TitledBorder.LEADING, TitledBorder.TOP, null, null));
			imageContainerPanel.add(vmPanel);
//			vmPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); //, 5, 5));

			JLabel vmImgLabel = new JLabel();
			vmPanel.add(vmImgLabel);
			
			vmPanels.put(provider, vmPanel);
			vmImgLabels.put(provider, vmImgLabel);

			JPanel utilPanel = new JPanel();
//			utilPanel.setBorder(new TitledBorder(null, "Utilization",
//					TitledBorder.LEADING, TitledBorder.TOP, null, null));
			imageContainerPanel.add(utilPanel);
//			utilPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); //, 5, 5));

			JLabel utilImgLabel = new JLabel();
			utilPanel.add(utilImgLabel);
			
			utilPanels.put(provider, utilPanel);
			utilImgLabels.put(provider, utilImgLabel);

			JPanel rtPanel = new JPanel();
//			rtPanel.setBorder(new TitledBorder(null, "Response Times",
//					TitledBorder.LEADING, TitledBorder.TOP, null, null));
			imageContainerPanel.add(rtPanel);
//			rtPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); //, 5, 5));

			JLabel rtImgLabel = new JLabel();
			rtPanel.add(rtImgLabel);
			
			rtPanels.put(provider, rtPanel);
			rtImgLabels.put(provider, rtImgLabel);
			
			// plotting the number of VMs
			Logger2JFreeChartImage vmLogger = new Logger2JFreeChartImage(
					"vmCount.properties");
			Map<String, SeriesHandle> vmSeriesHandlers = new HashMap<>();
			for (Tier t : providedSolution.getApplication(0).getTiers()) {
				vmSeriesHandlers.put(t.getId(), vmLogger.newSeries(t.getName()));
			}
			for (int i = 0; i < 24; i++) {
				for (Tier t : providedSolution.getApplication(i).getTiers()) {
					vmLogger.addPoint2Series(vmSeriesHandlers.get(t.getId()), i,
							((IaaS) t.getCloudService()).getReplicas());
				}
			}
			vmLoggers.put(provider, vmLogger);
	
			// plotting the response Times
			Logger2JFreeChartImage rtLogger = new Logger2JFreeChartImage(
					"responseTime.properties");
			Map<String, SeriesHandle> rtSeriesHandlers = new HashMap<>();
			for (Tier t : providedSolution.getApplication(0).getTiers())
				for (Component c : t.getComponents())
					for (Functionality f : c.getFunctionalities())
						rtSeriesHandlers.put(f.getName(),
								rtLogger.newSeries(f.getName()));
	
			for (int i = 0; i < 24; i++)
				for (Tier t : providedSolution.getApplication(i).getTiers())
					for (Component c : t.getComponents())
						for (Functionality f : c.getFunctionalities()){
							if(f.isEvaluated())
								rtLogger.addPoint2Series(
										rtSeriesHandlers.get(f.getName()), i,
										f.getResponseTime());
						}
			rtLoggers.put(provider, rtLogger);
	
			// plotting the utilization
			Logger2JFreeChartImage utilLogger = new Logger2JFreeChartImage(
					"utilization.properties");
			Map<String, SeriesHandle> utilSeriesHandlers = new HashMap<>();
			for (Tier t : providedSolution.getApplication(0).getTiers())
				utilSeriesHandlers.put(t.getId(), utilLogger.newSeries(t.getName()));
	
			for (int i = 0; i < 24; i++)
				for (Tier t : providedSolution.getApplication(i).getTiers())
					utilLogger.addPoint2Series(utilSeriesHandlers.get(t.getId()),
							i, ((Compute) t.getCloudService()).getUtilization());
			utilLoggers.put(provider, utilLogger);
		}
		
		frame.setVisible(true);
		frame.validate();
		
		updateImages();
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

	public void updateImages() {
		if (alreadyUpdating)
			return;
		
		alreadyUpdating = true;
		
		for (String provider : rtLoggers.keySet()) {
			
			Logger2JFreeChartImage rtLogger = rtLoggers.get(provider);
			JPanel rtPanel = rtPanels.get(provider);
			JLabel rtImgLabel = rtImgLabels.get(provider);
			
			if (rtLogger != null) {
				rtImgLabel.setIcon(new ImageIcon(rtLogger.save2buffer(rtPanel
						.getSize())));
				rtImgLabel.setVisible(true);
				rtPanel.setPreferredSize(rtImgLabel.getPreferredSize());
			}
			
			Logger2JFreeChartImage vmLogger = vmLoggers.get(provider);
			JPanel vmPanel = vmPanels.get(provider);
			JLabel vmImgLabel = vmImgLabels.get(provider);
			
			if (vmLogger != null) {
				vmImgLabel.setIcon(new ImageIcon(vmLogger.save2buffer(vmPanel
						.getSize())));
				vmImgLabel.setVisible(true);
				vmPanel.setPreferredSize(vmImgLabel.getPreferredSize());
			}
			
			Logger2JFreeChartImage utilLogger = utilLoggers.get(provider);
			JPanel utilPanel = utilPanels.get(provider);
			JLabel utilImgLabel = utilImgLabels.get(provider);
			
			if (utilLogger != null) {
				utilImgLabel.setIcon(new ImageIcon(utilLogger.save2buffer(utilPanel
						.getSize())));
				utilImgLabel.setVisible(true);
				utilPanel.setPreferredSize(utilImgLabel.getPreferredSize());
			}
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

}
