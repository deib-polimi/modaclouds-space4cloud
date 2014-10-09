package it.polimi.modaclouds.space4cloud.gui;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BestSolutionExplorer implements ActionListener {

	private JFrame frmBestSolutionsExplorer;
	private List<SolutionMulti> solutions;
	private SolutionWindowPanel solutionWindowTab;	
	private int current;
	private JButton btnPrevious;
	private JButton btnNext;
	private GridBagConstraints gbc_solutionPanel;



	/**
	 * Create the application.
	 */
	private BestSolutionExplorer(List<SolutionMulti> solutions) {
		this.solutions = new ArrayList<SolutionMulti>(solutions.size());
		this.solutions.addAll(solutions);
		initialize();
		frmBestSolutionsExplorer.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmBestSolutionsExplorer = new JFrame();
		frmBestSolutionsExplorer.setTitle("Best Solutions Explorer");
		frmBestSolutionsExplorer.setBounds(100, 100, 450, 300);
		frmBestSolutionsExplorer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 30, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		frmBestSolutionsExplorer.getContentPane().setLayout(gridBagLayout);

		current = solutions.size()-1;
		solutionWindowTab = new SolutionWindowPanel(solutions.get(current));
		gbc_solutionPanel = new GridBagConstraints();
		gbc_solutionPanel.insets = new Insets(0, 0, 5, 0);
		gbc_solutionPanel.fill = GridBagConstraints.BOTH;
		gbc_solutionPanel.gridx = 0;
		gbc_solutionPanel.gridy = 0;
		frmBestSolutionsExplorer.getContentPane().add(solutionWindowTab, gbc_solutionPanel);


		JPanel commandPanel = new JPanel();
		GridBagConstraints gbc_commandPanel = new GridBagConstraints();
		gbc_commandPanel.fill = GridBagConstraints.BOTH;
		gbc_commandPanel.gridx = 0;
		gbc_commandPanel.gridy = 1;
		frmBestSolutionsExplorer.getContentPane().add(commandPanel, gbc_commandPanel);

		btnPrevious = new JButton("Previous");
		btnPrevious.addActionListener(this);
		if(current == 0)
			btnPrevious.setEnabled(false);
		commandPanel.add(btnPrevious);

		btnNext = new JButton("Next");
		btnNext.addActionListener(this);
		btnNext.setEnabled(false);
		commandPanel.add(btnNext);
	}

	

	/**
	 * Shows the window with the set of solutions specified. If the window has already shown it updates  the window according to the specified solutions
	 * @param solutions
	 */
	public static void show(List<SolutionMulti> solutions){
		new BestSolutionExplorer(solutions);		
	}



	private void next(){
		if(current < solutions.size()-1){
			current++;			
			frmBestSolutionsExplorer.getContentPane().remove(solutionWindowTab);			
			solutionWindowTab = new SolutionWindowPanel(solutions.get(current));
			frmBestSolutionsExplorer.getContentPane().add(solutionWindowTab, gbc_solutionPanel);
			frmBestSolutionsExplorer.getContentPane().validate();
			if(current==solutions.size()-1)
				btnNext.setEnabled(false);
			if(current!=0)
				btnPrevious.setEnabled(true);
		}
	}

	private void previous(){
		if(current > 0){
			current--;
			frmBestSolutionsExplorer.getContentPane().remove(solutionWindowTab);			
			solutionWindowTab = new SolutionWindowPanel(solutions.get(current));
			frmBestSolutionsExplorer.getContentPane().add(solutionWindowTab, gbc_solutionPanel);
			frmBestSolutionsExplorer.getContentPane().validate();
			if(current<solutions.size()-1)
				btnNext.setEnabled(true);
			if(current==0)
				btnPrevious.setEnabled(false);
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(btnNext)){
			next();
		}else if(e.getSource().equals(btnPrevious)){
			previous();
		}
		
	}

}
