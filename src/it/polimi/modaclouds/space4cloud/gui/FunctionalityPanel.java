package it.polimi.modaclouds.space4cloud.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Operation;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Solver;

public class FunctionalityPanel extends JPanel implements ActionListener {

	//the property that can be listened when a change in the functionality is detected
	public static final String functionalityProperty = "functionality";
	public static final String privateCloudProperty = "privateCloud";
	public static final String contractorProperty = "contractor";
	public static final String design2runtimeProperty = "design2runtime";
	
	private static final long serialVersionUID = -5356951769849277734L;
	private static final String PANEL_NAME = "Functionality"; //"Functionality Selection";
	private JComboBox<Operation> operationBox;
	private JComboBox<Solver> solverBox;
	private JTextField dbConfText;
	private JButton dbLoadButton;
	private JTextField lineConfText;
	private JButton lineConfButton;
	private JLabel linePropLabel;
	private JLabel emptyLabel2;
	private JLabel randomEnvironmentLabel;
	private JLabel emptyLabel3;
	private JTextField randomEnvironmentText;
	private JButton randomEnvironmentButton;

	private JCheckBox usePrivateCloud;
	private JCheckBox useContractor;
	private JCheckBox generateDesign2Runtime;

	/**
	 * Create the panel.
	 */
	public FunctionalityPanel() {
		setName(PANEL_NAME);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {201, 201, 0};
		gridBagLayout.rowHeights = new int[] {35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		setLayout(gridBagLayout);

		JLabel operationLabel = new JLabel("Operation");
		GridBagConstraints gbc_operationLabel = new GridBagConstraints();
		gbc_operationLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_operationLabel.insets = new Insets(0, 0, 5, 5);
		gbc_operationLabel.gridx = 0;
		gbc_operationLabel.gridy = 0;
		add(operationLabel, gbc_operationLabel);

		operationBox = new JComboBox<Operation>();
		operationBox.setMaximumRowCount(3);
		operationBox.setSelectedItem(Operation.Assessment);
		operationBox.setModel(new DefaultComboBoxModel<Operation>(Operation.values()));
		operationBox.addActionListener(this);
		GridBagConstraints gbc_operationBox = new GridBagConstraints();
		gbc_operationBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_operationBox.insets = new Insets(0, 0, 5, 5);
		gbc_operationBox.gridx = 1;
		gbc_operationBox.gridy = 0;
		add(operationBox, gbc_operationBox);


		JLabel solverLabel = new JLabel("Performance Engine");
		GridBagConstraints gbc_solverLabel = new GridBagConstraints();
		gbc_solverLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_solverLabel.insets = new Insets(0, 0, 5, 5);
		gbc_solverLabel.gridx = 0;
		gbc_solverLabel.gridy = 1;
		add(solverLabel, gbc_solverLabel);

		solverBox = new JComboBox<Solver>();
		solverBox.setMaximumRowCount(2);
		solverBox.setSelectedItem(Solver.LQNS);
		solverBox.setModel(new DefaultComboBoxModel<Solver>(Solver.values()));
		solverBox.addActionListener(this);
		GridBagConstraints gbc_solverBox = new GridBagConstraints();
		gbc_solverBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_solverBox.insets = new Insets(0, 0, 5, 5);
		gbc_solverBox.gridx = 1;
		gbc_solverBox.gridy = 1;
		add(solverBox, gbc_solverBox);

		JLabel dbConfLabel = new JLabel("Data Base Connection Properties");
		GridBagConstraints gbc_dbConfLabel = new GridBagConstraints();
		gbc_dbConfLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_dbConfLabel.insets = new Insets(0, 0, 5, 5);
		gbc_dbConfLabel.gridx = 0;
		gbc_dbConfLabel.gridy = 2;
		add(dbConfLabel, gbc_dbConfLabel);

		JLabel emptyLabel = new JLabel("");
		GridBagConstraints gbc_emptyLabel = new GridBagConstraints();
		gbc_emptyLabel.insets = new Insets(0, 0, 5, 5);
		gbc_emptyLabel.fill = GridBagConstraints.BOTH;
		gbc_emptyLabel.gridx = 1;
		gbc_emptyLabel.gridy = 2;
		add(emptyLabel, gbc_emptyLabel);

		dbConfText = new JTextField();
		GridBagConstraints gbc_dbConfText = new GridBagConstraints();
		gbc_dbConfText.insets = new Insets(0, 0, 5, 5);
		gbc_dbConfText.fill = GridBagConstraints.HORIZONTAL;
		gbc_dbConfText.gridx = 0;
		gbc_dbConfText.gridy = 3;
		add(dbConfText, gbc_dbConfText);
		dbConfText.setColumns(10);

		dbLoadButton = new JButton("Browse");
		dbLoadButton.addActionListener(this);
		GridBagConstraints gbc_dbLoadButton = new GridBagConstraints();
		gbc_dbLoadButton.insets = new Insets(0, 0, 5, 5);
		gbc_dbLoadButton.gridx = 1;
		gbc_dbLoadButton.gridy = 3;
		add(dbLoadButton, gbc_dbLoadButton);

		linePropLabel = new JLabel("LINE Configuration File");
		GridBagConstraints gbc_linePropLabel = new GridBagConstraints();
		gbc_linePropLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_linePropLabel.insets = new Insets(0, 0, 5, 5);
		gbc_linePropLabel.gridx = 0;
		gbc_linePropLabel.gridy = 4;
		linePropLabel.setEnabled(false); //setVisible(false);
		add(linePropLabel, gbc_linePropLabel);

		emptyLabel2 = new JLabel("");
		GridBagConstraints gbc_emptyLabel2 = new GridBagConstraints();
		gbc_emptyLabel2.insets = new Insets(0, 0, 5, 5);
		gbc_emptyLabel2.gridx = 1;
		gbc_emptyLabel2.gridy = 4;
		emptyLabel2.setEnabled(false); //setVisible(false);
		add(emptyLabel2, gbc_emptyLabel2);

		lineConfText = new JTextField();
		GridBagConstraints gbc_lineConfText = new GridBagConstraints();
		gbc_lineConfText.insets = new Insets(0, 0, 5, 5);
		gbc_lineConfText.fill = GridBagConstraints.HORIZONTAL;
		gbc_lineConfText.gridx = 0;
		gbc_lineConfText.gridy = 5;
		add(lineConfText, gbc_lineConfText);
		lineConfText.setEnabled(false); //setVisible(false);
		lineConfText.setColumns(10);

		lineConfButton = new JButton("Browse");
		lineConfButton.addActionListener(this);
		GridBagConstraints gbc_lineConfButton = new GridBagConstraints();
		gbc_lineConfButton.insets = new Insets(0, 0, 5, 5);
		gbc_lineConfButton.gridx = 1;
		gbc_lineConfButton.gridy = 5;
		lineConfButton.setEnabled(false); //setVisible(false);
		add(lineConfButton, gbc_lineConfButton);
		
		/*
		JPanel pan = new JPanel(new BorderLayout());
		lineConfText = new JTextField();
		GridBagConstraints gbc_lineConfText = new GridBagConstraints();
		gbc_lineConfText.insets = new Insets(0, 0, 5, 5);
		gbc_lineConfText.fill = GridBagConstraints.HORIZONTAL;
		gbc_lineConfText.gridx = 0;
		gbc_lineConfText.gridy = 5;
		gbc_lineConfText.gridwidth = 2;
		pan.add(lineConfText, BorderLayout.CENTER);
		lineConfText.setEnabled(false); //setVisible(false);
		lineConfText.setColumns(10);

		lineConfButton = new JButton("Browse");
		lineConfButton.addActionListener(this);
		lineConfButton.setEnabled(false); //setVisible(false);
		pan.add(lineConfButton, BorderLayout.LINE_END);
		add(pan, gbc_lineConfText);
		*/

//		GridBagConstraints gbc_emptyLabel4 = new GridBagConstraints();	
//		gbc_emptyLabel4.gridx = 0;
//		gbc_emptyLabel4.gridy = 7;
//		gbc_emptyLabel4.insets = new Insets(0, 0, 5, 5);		
//		add(new JLabel(""),gbc_emptyLabel4);



		randomEnvironmentLabel = new JLabel("Random Environment (Optional)");
		GridBagConstraints gbc_randomEnvironmentLabel = new GridBagConstraints();
		gbc_randomEnvironmentLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_randomEnvironmentLabel.insets = new Insets(0, 0, 5, 5);
		gbc_randomEnvironmentLabel.gridx = 0;
		gbc_randomEnvironmentLabel.gridy = 6;
		randomEnvironmentLabel.setEnabled(false); //setVisible(false);
		add(randomEnvironmentLabel, gbc_randomEnvironmentLabel);

		emptyLabel3 = new JLabel("");
		GridBagConstraints gbc_emptyLabel3 = new GridBagConstraints();
		gbc_emptyLabel3.insets = new Insets(0, 0, 5, 5);
		gbc_emptyLabel3.gridx = 1;
		gbc_emptyLabel3.gridy = 6;
		emptyLabel3.setEnabled(false); //setVisible(false);
		add(emptyLabel3, gbc_emptyLabel3);

		randomEnvironmentButton = new JButton("Browse");
		randomEnvironmentButton.addActionListener(this);
		GridBagConstraints gbc_randomEnvironmentButton = new GridBagConstraints();
		gbc_randomEnvironmentButton.insets = new Insets(0, 0, 5, 5);
		gbc_randomEnvironmentButton.gridx = 1;
		gbc_randomEnvironmentButton.gridy = 7;
		randomEnvironmentButton.setEnabled(false); //setVisible(false);
		add(randomEnvironmentButton, gbc_randomEnvironmentButton);

		randomEnvironmentText = new JTextField();
		GridBagConstraints gbc_randomEnvironmentText = new GridBagConstraints();
		gbc_randomEnvironmentText.fill = GridBagConstraints.HORIZONTAL;
		gbc_randomEnvironmentText.insets = new Insets(0, 0, 5, 5);
		gbc_randomEnvironmentText.gridx = 0;
		gbc_randomEnvironmentText.gridy = 7;
		add(randomEnvironmentText, gbc_randomEnvironmentText);
		randomEnvironmentText.setEnabled(false); //setVisible(false);
		randomEnvironmentText.setColumns(10);

		GridBagConstraints gbc_privateCloudBox = new GridBagConstraints();
		gbc_privateCloudBox.fill = GridBagConstraints.HORIZONTAL;		
		gbc_privateCloudBox.gridx = 0;
		gbc_privateCloudBox.gridy = 8;
		gbc_privateCloudBox.insets = new Insets(0, 0, 5, 5);
		usePrivateCloud = new JCheckBox("Use a Private Cloud");
		add(usePrivateCloud, gbc_privateCloudBox);
		usePrivateCloud.addActionListener(this);
		
		GridBagConstraints gbc_contractor = new GridBagConstraints();
		gbc_contractor.fill = GridBagConstraints.HORIZONTAL;		
		gbc_contractor.gridx = 0;
		gbc_contractor.gridy = 9;
		gbc_contractor.insets = new Insets(0, 0, 5, 5);
		useContractor = new JCheckBox("Consider the contracts (when possible)");
		add(useContractor, gbc_contractor);
		useContractor.addActionListener(this);
		
		GridBagConstraints gbc_design2runtime = new GridBagConstraints();
		gbc_design2runtime.fill = GridBagConstraints.HORIZONTAL;		
		gbc_design2runtime.gridx = 0;
		gbc_design2runtime.gridy = 10;
		gbc_design2runtime.insets = new Insets(0, 0, 5, 5);
		generateDesign2Runtime = new JCheckBox("Generate the Design2Runtime files");
		add(generateDesign2Runtime, gbc_design2runtime);
		generateDesign2Runtime.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(operationBox)){
			Operation oldFunctionality = Configuration.FUNCTIONALITY;
			Configuration.FUNCTIONALITY = (Operation) operationBox.getSelectedItem();
			firePropertyChange(functionalityProperty, oldFunctionality, Configuration.FUNCTIONALITY);
		}else if(e.getSource().equals(solverBox)){
			Configuration.SOLVER = (Solver) solverBox.getSelectedItem();
			setSolver((Solver) solverBox.getSelectedItem());
		}else if(e.getSource().equals(dbLoadButton)){
			File dbConnectionFile = FileLoader.loadFile("Database Connection Parameters", "properties");
			if(dbConnectionFile!=null){
				Configuration.DB_CONNECTION_FILE=dbConnectionFile.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER=dbConnectionFile.getParent().toString();
				dbConfText.setText(dbConnectionFile.getAbsolutePath());
			}
		}else if(e.getSource().equals(lineConfButton)){
			File linePropFile = FileLoader.loadFile("LINE Server Connection Parameters", "properties");
			if(linePropFile!=null){
				Configuration.LINE_PROP_FILE=linePropFile.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER=linePropFile.getParent().toString();
				lineConfText.setText(linePropFile.getAbsolutePath());
			}
		} else if(e.getSource().equals(usePrivateCloud)){
			Configuration.USE_PRIVATE_CLOUD = usePrivateCloud.isSelected();
			setPrivateCloudVisibility(usePrivateCloud.isSelected());
		} else if(e.getSource().equals(useContractor)){
			Configuration.CONTRACTOR_TEST = useContractor.isSelected();
			setContractorVisibility(useContractor.isSelected());
		} else if(e.getSource().equals(randomEnvironmentButton)){
			File randomEnvFile = FileLoader.loadFile("Random Enviroment specification"); // TODO: what extension should this have?
			if(randomEnvFile!=null){
				Configuration.RANDOM_ENV_FILE=randomEnvFile.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER=randomEnvFile.getParent().toString();
				randomEnvironmentText.setText(randomEnvFile.getAbsolutePath());
			}
		} else if(e.getSource().equals(generateDesign2Runtime)){
			Configuration.GENERATE_DESIGN_TO_RUNTIME_FILES = generateDesign2Runtime.isSelected();
			setDesign2RuntimeVisibility(generateDesign2Runtime.isSelected());
		} 
	}

	/**
	 * Updates the values shown to the user according to those stored in the Configuration class
	 */
	public void loadConfiguration() {
		if (Configuration.FUNCTIONALITY != null)
			setFunctionality(Configuration.FUNCTIONALITY);
		if (Configuration.SOLVER != null)
			setSolver(Configuration.SOLVER);
		dbConfText.setText(Configuration.DB_CONNECTION_FILE);
		lineConfText.setText(Configuration.LINE_PROP_FILE);
		randomEnvironmentText.setText(Configuration.RANDOM_ENV_FILE);
		
		setPrivateCloudVisibility(Configuration.USE_PRIVATE_CLOUD);
		setContractorVisibility(Configuration.CONTRACTOR_TEST);
		setDesign2RuntimeVisibility(Configuration.GENERATE_DESIGN_TO_RUNTIME_FILES);
	}


	/**
	 * Updates the selection of the solver showing or hiding the parameters for the specific solver
	 * @param solver
	 */
	private void setSolver(Solver solver){
		solverBox.setSelectedItem(solver);
		if(solver.equals(Solver.LINE)){
			//show LINE configuration and random environment
			lineConfButton.setEnabled(true); //setVisible(true);
			lineConfText.setEnabled(true); //setVisible(true);
			linePropLabel.setEnabled(true); //setVisible(true);
			emptyLabel2.setEnabled(true); //setVisible(true);

			randomEnvironmentButton.setEnabled(true); //setVisible(true);
			randomEnvironmentText.setEnabled(true); //setVisible(true);
			randomEnvironmentLabel.setEnabled(true); //setVisible(true);
			emptyLabel3.setEnabled(true); //setVisible(true);
		}else{			

			//hide LINE configuration and random environment
			lineConfButton.setEnabled(false); //setVisible(false);
			lineConfText.setEnabled(false); //setVisible(false);
			linePropLabel.setEnabled(false); //setVisible(false);
			emptyLabel2.setEnabled(false); //setVisible(false);

			randomEnvironmentButton.setEnabled(false); //setVisible(false);
			randomEnvironmentText.setEnabled(false); //setVisible(false);
			randomEnvironmentLabel.setEnabled(false); //setVisible(false);
			emptyLabel3.setEnabled(false); //setVisible(false);

		}
	}


	/**
	 * Updates the selection of the functionality firing the change of the related property
	 * @param functionality
	 */
	private void setFunctionality(Operation functionality){
		Operation oldFun = (Operation) operationBox.getSelectedItem();
		operationBox.setSelectedItem(functionality);
		firePropertyChange(functionalityProperty, oldFun, functionality);
	}

	/**
	 * Updates the visibility of the private cloud parameters according to the box selection
	 */
	private void setPrivateCloudVisibility(boolean shown) {
//		boolean old = usePrivateCloud.isSelected();
		usePrivateCloud.setSelected(shown);
		firePropertyChange(privateCloudProperty, !shown, shown);
	}
	
	private void setContractorVisibility(boolean shown) {
//		boolean old = usePrivateCloud.isSelected();
		useContractor.setSelected(shown);
		firePropertyChange(contractorProperty, !shown, shown);
	}
	
	private void setDesign2RuntimeVisibility(boolean shown) {
//		boolean old = usePrivateCloud.isSelected();
		generateDesign2Runtime.setSelected(shown);
		firePropertyChange(design2runtimeProperty, !shown, shown);
	}

	/**
	 * Updates values in the Configuration class according to those selected in the panel
	 */
	public void updateConfiguration(){
		Configuration.FUNCTIONALITY = (Operation) operationBox.getSelectedItem();
		Configuration.SOLVER = (Solver) solverBox.getSelectedItem();
		Configuration.DB_CONNECTION_FILE = dbConfText.getText();
		Configuration.LINE_PROP_FILE = lineConfText.getText();
		Configuration.RANDOM_ENV_FILE = randomEnvironmentText.getText();
		Configuration.USE_PRIVATE_CLOUD = usePrivateCloud.isSelected();
		Configuration.CONTRACTOR_TEST = useContractor.isSelected();
		Configuration.GENERATE_DESIGN_TO_RUNTIME_FILES = generateDesign2Runtime.isSelected();
	}

}