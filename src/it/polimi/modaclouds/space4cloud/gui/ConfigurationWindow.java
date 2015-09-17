package it.polimi.modaclouds.space4cloud.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Operation;

public class ConfigurationWindow extends JFrame implements WindowListener, ActionListener, PropertyChangeListener, ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5168741744894100845L;
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
	private ExternalServerPanel externalServerPanel;
	private Design2RuntimePanel design2RuntimePanel;
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationWindow.class);
	private boolean disposed = false;
	private boolean cancelled = true;
	private static final String FRAME_NAME = "Space 4Clouds Configuration Window";
	private JLabel logoLabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConfigurationWindow window = new ConfigurationWindow();
					window.setVisible(true);
				} catch (Exception e) {
					logger.error("Error while initializing the GUI.", e);
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ConfigurationWindow() {
		super();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		//		frame.setBounds(100, 100, 701, 431);		
		setMinimumSize(new Dimension(950,600));
		setLocationRelativeTo(null);
		setTitle(FRAME_NAME);
		Image favicon = new ImageIcon(FrameworkUtil.getBundle(ConfigurationWindow.class).getEntry("icons/Cloud.png")).getImage();
		setIconImage(favicon);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{685, 0};
		gridBagLayout.rowHeights = new int[]{196, 60, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);

//		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP) {

            /**
			 * 
			 */
			private static final long serialVersionUID = 5557207405690424496L;

			// delegating to tabComponent 
            @Override
            public void setEnabledAt( int index, boolean enabled ) {
                super.setEnabledAt( index, enabled );
                Component tabComponent = getTabComponentAt( index );
                if(tabComponent != null) {
                    getTabComponentAt( index ).setEnabled( enabled );
                }
            }
            
            @Override
            public void addTab(String title, Component component) {
            	super.addTab( "", component );
                JLabel custom = new JLabel(title);
                setTabComponentAt( indexOfComponent( component ), custom );
            }
        };  
		
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		getContentPane().add(tabbedPane, gbc_tabbedPane);

		JPanel lowerPane = new JPanel();
		GridBagConstraints gbc_lowerPane = new GridBagConstraints();
		gbc_lowerPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_lowerPane.gridx = 0;
		gbc_lowerPane.gridy = 1;
		getContentPane().add(lowerPane, gbc_lowerPane);
		GridBagLayout gbl_lowerPane = new GridBagLayout();
		gbl_lowerPane.columnWidths = new int[] {100, 342, 100, 100, 100};
		gbl_lowerPane.rowHeights = new int[] {23};
		gbl_lowerPane.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0};
		gbl_lowerPane.rowWeights = new double[]{0.0};
		lowerPane.setLayout(gbl_lowerPane);

		loadConfigurationButton = new JButton("Load Configuration");
		loadConfigurationButton.addActionListener(this);
		
		Icon icon = new ImageIcon(FrameworkUtil.getBundle(ConfigurationWindow.class).getEntry("icons/logo.png"));
		logoLabel = new JLabel(icon);
		GridBagConstraints gbc_logoLabel = new GridBagConstraints();
		gbc_logoLabel.insets = new Insets(0, 0, 0, 5);
		gbc_logoLabel.gridx = 0;
		gbc_logoLabel.gridy = 0;
		lowerPane.add(logoLabel, gbc_logoLabel);

		JLabel emptyLabel = new JLabel("");
		GridBagConstraints gbc_emptyLabel = new GridBagConstraints();
		gbc_emptyLabel.fill = GridBagConstraints.BOTH;
		gbc_emptyLabel.insets = new Insets(0, 0, 0, 5);
		gbc_emptyLabel.gridx = 1;
		gbc_emptyLabel.gridy = 0;
		lowerPane.add(emptyLabel, gbc_emptyLabel);
		GridBagConstraints gbc_loadConfigurationButton = new GridBagConstraints();
		gbc_loadConfigurationButton.insets = new Insets(0, 0, 0, 5);
		gbc_loadConfigurationButton.gridx = 2;
		gbc_loadConfigurationButton.gridy = 0;
		lowerPane.add(loadConfigurationButton, gbc_loadConfigurationButton);

		saveConfigurationButton = new JButton("Save Configuration");
		saveConfigurationButton.addActionListener(this);
		GridBagConstraints gbc_saveConfigurationButton = new GridBagConstraints();
		gbc_saveConfigurationButton.insets = new Insets(0, 0, 0, 5);
		gbc_saveConfigurationButton.gridx = 3;
		gbc_saveConfigurationButton.gridy = 0;
		lowerPane.add(saveConfigurationButton, gbc_saveConfigurationButton);

		startButton = new JButton("Start");
		startButton.addActionListener(this);
		GridBagConstraints gbc_startButton = new GridBagConstraints();
		gbc_startButton.gridx = 4;
		gbc_startButton.gridy = 0;
		lowerPane.add(startButton, gbc_startButton);

		modelSelectionPane = new PalladioModelSpecificationPanel();
		modelSelectionPane.setPreferredSize(getContentPane().getPreferredSize());	
		tabbedPane.addTab(modelSelectionPane.getName(), modelSelectionPane);

		extensionSelectionPane = new ExtensionModelSpecificationPanel();
		extensionSelectionPane.setPreferredSize(getContentPane().getPreferredSize());
		tabbedPane.addTab(extensionSelectionPane.getName(), extensionSelectionPane);

		functionalityPane = new FunctionalityPanel();
		functionalityPane.setPreferredSize(getContentPane().getPreferredSize());
		tabbedPane.addTab(functionalityPane.getName(), functionalityPane);		

		optimizationConfigurationPane = new OptimizationConfigurationPanel();
		optimizationConfigurationPane.setPreferredSize(getContentPane().getPreferredSize());
		optimizationConfigurationPane.setEnabled(false);		
		tabbedPane.addTab(optimizationConfigurationPane.getName(), optimizationConfigurationPane);
		int tabNumber = tabbedPane.indexOfComponent(optimizationConfigurationPane);
		tabbedPane.setEnabledAt(tabNumber, false);

		robustnessConfigurationPane = new RobustnessConfigurationPanel();
		robustnessConfigurationPane.setPreferredSize(getContentPane().getPreferredSize());
		robustnessConfigurationPane.setEnabled(false);		
		tabbedPane.addTab(robustnessConfigurationPane.getName(), robustnessConfigurationPane);
		tabNumber = tabbedPane.indexOfComponent(robustnessConfigurationPane);
		tabbedPane.setEnabledAt(tabNumber, false);

		cloudBurstingPanel = new CloudBurstingPanel();
		cloudBurstingPanel.setPreferredSize(getContentPane().getPreferredSize());
		tabbedPane.addTab(cloudBurstingPanel.getName(), cloudBurstingPanel);
		tabNumber = tabbedPane.indexOfComponent(cloudBurstingPanel);
		tabbedPane.setEnabledAt(tabNumber, false);
		
		externalServerPanel = new ExternalServerPanel();
		externalServerPanel.setPreferredSize(getContentPane().getPreferredSize());
		tabbedPane.addTab(externalServerPanel.getName(), externalServerPanel);
		tabNumber = tabbedPane.indexOfComponent(externalServerPanel);
		tabbedPane.setEnabledAt(tabNumber, false);
		
		design2RuntimePanel = new Design2RuntimePanel();
		design2RuntimePanel.setPreferredSize(getContentPane().getPreferredSize());
		tabbedPane.addTab(design2RuntimePanel.getName(), design2RuntimePanel);
		tabNumber = tabbedPane.indexOfComponent(design2RuntimePanel);
		tabbedPane.setEnabledAt(tabNumber, true);
		
		optimizationConfigurationPane.addPropertyChangeListener(this);
		functionalityPane.addPropertyChangeListener(this);

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
			externalServerPanel.updateConfiguration();
			design2RuntimePanel.updateConfiguration();
			List<String> errors = Configuration.checkValidity();
			if(!errors.isEmpty()){
				String message="";
				for(String s:errors)
					message +=s+"\n";
				JOptionPane.showMessageDialog(this, message, "Configuration Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			//log the configuration
			Configuration.flushLog();
			if(Configuration.USE_PRIVATE_CLOUD)
				try {
					File tempFile = File.createTempFile("space4cloudBurst", ".properties");
					//					Configuration.PRIVATE_CLOUD_HOSTS_TMP = tempFile.getAbsolutePath();
					Configuration.PRIVATE_CLOUD_HOSTS = tempFile.getAbsolutePath();
					CloudBurstingPanel.saveFile(tempFile);
				} catch (IOException e1) {
					logger.error("Could not save the temporary file for the private cloud configuration");
				}
			cancelled = false;
			disposed = true;
			dispose();
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
					
					extensionSelectionPane.updateUsageModelValidation();
					extensionSelectionPane.updateResourceEnvironemntModelValidation();
					extensionSelectionPane.updateConstraintModelValidation();
					extensionSelectionPane.updateMceVisibility();
				} catch (IOException ex) {
					logger.error("Could not load the configuration from the file",e);
					 JOptionPane.showMessageDialog(null, "Could not load the configuration from the file", "Error", JOptionPane.ERROR_MESSAGE);
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
			
			externalServerPanel.updateConfiguration();
			design2RuntimePanel.updateConfiguration();

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
	
	private void setPanelVisibility(JPanel panel, boolean visibility) {
		panel.setEnabled(visibility);
		int tabNumber = tabbedPane.indexOfComponent(panel);
		tabbedPane.setEnabledAt(tabNumber, visibility);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		//when the user changes the selected functionality disable not relevant tabs
		//The if should not be necessary
		if(evt.getPropertyName().equals(FunctionalityPanel.functionalityProperty)){
			if (Configuration.FUNCTIONALITY.equals(Operation.Optimization)){
				setPanelVisibility(optimizationConfigurationPane, true);
				setPanelVisibility(robustnessConfigurationPane, false);
			} else if (Configuration.FUNCTIONALITY.equals(Operation.Robustness)){
				setPanelVisibility(optimizationConfigurationPane, true);
				setPanelVisibility(robustnessConfigurationPane, true);
			} else{
				setPanelVisibility(optimizationConfigurationPane, false);
				setPanelVisibility(robustnessConfigurationPane, false);
			}
			extensionSelectionPane.updateMceVisibility();
		} else if(evt.getPropertyName().equals(FunctionalityPanel.privateCloudProperty)) {
			boolean visibility = Configuration.USE_PRIVATE_CLOUD;
			setPanelVisibility(cloudBurstingPanel, visibility);
			
			visibility = optimizationConfigurationPane.sshNeeded();
			setPanelVisibility(externalServerPanel, visibility);
		} else if(evt.getPropertyName().equals(OptimizationConfigurationPanel.updateSSH)) {
			boolean visibility = (Boolean)evt.getNewValue();
			setPanelVisibility(externalServerPanel, visibility);
		} else if(evt.getPropertyName().equals(FunctionalityPanel.contractorProperty)) {
			boolean visibility = optimizationConfigurationPane.sshNeeded();
			setPanelVisibility(externalServerPanel, visibility);
		} else if(evt.getPropertyName().equals(FunctionalityPanel.design2runtimeProperty)) {
			boolean visibility = Configuration.GENERATE_DESIGN_TO_RUNTIME_FILES;
			setPanelVisibility(design2RuntimePanel, visibility);
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
//		super.windowClosing(e);
		dispose();
		disposed = true;
	}

	public void loadConfiguration() {
		modelSelectionPane.loadConfiguration();
		extensionSelectionPane.loadConfiguration();	
		functionalityPane.loadConfiguration();
		optimizationConfigurationPane.loadConfiguration();
		robustnessConfigurationPane.loadConfiguration();
		cloudBurstingPanel.loadConfiguration();
		externalServerPanel.loadConfiguration();
		design2RuntimePanel.loadConfiguration();
		
		int index = tabbedPane.getSelectedIndex(), origIndex = index;
		boolean found = false;
		while (!found && index >= 0) {
			if (tabbedPane.isEnabledAt(index)) {
				found = true;
				tabbedPane.setSelectedIndex(index);
			}
			index--;
		}
		for (index = origIndex+1; index < tabbedPane.getTabCount() && !found; ++index) {
			if (tabbedPane.isEnabledAt(index)) {
				found = true;
				tabbedPane.setSelectedIndex(index);
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		int index = tabbedPane.getSelectedIndex();
		String tabTitle = tabbedPane.getTitleAt(index);

		if (tabTitle.equals(optimizationConfigurationPane.getName())) {
//			boolean visibility = optimizationConfigurationPane.sshNeeded();
//			setPanelVisibility(externalServerPanel, visibility);
		}
			
		else if (tabTitle.equals(extensionSelectionPane.getName()))
			extensionSelectionPane.updateMceVisibility();

	}

	@Override
	public void windowOpened(WindowEvent e) { }

	@Override
	public void windowClosed(WindowEvent e) { }

	@Override
	public void windowIconified(WindowEvent e) { }

	@Override
	public void windowDeiconified(WindowEvent e) { }

	@Override
	public void windowActivated(WindowEvent e) { }

	@Override
	public void windowDeactivated(WindowEvent e) { }

}


