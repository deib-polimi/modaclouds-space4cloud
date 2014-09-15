package it.polimi.modaclouds.space4cloud.gui;

import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Operation;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Solver;

import java.awt.Dimension;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FunctionalityPanel extends JPanel implements ActionListener {

	//the property that can be listened when a change in the functionality is detected
	public static final String functionalityProperty = "functionality";
	private static final long serialVersionUID = -5356951769849277734L;
	private static final String PANEL_NAME = "Functionality Selection Panel";
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
	private JPanel panel;
	private JTextField randomEnvironmentText;
	private JButton randomEnvironmentButton;
	
	private JTextField cpus, cpuPower, ram, storage;
	private JCheckBox usePrivateCloud;
	private JLabel cpusLabel;
	private JLabel cpuPowerLabel;
	private JLabel ramLabel;
	private JLabel storageLabel;
	
	/**
	 * Create the panel.
	 */
	public FunctionalityPanel() {
		setName(PANEL_NAME);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{201, 201, 0};
		gridBagLayout.rowHeights = new int[] {35, 35, 35, 35, 35, 35, 35, 35, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
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
		gbc_operationBox.insets = new Insets(0, 0, 5, 0);
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
		gbc_solverBox.insets = new Insets(0, 0, 5, 0);
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
		gbc_emptyLabel.insets = new Insets(0, 0, 5, 0);
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
		gbc_dbLoadButton.insets = new Insets(0, 0, 5, 0);
		gbc_dbLoadButton.gridx = 1;
		gbc_dbLoadButton.gridy = 3;
		add(dbLoadButton, gbc_dbLoadButton);

		linePropLabel = new JLabel("LINE Configuration File");
		GridBagConstraints gbc_linePropLabel = new GridBagConstraints();
		gbc_linePropLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_linePropLabel.insets = new Insets(0, 0, 5, 5);
		gbc_linePropLabel.gridx = 0;
		gbc_linePropLabel.gridy = 4;
		linePropLabel.setVisible(false);
		add(linePropLabel, gbc_linePropLabel);

		emptyLabel2 = new JLabel("");
		GridBagConstraints gbc_emptyLabel2 = new GridBagConstraints();
		gbc_emptyLabel2.insets = new Insets(0, 0, 5, 0);
		gbc_emptyLabel2.gridx = 1;
		gbc_emptyLabel2.gridy = 4;
		emptyLabel.setVisible(false);
		add(emptyLabel2, gbc_emptyLabel2);

		lineConfText = new JTextField();
		GridBagConstraints gbc_lineConfText = new GridBagConstraints();
		gbc_lineConfText.insets = new Insets(0, 0, 5, 5);
		gbc_lineConfText.fill = GridBagConstraints.HORIZONTAL;
		gbc_lineConfText.gridx = 0;
		gbc_lineConfText.gridy = 5;
		add(lineConfText, gbc_lineConfText);
		lineConfText.setVisible(false);
		lineConfText.setColumns(10);

		lineConfButton = new JButton("Browse");
		lineConfButton.addActionListener(this);
		GridBagConstraints gbc_lineConfButton = new GridBagConstraints();
		gbc_lineConfButton.insets = new Insets(0, 0, 5, 0);
		gbc_lineConfButton.gridx = 1;
		gbc_lineConfButton.gridy = 5;
		lineConfButton.setVisible(false);
		add(lineConfButton, gbc_lineConfButton);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridx = 0;
		c.gridy = 6;
		c.insets = new Insets(0, 0, 5, 5);
		usePrivateCloud = new JCheckBox("Consider also a Private Cloud");
		add(usePrivateCloud, c);
		usePrivateCloud.addActionListener(this);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		add(new JLabel(""), c);
		
		c.gridx = 0;
		c.gridy++;
        c.insets = new Insets(0, 0, 5, 5);
        cpusLabel = new JLabel("CPUs");
		add(cpusLabel, c);
        cpusLabel.setVisible(false);
		cpus = new JTextField(10);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		add(cpus, c);
		cpus.setVisible(false);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);
		cpuPowerLabel = new JLabel("CPU power for each CPU [MHz]");
		add(cpuPowerLabel, c);
		cpuPowerLabel.setVisible(false);
		c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 0, 5, 0);
		cpuPower = new JTextField(10);
		add(cpuPower, c);
		cpuPower.setVisible(false);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);
		ramLabel = new JLabel("RAM [MB]");
		add(ramLabel, c);
		ramLabel.setVisible(false);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		ram = new JTextField(10);
		add(ram, c);
		ram.setVisible(false);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);
		storageLabel = new JLabel("Storage [GB]");
		add(storageLabel, c);
		storageLabel.setVisible(false);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		storage = new JTextField(10);
		add(storage, c);
		storage.setVisible(false);
		
		

		randomEnvironmentLabel = new JLabel("Random Environment (Optional)");
		GridBagConstraints gbc_randomEnvironmentLabel = new GridBagConstraints();
		gbc_randomEnvironmentLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_randomEnvironmentLabel.insets = new Insets(0, 0, 5, 5);
		gbc_randomEnvironmentLabel.gridx = 0;
		gbc_randomEnvironmentLabel.gridy = 6;
		randomEnvironmentLabel.setVisible(false);
		add(randomEnvironmentLabel, gbc_randomEnvironmentLabel);

		emptyLabel3 = new JLabel("");
		GridBagConstraints gbc_emptyLabel3 = new GridBagConstraints();
		gbc_emptyLabel3.insets = new Insets(0, 0, 5, 0);
		gbc_emptyLabel3.gridx = 1;
		gbc_emptyLabel3.gridy = 6;
		add(emptyLabel3, gbc_emptyLabel3);

		randomEnvironmentText = new JTextField();
		GridBagConstraints gbc_randomEnvironmentText = new GridBagConstraints();
		gbc_randomEnvironmentText.insets = new Insets(0, 0, 5, 5);
		gbc_randomEnvironmentText.fill = GridBagConstraints.HORIZONTAL;
		gbc_randomEnvironmentText.gridx = 0;
		gbc_randomEnvironmentText.gridy = 7;
		add(randomEnvironmentText, gbc_randomEnvironmentText);
		randomEnvironmentText.setVisible(false);
		randomEnvironmentText.setColumns(10);

		randomEnvironmentButton = new JButton("Browse");
		randomEnvironmentButton.addActionListener(this);
		GridBagConstraints gbc_randomEnvironmentButton = new GridBagConstraints();
		gbc_randomEnvironmentButton.insets = new Insets(0, 0, 5, 0);
		gbc_randomEnvironmentButton.gridx = 1;
		gbc_randomEnvironmentButton.gridy = 7;
		randomEnvironmentButton.setVisible(false);
		add(randomEnvironmentButton, gbc_randomEnvironmentButton);

		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 8;
		add(panel, gbc_panel);
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
			File dbConnectionFile = FileLoader.loadFile("Database Connection Parameters");
			if(dbConnectionFile!=null){
				Configuration.DB_CONNECTION_FILE=dbConnectionFile.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER=dbConnectionFile.getParent().toString();
				dbConfText.setText(dbConnectionFile.getAbsolutePath());
			}
		}else if(e.getSource().equals(lineConfButton)){
			File linePropFile = FileLoader.loadFile("LINE Server Connection Parameters");
			if(linePropFile!=null){
				Configuration.LINE_PROP_FILE=linePropFile.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER=linePropFile.getParent().toString();
				lineConfText.setText(linePropFile.getAbsolutePath());
			}
		} else if(e.getSource().equals(usePrivateCloud)){
			updatePrivateCloudVisibility();
		}
		else if(e.getSource().equals(randomEnvironmentButton)){
			File randomEnvFile = FileLoader.loadFile("Random Enviroment specification");
			if(randomEnvFile!=null){
				Configuration.RANDOM_ENV_FILE=randomEnvFile.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER=randomEnvFile.getParent().toString();
				randomEnvironmentText.setText(randomEnvFile.getAbsolutePath());
			}
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
		usePrivateCloud.setSelected(Configuration.USE_PRIVATE_CLOUD);
		cpus.setText(Integer.toString(Configuration.PRIVATE_CPUS));
		cpuPower.setText(Double.toString(Configuration.PRIVATE_CPUPOWER));
		ram.setText(Integer.toString(Configuration.PRIVATE_RAM));
		storage.setText(Integer.toString(Configuration.PRIVATE_STORAGE));
		
		updatePrivateCloudVisibility();
	}


	/**
	 * Updates the selection of the solver showing or hiding the parameters for the specific solver
	 * @param solver
	 */
	private void setSolver(Solver solver){
		solverBox.setSelectedItem(solver);
		if(solver.equals(Solver.LINE)){
			//show LINE configuration and random environment
			lineConfButton.setVisible(true);
			lineConfText.setVisible(true);
			linePropLabel.setVisible(true);
			emptyLabel2.setVisible(true);
			
			randomEnvironmentButton.setVisible(true);
			randomEnvironmentText.setVisible(true);
			randomEnvironmentLabel.setVisible(true);
			emptyLabel3.setVisible(true);
		}else{			
			
			//hide LINE configuration and random environment
			lineConfButton.setVisible(false);
			lineConfText.setVisible(false);
			linePropLabel.setVisible(false);
			emptyLabel2.setVisible(false);
			
			randomEnvironmentButton.setVisible(false);
			randomEnvironmentText.setVisible(false);
			randomEnvironmentLabel.setVisible(false);
			emptyLabel3.setVisible(false);
			
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
	private void updatePrivateCloudVisibility(){
		boolean shown = usePrivateCloud.isSelected();
		
		cpus.setVisible(shown);
		cpuPower.setVisible(shown);
		ram.setVisible(shown);
		storage.setVisible(shown);
		cpusLabel.setVisible(shown);
		cpuPowerLabel.setVisible(shown);
		ramLabel.setVisible(shown);
		storageLabel.setVisible(shown);
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
		
		try{
			Configuration.PRIVATE_CPUS = Integer.parseInt(cpus.getText());
		} catch (NumberFormatException e){
			Configuration.PRIVATE_CPUS = -1;
		}
		try{
			Configuration.PRIVATE_CPUPOWER = Double.parseDouble(cpuPower.getText());
		} catch (NumberFormatException e){
			Configuration.PRIVATE_CPUPOWER = -1;
		}
		try{
			Configuration.PRIVATE_RAM = Integer.parseInt(ram.getText());
		} catch (NumberFormatException e){
			Configuration.PRIVATE_RAM = -1;
		}
		try{
			Configuration.PRIVATE_STORAGE = Integer.parseInt(storage.getText());
		} catch (NumberFormatException e){
			Configuration.PRIVATE_STORAGE = -1;
		}
		Configuration.USE_PRIVATE_CLOUD = usePrivateCloud.isSelected();
	}
	
	public static void main(String[] args) {
		JFrame gui = new JFrame();
		gui.setMinimumSize(new Dimension(900,500));
		gui.setLocationRelativeTo(null);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // .DISPOSE_ON_CLOSE);
		
		FunctionalityPanel panel = new FunctionalityPanel();
		gui.add(panel);
		
		gui.setVisible(true);
	}

}
