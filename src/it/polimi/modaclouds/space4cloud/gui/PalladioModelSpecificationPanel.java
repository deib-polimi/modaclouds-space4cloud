package it.polimi.modaclouds.space4cloud.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.polimi.modaclouds.space4cloud.utils.Configuration;

public class PalladioModelSpecificationPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5018880486991183723L;
	private static final String PANEL_NAME = "Model"; // Specification";
	private JTextField reposirotyTextField;
	private JTextField systemTextField;
	private JTextField allocationTextField;
	private JTextField resourceTextField;
	private JTextField usageTextField;
	private JButton repositoryButton;
	private JButton systemButton;
	private JButton allocationButton;
	private JButton resourceButton;
	private JButton usageButton;
	/**
	 * Create the panel.
	 */
	public PalladioModelSpecificationPanel() {
		setName(PANEL_NAME);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{350, 50, 0};
		gridBagLayout.rowHeights = new int[]{30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JLabel repositoryLabel = new JLabel("Repository Model");
		GridBagConstraints gbc_repositoryLabel = new GridBagConstraints();
		gbc_repositoryLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_repositoryLabel.insets = new Insets(0, 0, 5, 5);
		gbc_repositoryLabel.gridx = 0;
		gbc_repositoryLabel.gridy = 0;
		add(repositoryLabel, gbc_repositoryLabel);
		JLabel emptyLabel = new JLabel("");
		GridBagConstraints gbc_emptyLabel = new GridBagConstraints();
		gbc_emptyLabel.fill = GridBagConstraints.BOTH;
		gbc_emptyLabel.insets = new Insets(0, 0, 5, 0);
		gbc_emptyLabel.gridx = 1;
		gbc_emptyLabel.gridy = 0;
		add(emptyLabel, gbc_emptyLabel);

		reposirotyTextField = new JTextField();
		GridBagConstraints gbc_reposirotyTextField = new GridBagConstraints();
		gbc_reposirotyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_reposirotyTextField.insets = new Insets(0, 0, 5, 5);
		gbc_reposirotyTextField.gridx = 0;
		gbc_reposirotyTextField.gridy = 1;
		add(reposirotyTextField, gbc_reposirotyTextField);
		reposirotyTextField.setColumns(10);

		repositoryButton = new JButton("Load Model");		
		repositoryButton.addActionListener(this);
		GridBagConstraints gbc_repositoryButton = new GridBagConstraints();
		gbc_repositoryButton.insets = new Insets(0, 0, 5, 0);
		gbc_repositoryButton.gridx = 1;
		gbc_repositoryButton.gridy = 1;
		add(repositoryButton, gbc_repositoryButton);		


		JLabel systemLabel = new JLabel("System Model");
		GridBagConstraints gbc_systemLabel = new GridBagConstraints();
		gbc_systemLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_systemLabel.insets = new Insets(0, 0, 5, 5);
		gbc_systemLabel.gridx = 0;
		gbc_systemLabel.gridy = 2;
		add(systemLabel, gbc_systemLabel);
		JLabel emptyLabel1 = new JLabel("");
		GridBagConstraints gbc_emptyLabel1 = new GridBagConstraints();
		gbc_emptyLabel1.fill = GridBagConstraints.BOTH;
		gbc_emptyLabel1.insets = new Insets(0, 0, 5, 0);
		gbc_emptyLabel1.gridx = 1;
		gbc_emptyLabel1.gridy = 2;
		add(emptyLabel1, gbc_emptyLabel1);

		systemTextField = new JTextField();
		GridBagConstraints gbc_systemTextField = new GridBagConstraints();
		gbc_systemTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_systemTextField.insets = new Insets(0, 0, 5, 5);
		gbc_systemTextField.gridx = 0;
		gbc_systemTextField.gridy = 3;
		add(systemTextField, gbc_systemTextField);
		systemTextField.setColumns(10);

		systemButton = new JButton("Load Model");
		systemButton.addActionListener(this);
		GridBagConstraints gbc_systemButton = new GridBagConstraints();
		gbc_systemButton.insets = new Insets(0, 0, 5, 0);
		gbc_systemButton.gridx = 1;
		gbc_systemButton.gridy = 3;
		add(systemButton, gbc_systemButton);

		JLabel allocationLabel = new JLabel("Allocation Model");
		GridBagConstraints gbc_allocationLabel = new GridBagConstraints();
		gbc_allocationLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_allocationLabel.insets = new Insets(0, 0, 5, 5);
		gbc_allocationLabel.gridx = 0;
		gbc_allocationLabel.gridy = 4;
		add(allocationLabel, gbc_allocationLabel);
		JLabel emptyLabel2 = new JLabel("");
		GridBagConstraints gbc_emptyLabel2 = new GridBagConstraints();
		gbc_emptyLabel2.fill = GridBagConstraints.BOTH;
		gbc_emptyLabel2.insets = new Insets(0, 0, 5, 0);
		gbc_emptyLabel2.gridx = 1;
		gbc_emptyLabel2.gridy = 4;
		add(emptyLabel2, gbc_emptyLabel2);


		allocationTextField = new JTextField();
		GridBagConstraints gbc_allocationTextField = new GridBagConstraints();
		gbc_allocationTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_allocationTextField.insets = new Insets(0, 0, 5, 5);
		gbc_allocationTextField.gridx = 0;
		gbc_allocationTextField.gridy = 5;
		add(allocationTextField, gbc_allocationTextField);
		allocationTextField.setColumns(10);

		allocationButton = new JButton("Load Model");
		allocationButton.addActionListener(this);
		GridBagConstraints gbc_allocationButton = new GridBagConstraints();
		gbc_allocationButton.insets = new Insets(0, 0, 5, 0);
		gbc_allocationButton.gridx = 1;
		gbc_allocationButton.gridy = 5;
		add(allocationButton, gbc_allocationButton);

		JLabel resourceModel = new JLabel("Resource Environment Model");
		GridBagConstraints gbc_resourceModel = new GridBagConstraints();
		gbc_resourceModel.fill = GridBagConstraints.HORIZONTAL;
		gbc_resourceModel.insets = new Insets(0, 0, 5, 5);
		gbc_resourceModel.gridx = 0;
		gbc_resourceModel.gridy = 6;
		add(resourceModel, gbc_resourceModel);
		JLabel emptyLabel3 = new JLabel("");
		GridBagConstraints gbc_emptyLabel3 = new GridBagConstraints();
		gbc_emptyLabel3.fill = GridBagConstraints.BOTH;
		gbc_emptyLabel3.insets = new Insets(0, 0, 5, 0);
		gbc_emptyLabel3.gridx = 1;
		gbc_emptyLabel3.gridy = 6;
		add(emptyLabel3, gbc_emptyLabel3);

		resourceTextField = new JTextField();
		GridBagConstraints gbc_resourceTextField = new GridBagConstraints();
		gbc_resourceTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_resourceTextField.insets = new Insets(0, 0, 5, 5);
		gbc_resourceTextField.gridx = 0;
		gbc_resourceTextField.gridy = 7;
		add(resourceTextField, gbc_resourceTextField);
		resourceTextField.setColumns(10);

		resourceButton = new JButton("Load Model");
		resourceButton.addActionListener(this);
		GridBagConstraints gbc_resourceButton = new GridBagConstraints();
		gbc_resourceButton.insets = new Insets(0, 0, 5, 0);
		gbc_resourceButton.gridx = 1;
		gbc_resourceButton.gridy = 7;
		add(resourceButton, gbc_resourceButton);

		JLabel usageLabel = new JLabel("Usage Model");
		GridBagConstraints gbc_usageLabel = new GridBagConstraints();
		gbc_usageLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_usageLabel.insets = new Insets(0, 0, 5, 5);
		gbc_usageLabel.gridx = 0;
		gbc_usageLabel.gridy = 8;
		add(usageLabel, gbc_usageLabel);
		JLabel emptyLabel4 = new JLabel("");
		GridBagConstraints gbc_emptyLabel4 = new GridBagConstraints();
		gbc_emptyLabel4.fill = GridBagConstraints.BOTH;
		gbc_emptyLabel4.insets = new Insets(0, 0, 5, 0);
		gbc_emptyLabel4.gridx = 1;
		gbc_emptyLabel4.gridy = 8;
		add(emptyLabel4, gbc_emptyLabel4);

		usageTextField = new JTextField();
		GridBagConstraints gbc_usageTextField = new GridBagConstraints();
		gbc_usageTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_usageTextField.insets = new Insets(0, 0, 5, 5);
		gbc_usageTextField.gridx = 0;
		gbc_usageTextField.gridy = 9;
		add(usageTextField, gbc_usageTextField);
		usageTextField.setColumns(10);

		usageButton = new JButton("Load Model");
		usageButton.addActionListener(this);
		GridBagConstraints gbc_usageButton = new GridBagConstraints();
		gbc_usageButton.insets = new Insets(0, 0, 5, 0);
		gbc_usageButton.gridx = 1;
		gbc_usageButton.gridy = 9;
		add(usageButton, gbc_usageButton);

	}


	@Override
	public void actionPerformed(ActionEvent e) {
		File loadedFile = null;
		if(e.getSource().equals(repositoryButton)){
			loadedFile = FileLoader.loadFile("Load Repository Model", "repository");
			if(loadedFile!=null){
				reposirotyTextField.setText(loadedFile.getAbsolutePath());
				Configuration.PALLADIO_REPOSITORY_MODEL=loadedFile.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER=loadedFile.getParent().toString();
			}
		}else if(e.getSource().equals(systemButton)){
			loadedFile = FileLoader.loadFile("Load System Model", "system");
			if(loadedFile!=null){
				systemTextField.setText(loadedFile.getAbsolutePath());
				Configuration.PALLADIO_SYSTEM_MODEL=loadedFile.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER=loadedFile.getParent().toString();
			}
		}else if(e.getSource().equals(allocationButton)){
			loadedFile = FileLoader.loadFile("Load Allocation Model", "allocation");
			if(loadedFile!=null){
				allocationTextField.setText(loadedFile.getAbsolutePath());
				Configuration.PALLADIO_ALLOCATION_MODEL=loadedFile.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER=loadedFile.getParent().toString();
			}
		}else if(e.getSource().equals(resourceButton)){
			loadedFile = FileLoader.loadFile("Load Resource Model", "resourceenvironment");
			if(loadedFile!=null){
				resourceTextField.setText(loadedFile.getAbsolutePath());
				Configuration.PALLADIO_RESOURCE_MODEL=loadedFile.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER=loadedFile.getParent().toString();
			}
		}else if(e.getSource().equals(usageButton)){
			loadedFile = FileLoader.loadFile("Load Usage Model", "usagemodel");
			if(loadedFile!=null){
				usageTextField.setText(loadedFile.getAbsolutePath());
				Configuration.PALLADIO_USAGE_MODEL=loadedFile.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER=loadedFile.getParent().toString();
			}
		}
	}

	/**
	 * Updates the values shown to the user according to those stored in the Configuration class
	 */
	public void loadConfiguration() {
		reposirotyTextField.setText(Configuration.PALLADIO_REPOSITORY_MODEL);
		systemTextField.setText(Configuration.PALLADIO_SYSTEM_MODEL);
		resourceTextField.setText(Configuration.PALLADIO_RESOURCE_MODEL);
		allocationTextField.setText(Configuration.PALLADIO_ALLOCATION_MODEL);
		usageTextField.setText(Configuration.PALLADIO_USAGE_MODEL);
		
	}
	
	/**
	 * Updates values in the Configuration class according to those selected in the panel
	 */
	public void updateConfiguration(){	
		if(!reposirotyTextField.getText().isEmpty())
			Configuration.PROJECT_BASE_FOLDER = Paths.get(reposirotyTextField.getText()).getParent().toString();
		Configuration.PALLADIO_REPOSITORY_MODEL = reposirotyTextField.getText();
		Configuration.PALLADIO_SYSTEM_MODEL = systemTextField.getText();
		Configuration.PALLADIO_RESOURCE_MODEL = resourceTextField.getText();
		Configuration.PALLADIO_ALLOCATION_MODEL = allocationTextField.getText();
		Configuration.PALLADIO_USAGE_MODEL = usageTextField.getText();
	}

}
