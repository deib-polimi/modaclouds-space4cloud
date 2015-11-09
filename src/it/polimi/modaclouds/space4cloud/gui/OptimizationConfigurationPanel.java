package it.polimi.modaclouds.space4cloud.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Benchmark;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Policy;

public class OptimizationConfigurationPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5356951769849277734L;
	private static final String PANEL_NAME = "Optimization"; // Configuration";
	private JLabel scaleInFactorLabel;
	private JTextField tabuMemoryText;
	private JTextField scrumbleText;
	private JTextField feasibilityText;
	private JLabel selectionPolicyLabel;
	private JComboBox<Policy> selectionPolicyBox;
	private JTextField scaleInFactorText;
	private JLabel scaleInIterLabel;
	private JTextField scaleInIterText;
	private JLabel scaleInConvLabel;
	private JTextField scaleInConfText;
	private JCheckBox initialSolutionBox;
	private JLabel emptyLabel;
	private JLabel emptyLabel2;
	private JCheckBox redistributeWorkloadBox;
	private JComboBox<Benchmark> selectionBenchmarkBox;
	/**
	 * Create the panel.
	 */
	public OptimizationConfigurationPanel() {
		setName(PANEL_NAME);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{201, 201, 0};
		gridBagLayout.rowHeights = new int[]{35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		setLayout(gridBagLayout);

		JLabel tabuMemoryLabel = new JLabel("Tabu Memory Size");
		GridBagConstraints gbc_tabuMemoryLabel = new GridBagConstraints();
		gbc_tabuMemoryLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_tabuMemoryLabel.insets = new Insets(0, 0, 5, 5);
		gbc_tabuMemoryLabel.gridx = 0;
		gbc_tabuMemoryLabel.gridy = 0;
		add(tabuMemoryLabel, gbc_tabuMemoryLabel);
		
		tabuMemoryText = new JTextField();
		GridBagConstraints gbc_tabuMemoryText = new GridBagConstraints();
		gbc_tabuMemoryText.insets = new Insets(0, 0, 5, 0);
		gbc_tabuMemoryText.fill = GridBagConstraints.HORIZONTAL;
		gbc_tabuMemoryText.gridx = 1;
		gbc_tabuMemoryText.gridy = 0;
		add(tabuMemoryText, gbc_tabuMemoryText);
		tabuMemoryText.setColumns(10);


		JLabel scrumbleLabel = new JLabel("Scrumble Iterations");
		GridBagConstraints gbc_scrumbleLabel = new GridBagConstraints();
		gbc_scrumbleLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_scrumbleLabel.insets = new Insets(0, 0, 5, 5);
		gbc_scrumbleLabel.gridx = 0;
		gbc_scrumbleLabel.gridy = 1;
		add(scrumbleLabel, gbc_scrumbleLabel);
		
		scrumbleText = new JTextField();
		GridBagConstraints gbc_scrumbleText = new GridBagConstraints();
		gbc_scrumbleText.insets = new Insets(0, 0, 5, 0);
		gbc_scrumbleText.fill = GridBagConstraints.HORIZONTAL;
		gbc_scrumbleText.gridx = 1;
		gbc_scrumbleText.gridy = 1;
		add(scrumbleText, gbc_scrumbleText);
		scrumbleText.setColumns(10);

		JLabel feasibilityLabel = new JLabel("Feasibility Iterations");
		GridBagConstraints gbc_feasibilityLabel = new GridBagConstraints();
		gbc_feasibilityLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_feasibilityLabel.insets = new Insets(0, 0, 5, 5);
		gbc_feasibilityLabel.gridx = 0;
		gbc_feasibilityLabel.gridy = 2;
		add(feasibilityLabel, gbc_feasibilityLabel);
		
		feasibilityText = new JTextField();
		GridBagConstraints gbc_feasibilityText = new GridBagConstraints();
		gbc_feasibilityText.insets = new Insets(0, 0, 5, 0);
		gbc_feasibilityText.fill = GridBagConstraints.HORIZONTAL;
		gbc_feasibilityText.gridx = 1;
		gbc_feasibilityText.gridy = 2;
		add(feasibilityText, gbc_feasibilityText);
		feasibilityText.setColumns(10);
		
		selectionPolicyLabel = new JLabel("Selection Policy");
		GridBagConstraints gbc_selectionPolicyLabel = new GridBagConstraints();
		gbc_selectionPolicyLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectionPolicyLabel.insets = new Insets(0, 0, 5, 5);
		gbc_selectionPolicyLabel.gridx = 0;
		gbc_selectionPolicyLabel.gridy = 3;
		add(selectionPolicyLabel, gbc_selectionPolicyLabel);

		scaleInFactorLabel = new JLabel("Scale In Factor");
		GridBagConstraints gbc_scaleInFactorLabel = new GridBagConstraints();
		gbc_scaleInFactorLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_scaleInFactorLabel.insets = new Insets(0, 0, 5, 5);
		gbc_scaleInFactorLabel.gridx = 0;
		gbc_scaleInFactorLabel.gridy = 4;
		
		selectionPolicyBox = new JComboBox<Policy>();
		selectionPolicyBox.setModel(new DefaultComboBoxModel<Policy>(Policy.values()));
		selectionPolicyBox.addActionListener(this);
		selectionPolicyBox.setSelectedItem(Policy.Utilization);
		GridBagConstraints gbc_selectionPolicyBox = new GridBagConstraints();
		gbc_selectionPolicyBox.insets = new Insets(0, 0, 5, 0);
		gbc_selectionPolicyBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectionPolicyBox.gridx = 1;
		gbc_selectionPolicyBox.gridy = 3;
		add(selectionPolicyBox, gbc_selectionPolicyBox);
		add(scaleInFactorLabel, gbc_scaleInFactorLabel);
		
		scaleInFactorText = new JTextField();
		GridBagConstraints gbc_scaleInFactorText = new GridBagConstraints();
		gbc_scaleInFactorText.insets = new Insets(0, 0, 5, 0);
		gbc_scaleInFactorText.fill = GridBagConstraints.HORIZONTAL;
		gbc_scaleInFactorText.gridx = 1;
		gbc_scaleInFactorText.gridy = 4;
		add(scaleInFactorText, gbc_scaleInFactorText);
		scaleInFactorText.setColumns(10);
		
		scaleInIterLabel = new JLabel("Scale In Iterations");
		GridBagConstraints gbc_scaleInIterLabel = new GridBagConstraints();
		gbc_scaleInIterLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_scaleInIterLabel.insets = new Insets(0, 0, 5, 5);
		gbc_scaleInIterLabel.gridx = 0;
		gbc_scaleInIterLabel.gridy = 5;
		add(scaleInIterLabel, gbc_scaleInIterLabel);
		
		scaleInIterText = new JTextField();
		GridBagConstraints gbc_scaleInIterText = new GridBagConstraints();
		gbc_scaleInIterText.insets = new Insets(0, 0, 5, 0);
		gbc_scaleInIterText.fill = GridBagConstraints.HORIZONTAL;
		gbc_scaleInIterText.gridx = 1;
		gbc_scaleInIterText.gridy = 5;
		add(scaleInIterText, gbc_scaleInIterText);
		scaleInIterText.setColumns(10);
		
		scaleInConvLabel = new JLabel("Scale In Convergence Iterations");
		GridBagConstraints gbc_scaleInConvLabel = new GridBagConstraints();
		gbc_scaleInConvLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_scaleInConvLabel.insets = new Insets(0, 0, 5, 5);
		gbc_scaleInConvLabel.gridx = 0;
		gbc_scaleInConvLabel.gridy = 6;
		add(scaleInConvLabel, gbc_scaleInConvLabel);
		
		scaleInConfText = new JTextField();
		GridBagConstraints gbc_scaleInConfText = new GridBagConstraints();
		gbc_scaleInConfText.insets = new Insets(0, 0, 5, 0);
		gbc_scaleInConfText.fill = GridBagConstraints.HORIZONTAL;
		gbc_scaleInConfText.gridx = 1;
		gbc_scaleInConfText.gridy = 6;
		add(scaleInConfText, gbc_scaleInConfText);
		scaleInConfText.setColumns(10);
		
		initialSolutionBox = new JCheckBox("Generate relaxed initial solution");
		initialSolutionBox.addActionListener(this);
		GridBagConstraints gbc_initialSolutionBox = new GridBagConstraints();
		gbc_initialSolutionBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_initialSolutionBox.insets = new Insets(0, 0, 5, 5);
		gbc_initialSolutionBox.gridx = 0;
		gbc_initialSolutionBox.gridy = 7;
		add(initialSolutionBox, gbc_initialSolutionBox);
		
		emptyLabel = new JLabel("");
		GridBagConstraints gbc_emptyLabel = new GridBagConstraints();
		gbc_emptyLabel.insets = new Insets(0, 0, 5, 0);
		gbc_emptyLabel.gridx = 1;
		gbc_emptyLabel.gridy = 7;
		add(emptyLabel, gbc_emptyLabel);
		
		redistributeWorkloadBox = new JCheckBox("Optimized workload distribution");
		redistributeWorkloadBox.addActionListener(this);
		GridBagConstraints gbc_redistributeWorkloadBox = new GridBagConstraints();
		gbc_redistributeWorkloadBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_redistributeWorkloadBox.insets = new Insets(0, 0, 5, 5);
		gbc_redistributeWorkloadBox.gridx = 0;
		gbc_redistributeWorkloadBox.gridy = 8;
		add(redistributeWorkloadBox, gbc_redistributeWorkloadBox);
		
		emptyLabel2 = new JLabel("");
		GridBagConstraints gbc_emptyLabel2 = new GridBagConstraints();
		gbc_emptyLabel2.insets = new Insets(0, 0, 5, 0);
		gbc_emptyLabel2.gridx = 1;
		gbc_emptyLabel2.gridy = 8;
		add(emptyLabel2, gbc_emptyLabel2);
		
		GridBagConstraints gbc_selectionBenchmarkLabel = new GridBagConstraints();
		gbc_selectionBenchmarkLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectionBenchmarkLabel.insets = new Insets(0, 0, 5, 5);
		gbc_selectionBenchmarkLabel.gridx = 0;
		gbc_selectionBenchmarkLabel.gridy = 9;
		add(new JLabel("Selection Benchmark"), gbc_selectionBenchmarkLabel);
		
		selectionBenchmarkBox = new JComboBox<Benchmark>();
		selectionBenchmarkBox.setModel(new DefaultComboBoxModel<Benchmark>(Benchmark.values()));
		selectionBenchmarkBox.addActionListener(this);
		selectionBenchmarkBox.setSelectedItem(Benchmark.None);
		GridBagConstraints gbc_selectionBenchmarkBox = new GridBagConstraints();
		gbc_selectionBenchmarkBox.insets = new Insets(0, 0, 5, 0);
		gbc_selectionBenchmarkBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectionBenchmarkBox.gridx = 1;
		gbc_selectionBenchmarkBox.gridy = 9;
		add(selectionBenchmarkBox, gbc_selectionBenchmarkBox);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(selectionPolicyBox)){
			Configuration.SELECTION_POLICY= (Policy) selectionPolicyBox.getSelectedItem();
		}else if(e.getSource().equals(initialSolutionBox) || e.getSource().equals(redistributeWorkloadBox)){
			updateSSHVisibility();
		}
	}
	
	public static final String updateSSH = "UpdateSSH";
	
	/**
	 * Updates the visibility of the ssh connection parameters according to the initial solution generation box selection
	 */
	public void updateSSHVisibility(){
		boolean shown = sshNeeded();
		
		firePropertyChange(updateSSH, !shown, shown);
	}
	
	public boolean sshNeeded() {
		return initialSolutionBox.isSelected() || redistributeWorkloadBox.isSelected() || Configuration.USE_PRIVATE_CLOUD || Configuration.CONTRACTOR_TEST;
	}

	/**
	 * Updates the values shown to the user according to those stored in the Configuration class
	 */
	public void loadConfiguration() {
		//TODO: validate with number format exceptions
		selectionPolicyBox.setSelectedItem(Configuration.SELECTION_POLICY);		
		tabuMemoryText.setText(Integer.toString(Configuration.TABU_MEMORY_SIZE));
		scrumbleText.setText(Integer.toString(Configuration.SCRUMBLE_ITERS));
		feasibilityText.setText(Integer.toString(Configuration.FEASIBILITY_ITERS));
		scaleInFactorText.setText(Double.toString(Configuration.SCALE_IN_FACTOR));
		scaleInIterText.setText(Integer.toString(Configuration.SCALE_IN_ITERS));
		scaleInConfText.setText(Integer.toString(Configuration.SCALE_IN_CONV_ITERS));
		initialSolutionBox.setSelected(Configuration.RELAXED_INITIAL_SOLUTION);
		redistributeWorkloadBox.setSelected(Configuration.REDISTRIBUTE_WORKLOAD);
		
		selectionBenchmarkBox.setSelectedItem(Configuration.BENCHMARK);
		
		updateSSHVisibility();
		
	}
	
	/**
	 * Updates values in the Configuration class according to those selected in the panel
	 */
	public void updateConfiguration(){
		//TODO: validate with number format exceptions
		Configuration.SELECTION_POLICY = (Policy) selectionPolicyBox.getSelectedItem();
		try{
		Configuration.TABU_MEMORY_SIZE = Integer.parseInt(tabuMemoryText.getText());
		}catch(NumberFormatException e){
			Configuration.TABU_MEMORY_SIZE = -1;
		}
		try{
		Configuration.SCRUMBLE_ITERS = Integer.parseInt(scrumbleText.getText());
		}catch(NumberFormatException e){
			Configuration.SCRUMBLE_ITERS = -1;
		}
		try{
		Configuration.FEASIBILITY_ITERS = Integer.parseInt(feasibilityText.getText());
		}catch(NumberFormatException e){
			Configuration.FEASIBILITY_ITERS = -1;
		}
		try{
		Configuration.SCALE_IN_FACTOR = Double.parseDouble(scaleInFactorText.getText());
		}catch(NumberFormatException e){
			Configuration.SCALE_IN_FACTOR = -1;
		}
		try{
		Configuration.SCALE_IN_ITERS = Integer.parseInt(scaleInIterText.getText());
		}catch(NumberFormatException e){
			Configuration.SCALE_IN_ITERS = -1;
		}
		try{
		Configuration.SCALE_IN_CONV_ITERS = Integer.parseInt(scaleInConfText.getText());
		}catch(NumberFormatException e){
			Configuration.SCALE_IN_CONV_ITERS = -1;
		}
		Configuration.RELAXED_INITIAL_SOLUTION = initialSolutionBox.isSelected();
		
		Configuration.REDISTRIBUTE_WORKLOAD = redistributeWorkloadBox.isSelected();
		
		Configuration.BENCHMARK = (Benchmark) selectionBenchmarkBox.getSelectedItem();
	}

	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		//change also the state of all internal components
		for(Component comp:getComponents()){
			comp.setEnabled(enabled);
		}
	}
	
}
