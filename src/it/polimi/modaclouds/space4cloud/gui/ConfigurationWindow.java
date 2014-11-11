package it.polimi.modaclouds.space4cloud.gui;

import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Operation;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationWindow extends WindowAdapter implements ActionListener, PropertyChangeListener, ChangeListener {

	private JFrame frame;
	private JTabbedPane tabbedPane;	
	private JButton loadConfigurationButton;
	private JButton saveConfigurationButton;
	private JButton startButton;
	private PalladioModelSpecificationPanel modelSelectionPane;
	private ExtensionModelSpecificationPanel extensionSelectionPane;
	private FunctionalityPanel functionalityPane;
	private OptimizationConfigurationPanel optimizationConfigurationPane;
	private RobustnessConfigurationPanel robustnessConfigurationPane;
	private CloudBurstingPanel cloudBurstingPanel;
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationWindow.class);
	private boolean disposed = false;
	private boolean cancelled = true;
	private static final String FRAME_NAME = "Space4Cloud Configuration Window";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConfigurationWindow window = new ConfigurationWindow();
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
	public ConfigurationWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
//		frame.setBounds(100, 100, 701, 431);		
		frame.setMinimumSize(new Dimension(950,600));
		frame.setLocationRelativeTo(null);
		frame.setTitle(FRAME_NAME);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{685, 0};
		gridBagLayout.rowHeights = new int[]{196, 60, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(this);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		frame.getContentPane().add(tabbedPane, gbc_tabbedPane);

		JPanel lowerPane = new JPanel();
		GridBagConstraints gbc_lowerPane = new GridBagConstraints();
		gbc_lowerPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_lowerPane.gridx = 0;
		gbc_lowerPane.gridy = 1;
		frame.getContentPane().add(lowerPane, gbc_lowerPane);
		GridBagLayout gbl_lowerPane = new GridBagLayout();
		gbl_lowerPane.columnWidths = new int[] {342, 100, 100, 100};
		gbl_lowerPane.rowHeights = new int[] {23};
		gbl_lowerPane.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0};
		gbl_lowerPane.rowWeights = new double[]{0.0};
		lowerPane.setLayout(gbl_lowerPane);

		JLabel emptyLabel = new JLabel("");
		GridBagConstraints gbc_emptyLabel = new GridBagConstraints();
		gbc_emptyLabel.fill = GridBagConstraints.BOTH;
		gbc_emptyLabel.insets = new Insets(0, 0, 5, 5);
		gbc_emptyLabel.gridx = 0;
		gbc_emptyLabel.gridy = 0;
		lowerPane.add(emptyLabel, gbc_emptyLabel);

		loadConfigurationButton = new JButton("Load Configuration");
		loadConfigurationButton.addActionListener(this);
		GridBagConstraints gbc_loadConfigurationButton = new GridBagConstraints();
		gbc_loadConfigurationButton.insets = new Insets(0, 0, 5, 5);
		gbc_loadConfigurationButton.gridx = 1;
		gbc_loadConfigurationButton.gridy = 0;
		lowerPane.add(loadConfigurationButton, gbc_loadConfigurationButton);

		saveConfigurationButton = new JButton("Save Configuration");
		saveConfigurationButton.addActionListener(this);
		GridBagConstraints gbc_saveConfigurationButton = new GridBagConstraints();
		gbc_saveConfigurationButton.insets = new Insets(0, 0, 5, 5);
		gbc_saveConfigurationButton.gridx = 2;
		gbc_saveConfigurationButton.gridy = 0;
		lowerPane.add(saveConfigurationButton, gbc_saveConfigurationButton);

		startButton = new JButton("Start");
		startButton.addActionListener(this);
		GridBagConstraints gbc_startButton = new GridBagConstraints();
		gbc_startButton.insets = new Insets(0, 0, 5, 0);
		gbc_startButton.gridx = 3;
		gbc_startButton.gridy = 0;
		lowerPane.add(startButton, gbc_startButton);

		modelSelectionPane = new PalladioModelSpecificationPanel();
		modelSelectionPane.setPreferredSize(frame.getContentPane().getPreferredSize());	
		tabbedPane.addTab(modelSelectionPane.getName(), modelSelectionPane);

		extensionSelectionPane = new ExtensionModelSpecificationPanel();
		extensionSelectionPane.setPreferredSize(frame.getContentPane().getPreferredSize());
		tabbedPane.addTab(extensionSelectionPane.getName(), extensionSelectionPane);

		functionalityPane = new FunctionalityPanel();
		functionalityPane.setPreferredSize(frame.getContentPane().getPreferredSize());
		functionalityPane.addPropertyChangeListener(FunctionalityPanel.functionalityProperty,this);
		functionalityPane.addPropertyChangeListener(FunctionalityPanel.privateCloudProperty,this);
		tabbedPane.addTab(functionalityPane.getName(), functionalityPane);		

		optimizationConfigurationPane = new OptimizationConfigurationPanel();
		optimizationConfigurationPane.setPreferredSize(frame.getContentPane().getPreferredSize());
		optimizationConfigurationPane.setEnabled(false);		
		tabbedPane.addTab(optimizationConfigurationPane.getName(), optimizationConfigurationPane);
		int tabNumber = tabbedPane.indexOfComponent(optimizationConfigurationPane);
		tabbedPane.setEnabledAt(tabNumber, false);
		
		robustnessConfigurationPane = new RobustnessConfigurationPanel();
		robustnessConfigurationPane.setPreferredSize(frame.getContentPane().getPreferredSize());
		robustnessConfigurationPane.setEnabled(false);		
		tabbedPane.addTab(robustnessConfigurationPane.getName(), robustnessConfigurationPane);
		tabNumber = tabbedPane.indexOfComponent(robustnessConfigurationPane);
		tabbedPane.setEnabledAt(tabNumber, false);
		
		cloudBurstingPanel = new CloudBurstingPanel();
		cloudBurstingPanel.setPreferredSize(frame.getContentPane().getPreferredSize());
		tabbedPane.addTab(cloudBurstingPanel.getName(), cloudBurstingPanel);
		tabNumber = tabbedPane.indexOfComponent(cloudBurstingPanel);
		tabbedPane.setEnabledAt(tabNumber, false);

		loadConfiguration();
		
		tabbedPane.addChangeListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(startButton)){
			modelSelectionPane.updateConfiguration();
			extensionSelectionPane.updateConfiguration();
			functionalityPane.updateConfiguration();
			if(Configuration.USE_PRIVATE_CLOUD)
				cloudBurstingPanel.updateConfiguration();
			if (Configuration.FUNCTIONALITY == Operation.Optimization)
				optimizationConfigurationPane.updateConfiguration();
			else if (Configuration.FUNCTIONALITY == Operation.Robustness) {
				robustnessConfigurationPane.updateConfiguration();
				optimizationConfigurationPane.updateConfiguration();
			}
			List<String> errors = Configuration.checkValidity();
			if(!errors.isEmpty()){
				String message="";
				for(String s:errors)
					message +=s+"\n";
				JOptionPane.showMessageDialog(frame, message, "Configuration Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			//log the configuration
			Configuration.flushLog();
			if(Configuration.USE_PRIVATE_CLOUD)
				try {
					File tempFile = File.createTempFile("space4cloudBurst", ".properties");
					Configuration.PRIVATE_CLOUD_HOSTS_TMP = tempFile.getAbsolutePath();
					CloudBurstingPanel.saveFile(tempFile);
				} catch (IOException e1) {
					logger.error("Could not save the temporary file for the private cloud configuration");
				}
			cancelled = false;
			disposed = true;
			frame.dispose();
		}else if (e.getSource().equals(loadConfigurationButton)){
			File configurationFile = FileLoader.loadFile("Load SPACE4Cloud Configuration", "properties", "prop");
			if(configurationFile !=  null){
				try {
					Configuration.loadConfiguration(configurationFile.getAbsolutePath());					
//					modelSelectionPane.loadConfiguration();
//					extensionSelectionPane.loadConfiguration();	
//					functionalityPane.loadConfiguration();
//					optimizationConfigurationPane.loadConfiguration();
//					robustnessConfigurationPane.loadConfiguration();
					loadConfiguration();
				} catch (IOException ex) {
					logger.error("Could not load the configuration from the file",e);
				}
			}

		}else if(e.getSource().equals(saveConfigurationButton)){
			modelSelectionPane.updateConfiguration();
			extensionSelectionPane.updateConfiguration();
			functionalityPane.updateConfiguration();		
			if (Configuration.FUNCTIONALITY == Operation.Optimization)
				optimizationConfigurationPane.updateConfiguration();
			else if (Configuration.FUNCTIONALITY == Operation.Robustness)
				robustnessConfigurationPane.updateConfiguration();
			
			if (Configuration.USE_PRIVATE_CLOUD == true)
				cloudBurstingPanel.updateConfiguration();
			
			File configurationFile = FileLoader.saveFile("Load SPACE4Cloud Configuration", "properties");
			if(configurationFile!=null){
				try {					
					Configuration.saveConfiguration(configurationFile.getAbsolutePath());
				} catch (IOException ex) {
					logger.error("Could not save the configuration to the file",e);
				}				
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		//when the user changes the selected functionality disable not relevant tabs
		//The if should not be necessary
		if(evt.getPropertyName().equals(FunctionalityPanel.functionalityProperty)){
			if (Configuration.FUNCTIONALITY.equals(Operation.Optimization)){
				optimizationConfigurationPane.setEnabled(true);
				int tabNumber = tabbedPane.indexOfComponent(optimizationConfigurationPane);
				tabbedPane.setEnabledAt(tabNumber, true);
				robustnessConfigurationPane.setEnabled(false);
				tabNumber = tabbedPane.indexOfComponent(robustnessConfigurationPane);
				tabbedPane.setEnabledAt(tabNumber, false);
			} else if (Configuration.FUNCTIONALITY.equals(Operation.Robustness)){
				optimizationConfigurationPane.setEnabled(true);
				int tabNumber = tabbedPane.indexOfComponent(optimizationConfigurationPane);
				tabbedPane.setEnabledAt(tabNumber, true);
				robustnessConfigurationPane.setEnabled(true);
				tabNumber = tabbedPane.indexOfComponent(robustnessConfigurationPane);
				tabbedPane.setEnabledAt(tabNumber, true);
			} else{
				optimizationConfigurationPane.setEnabled(false);
				int tabNumber = tabbedPane.indexOfComponent(optimizationConfigurationPane);
				tabbedPane.setEnabledAt(tabNumber, false);
				robustnessConfigurationPane.setEnabled(false);
				tabNumber = tabbedPane.indexOfComponent(robustnessConfigurationPane);
				tabbedPane.setEnabledAt(tabNumber, false);
			}
		} else if(evt.getPropertyName().equals(FunctionalityPanel.privateCloudProperty)) {
			boolean visibility = Configuration.USE_PRIVATE_CLOUD;
			cloudBurstingPanel.setEnabled(visibility);
			int tabNumber = tabbedPane.indexOfComponent(cloudBurstingPanel);
			tabbedPane.setEnabledAt(tabNumber, visibility);
		}
	}

	public boolean hasBeenDisposed() {
		return disposed;
	}

	public boolean isCancelled() {
		return cancelled;
	}


	@Override
	public void windowClosing(WindowEvent e) {		
		super.windowClosing(e);
		frame.dispose();
		disposed = true;
	}

	public void show() {
		frame.setVisible(true);
	}
	
	public void loadConfiguration() {
		modelSelectionPane.loadConfiguration();
		extensionSelectionPane.loadConfiguration();	
		functionalityPane.loadConfiguration();
		optimizationConfigurationPane.loadConfiguration();
		robustnessConfigurationPane.loadConfiguration();
		cloudBurstingPanel.loadConfiguration();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		int index = tabbedPane.getSelectedIndex();
		String tabTitle = tabbedPane.getTitleAt(index);
		
		if (tabTitle.equals(optimizationConfigurationPane.getName()))
			optimizationConfigurationPane.updateSSHVisibility();
			
	}

}


