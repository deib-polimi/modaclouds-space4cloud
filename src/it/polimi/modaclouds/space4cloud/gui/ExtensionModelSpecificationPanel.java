package it.polimi.modaclouds.space4cloud.gui;

import it.polimi.modaclouds.qos_models.schema.Constraints;
import it.polimi.modaclouds.qos_models.schema.ResourceModelExtension;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtensions;
import it.polimi.modaclouds.space4cloud.utils.Configuration;

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

public class ExtensionModelSpecificationPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -804310895749263278L;

	
	private static final String PANEL_NAME = "Extension Model Panel";
	private JTextField usageExtensionTextField;
	private JTextField resourceEnvironmentTextField;
	private JTextField constraintTextField;
	private JButton usageExtensionButton;
	private JButton resourceEnvironmentButton;
	private JButton constraintButton;
	/**
	 * Create the panel.
	 */
	public ExtensionModelSpecificationPanel() {
		setName(PANEL_NAME);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{350, 50, 0};
		gridBagLayout.rowHeights = new int[] {30, 30, 30, 30, 30, 30, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		setLayout(gridBagLayout);

		JLabel usageExtensionLabel = new JLabel("Usage Model Extension");
		GridBagConstraints gbc_usageExtensionLabel = new GridBagConstraints();
		gbc_usageExtensionLabel.anchor = GridBagConstraints.WEST;
		gbc_usageExtensionLabel.insets = new Insets(0, 0, 5, 5);
		gbc_usageExtensionLabel.gridx = 0;
		gbc_usageExtensionLabel.gridy = 0;
		add(usageExtensionLabel, gbc_usageExtensionLabel);
		JLabel emptyLabel = new JLabel("");
		GridBagConstraints gbc_emptyLabel = new GridBagConstraints();
		gbc_emptyLabel.fill = GridBagConstraints.BOTH;
		gbc_emptyLabel.insets = new Insets(0, 0, 5, 0);
		gbc_emptyLabel.gridx = 1;
		gbc_emptyLabel.gridy = 0;
		add(emptyLabel, gbc_emptyLabel);

		usageExtensionTextField = new JTextField();
		GridBagConstraints gbc_usageExtensionTextField = new GridBagConstraints();
		gbc_usageExtensionTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_usageExtensionTextField.insets = new Insets(0, 0, 5, 5);
		gbc_usageExtensionTextField.gridx = 0;
		gbc_usageExtensionTextField.gridy = 1;
		add(usageExtensionTextField, gbc_usageExtensionTextField);
		usageExtensionTextField.setColumns(10);

		usageExtensionButton = new JButton("Load Model");	
		usageExtensionButton.addActionListener(this);
		GridBagConstraints gbc_usageExtensionButton = new GridBagConstraints();
		gbc_usageExtensionButton.insets = new Insets(0, 0, 5, 0);
		gbc_usageExtensionButton.gridx = 1;
		gbc_usageExtensionButton.gridy = 1;
		add(usageExtensionButton, gbc_usageExtensionButton);		


		JLabel resourceEnvironmentLabel = new JLabel("Resource Environment Extension");
		GridBagConstraints gbc_resourceEnvironmentLabel = new GridBagConstraints();
		gbc_resourceEnvironmentLabel.anchor = GridBagConstraints.WEST;
		gbc_resourceEnvironmentLabel.insets = new Insets(0, 0, 5, 5);
		gbc_resourceEnvironmentLabel.gridx = 0;
		gbc_resourceEnvironmentLabel.gridy = 2;
		add(resourceEnvironmentLabel, gbc_resourceEnvironmentLabel);
		JLabel emptyLabel1 = new JLabel("");
		GridBagConstraints gbc_emptyLabel1 = new GridBagConstraints();
		gbc_emptyLabel1.fill = GridBagConstraints.BOTH;
		gbc_emptyLabel1.insets = new Insets(0, 0, 5, 0);
		gbc_emptyLabel1.gridx = 1;
		gbc_emptyLabel1.gridy = 2;
		add(emptyLabel1, gbc_emptyLabel1);

		resourceEnvironmentTextField = new JTextField();
		GridBagConstraints gbc_resourceEnvironmentTextField = new GridBagConstraints();
		gbc_resourceEnvironmentTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_resourceEnvironmentTextField.insets = new Insets(0, 0, 5, 5);
		gbc_resourceEnvironmentTextField.gridx = 0;
		gbc_resourceEnvironmentTextField.gridy = 3;
		add(resourceEnvironmentTextField, gbc_resourceEnvironmentTextField);
		resourceEnvironmentTextField.setColumns(10);

		resourceEnvironmentButton = new JButton("Load Model");
		resourceEnvironmentButton.addActionListener(this);
		GridBagConstraints gbc_resourceEnvironmentButton = new GridBagConstraints();
		gbc_resourceEnvironmentButton.insets = new Insets(0, 0, 5, 0);
		gbc_resourceEnvironmentButton.gridx = 1;
		gbc_resourceEnvironmentButton.gridy = 3;
		add(resourceEnvironmentButton, gbc_resourceEnvironmentButton);

		JLabel constraintLabel = new JLabel("Constraints");
		GridBagConstraints gbc_constraintLabel = new GridBagConstraints();
		gbc_constraintLabel.anchor = GridBagConstraints.WEST;
		gbc_constraintLabel.insets = new Insets(0, 0, 5, 5);
		gbc_constraintLabel.gridx = 0;
		gbc_constraintLabel.gridy = 4;
		add(constraintLabel, gbc_constraintLabel);
		JLabel emptyLabel2 = new JLabel("");
		GridBagConstraints gbc_emptyLabel2 = new GridBagConstraints();
		gbc_emptyLabel2.fill = GridBagConstraints.BOTH;
		gbc_emptyLabel2.insets = new Insets(0, 0, 5, 0);
		gbc_emptyLabel2.gridx = 1;
		gbc_emptyLabel2.gridy = 4;
		add(emptyLabel2, gbc_emptyLabel2);


		constraintTextField = new JTextField();
		GridBagConstraints gbc_constraintTextField = new GridBagConstraints();
		gbc_constraintTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_constraintTextField.insets = new Insets(0, 0, 5, 5);
		gbc_constraintTextField.gridx = 0;
		gbc_constraintTextField.gridy = 5;
		add(constraintTextField, gbc_constraintTextField);
		constraintTextField.setColumns(10);

		constraintButton = new JButton("Load Model");
		constraintButton.addActionListener(this);
		GridBagConstraints gbc_constraintButton = new GridBagConstraints();
		gbc_constraintButton.insets = new Insets(0, 0, 5, 0);
		gbc_constraintButton.gridx = 1;
		gbc_constraintButton.gridy = 5;
		add(constraintButton, gbc_constraintButton);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 6;
		add(panel, gbc_panel);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		File loadedFile = null;
		//TODO: pass the loaded file to the configuration class
		if(e.getSource().equals(usageExtensionButton)){
			loadedFile = FileLoader.loadExtensionFile("Load Usage Extension",UsageModelExtensions.class);
			if(loadedFile!=null){	
				usageExtensionTextField.setText(loadedFile.getAbsolutePath());
				Configuration.USAGE_MODEL_EXTENSION=loadedFile.getAbsolutePath();
			}
		}else if(e.getSource().equals(resourceEnvironmentButton)){
			loadedFile = FileLoader.loadExtensionFile("Load Resource Environment Extension",ResourceModelExtension.class);
			if(loadedFile!=null){
				resourceEnvironmentTextField.setText(loadedFile.getAbsolutePath());
				Configuration.RESOURCE_ENVIRONMENT_EXTENSION=loadedFile.getAbsolutePath();
			}
		}else if(e.getSource().equals(constraintButton)){
			loadedFile = FileLoader.loadExtensionFile("Load Constraints",Constraints.class);
			if(loadedFile!=null){
				constraintTextField.setText(loadedFile.getAbsolutePath());
				Configuration.CONSTRAINTS=loadedFile.getAbsolutePath();
			}
		}
	}

	/**
	 * Updates the values shown to the user according to those stored in the Configuration class
	 */
	public void loadConfiguration() {
		usageExtensionTextField.setText(Configuration.USAGE_MODEL_EXTENSION);
		resourceEnvironmentTextField.setText(Configuration.RESOURCE_ENVIRONMENT_EXTENSION);
		constraintTextField.setText(Configuration.CONSTRAINTS);
	}
	
	/**
	 * Updates values in the Configuration class according to those selected in the panel
	 */
	public void updateConfiguration(){
		Configuration.USAGE_MODEL_EXTENSION = usageExtensionTextField.getText();
		Configuration.RESOURCE_ENVIRONMENT_EXTENSION = resourceEnvironmentTextField.getText();
		Configuration.CONSTRAINTS = constraintTextField.getText();
	}


}
