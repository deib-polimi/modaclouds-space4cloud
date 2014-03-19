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
import java.awt.EventQueue;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import java.awt.FlowLayout;
import java.awt.GridLayout;

public class AssesmentWindow {

	private JFrame frame;

	private Logger2JFreeChartImage rtLogger;

	private Logger2JFreeChartImage vmLogger;

	private Logger2JFreeChartImage utilLogger;

	JPanel vmPanel;

	JPanel rtPanel;

	JPanel utilPanel;

	JLabel vmImgLabel;

	JLabel rtImgLabel;

	JLabel utilImgLabel;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AssesmentWindow window = new AssesmentWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public AssesmentWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel imageContainerPanel = new JPanel();
		frame.getContentPane().add(imageContainerPanel, BorderLayout.CENTER);
		imageContainerPanel.setLayout(new GridLayout(0, 1, 0, 0));

		
		
		vmPanel = new JPanel();
		vmPanel.setBorder(new TitledBorder(null, "Number of VMs", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		imageContainerPanel.add(vmPanel);
		vmPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		vmImgLabel = new JLabel();
		vmPanel.add(vmImgLabel);
		
				utilPanel = new JPanel();
				utilPanel.setBorder(new TitledBorder(null, "Utilization", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				imageContainerPanel.add(utilPanel);
				utilPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
				
						utilImgLabel = new JLabel();
						utilPanel.add(utilImgLabel);

		rtPanel = new JPanel();
		rtPanel.setBorder(new TitledBorder(null, "Response Times", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		imageContainerPanel.add(rtPanel);
		rtPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		rtImgLabel = new JLabel();
		rtPanel.add(rtImgLabel);
		

		//listener to resize images
		frame.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void componentResized(ComponentEvent e) {
				updateImages();				
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub				
			}
		});
	}


	public void setResponseTimeLogger(Logger2JFreeChartImage costLogger) {
		this.rtLogger = costLogger;
	}

	public void setVMLogger(Logger2JFreeChartImage vmLogger) {
		this.vmLogger = vmLogger;

	}

	public void setUtilizationLogger(Logger2JFreeChartImage constraintsLogger) {
		this.utilLogger = constraintsLogger;
	}

	public void updateImages(){
		if(rtLogger != null ){
			rtImgLabel.setIcon(new ImageIcon(rtLogger.save2buffer(rtPanel.getSize())));            
			rtImgLabel.setVisible(true);
			rtPanel.setPreferredSize(rtImgLabel.getPreferredSize());
		}
		if(vmLogger != null ){
			vmImgLabel.setIcon(new ImageIcon(vmLogger.save2buffer(vmPanel.getSize())));
			vmImgLabel.setVisible(true);
			vmPanel.setPreferredSize(vmImgLabel.getPreferredSize());
		}
		if(utilLogger != null ){
			utilImgLabel.setIcon(new ImageIcon(utilLogger.save2buffer(utilPanel.getSize())));
			utilImgLabel.setVisible(true);
			utilPanel.setPreferredSize(utilImgLabel.getPreferredSize());
		}

	}

	public void show() {
		frame.setVisible(true);
	}

}
