package it.polimi.modaclouds.space4cloud.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.polimi.modaclouds.space4cloud.utils.Configuration;

public class Design2RuntimePanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6515518344732137935L;
	
	private static final String PANEL_NAME = "Design2Runtime";
	private JTextField functionality2TierText;
	private JButton functionality2TierButton;
	private JTextField optimizationWindowLengthText;
	private JTextField timestepDurationText;

	/**
	 * Create the panel.
	 */
	public Design2RuntimePanel() {
		setName(PANEL_NAME);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {201, 201, 0};
		gridBagLayout.rowHeights = new int[] {35, 35, 35, 35, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
		setLayout(gridBagLayout);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(new JLabel("Functionality2Tier File"), gbc);

		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx++;
		add(new JLabel(""), gbc);

		functionality2TierText = new JTextField();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy++;
		add(functionality2TierText, gbc);
		functionality2TierText.setColumns(10);

		functionality2TierButton = new JButton("Browse");
		functionality2TierButton.addActionListener(this);
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx++;
		add(functionality2TierButton, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy++;
		add(new JLabel("Optimization Window Length"), gbc);
		
		optimizationWindowLengthText = new JTextField();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx++;
		add(optimizationWindowLengthText, gbc);
		optimizationWindowLengthText.setColumns(10);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy++;
		add(new JLabel("Timestep Duration"), gbc);
		
		timestepDurationText = new JTextField();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx++;
		add(timestepDurationText, gbc);
		timestepDurationText.setColumns(10);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(functionality2TierButton)) {
			File functionality2TierFile = FileLoader.loadFile("Functionality2Tier File", "xml");
			if (functionality2TierFile != null) {
				Configuration.FUNCTIONALITY_TO_TIER_FILE = functionality2TierFile.getAbsolutePath();
				functionality2TierText.setText(functionality2TierFile.getAbsolutePath());
			}
		}
	}

	/**
	 * Updates the values shown to the user according to those stored in the Configuration class
	 */
	public void loadConfiguration() {
		functionality2TierText.setText(Configuration.FUNCTIONALITY_TO_TIER_FILE);
		optimizationWindowLengthText.setText(Integer.toString(Configuration.OPTIMIZATION_WINDOW_LENGTH));
		timestepDurationText.setText(Integer.toString(Configuration.TIMESTEP_DURATION));
	}

	/**
	 * Updates values in the Configuration class according to those selected in the panel
	 */
	public void updateConfiguration(){
		Configuration.FUNCTIONALITY_TO_TIER_FILE = functionality2TierText.getText();
		Configuration.OPTIMIZATION_WINDOW_LENGTH = Integer.parseInt(optimizationWindowLengthText.getText());
		Configuration.TIMESTEP_DURATION = Integer.parseInt(timestepDurationText.getText());
	}

}