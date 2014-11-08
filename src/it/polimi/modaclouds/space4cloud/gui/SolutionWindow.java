package it.polimi.modaclouds.space4cloud.gui;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;

public class SolutionWindow {
	
	private JFrame gui;
	private SolutionWindowPanel solutionWindowTab;
	
	
	private SolutionWindow(SolutionMulti solutionMulti) {
		gui = new JFrame();
		gui.setTitle("Best Solutions Explorer");
//		gui.setBounds(100, 100, 450, 300);
		gui.setMinimumSize(new Dimension(900, 600));
		gui.setLocationRelativeTo(null);
		gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 30, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gui.getContentPane().setLayout(gridBagLayout);


		solutionWindowTab = new SolutionWindowPanel(solutionMulti);
		GridBagConstraints gbc_solutionPanel = new GridBagConstraints();
		gbc_solutionPanel.insets = new Insets(0, 0, 5, 0);
		gbc_solutionPanel.fill = GridBagConstraints.BOTH;
		gbc_solutionPanel.gridx = 0;
		gbc_solutionPanel.gridy = 0;
		gui.getContentPane().add(solutionWindowTab, gbc_solutionPanel);
		
		gui.setVisible(true);
	}
	
	
	public static void show(SolutionMulti solution) {
		new SolutionWindow(solution);
				
	}
	
}