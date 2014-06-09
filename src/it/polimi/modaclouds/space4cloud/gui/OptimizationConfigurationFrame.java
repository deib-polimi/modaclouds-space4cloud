package it.polimi.modaclouds.space4cloud.gui;

import it.polimi.modaclouds.space4cloud.optimization.SelectionPolicies;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class OptimizationConfigurationFrame extends JFrame implements
		ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -179380632887639091L;
	private static final int DEFAULT_MEMORY_SIZE = 10;
	private static final int DEFAULT_MAX_ITERATIONS = 10;
	private static final int DEFAULT_MAX_FEASIBILITY = 10;
	private static final String DEFAULT_SELECTION_POLICY = "utilization";
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					OptimizationConfigurationFrame frame = new OptimizationConfigurationFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	private JPanel contentPane;
	private JLabel lblNotification;
	private JTextField txtConfFile;
	private JTextField txtMaxIteration;
	private JTextField txtMaxFeasIter;
	private JComboBox<SelectionPolicies> policyBox;
	private String preferenceFile;
	private int maxMemorySize;
	private int maxIterations;
	private int maxFeasIter;
	private SelectionPolicies policy;
	private JTextField txtMaxMemorySize;

	private boolean saved = false;

	/**
	 * Create the frame.
	 */
	public OptimizationConfigurationFrame() {
		setResizable(false);
		setTitle("Optimization Configuration Parameters");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 668, 248);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 141, 400, 100 };
		gbl_contentPane.rowHeights = new int[] { 30, 30, 30, 30, 30, 30 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0 };
		contentPane.setLayout(gbl_contentPane);

		JLabel lblConfFile = new JLabel("Configuration File");
		lblConfFile.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblConfFile = new GridBagConstraints();
		gbc_lblConfFile.fill = GridBagConstraints.BOTH;
		gbc_lblConfFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblConfFile.gridx = 0;
		gbc_lblConfFile.gridy = 0;
		contentPane.add(lblConfFile, gbc_lblConfFile);

		txtConfFile = new JTextField();
		GridBagConstraints gbc_txtConfFile = new GridBagConstraints();
		gbc_txtConfFile.fill = GridBagConstraints.BOTH;
		gbc_txtConfFile.insets = new Insets(0, 0, 5, 5);
		gbc_txtConfFile.gridx = 1;
		gbc_txtConfFile.gridy = 0;
		contentPane.add(txtConfFile, gbc_txtConfFile);
		txtConfFile.setColumns(10);

		JButton btnConfFile = new JButton("Load");
		GridBagConstraints gbc_btnConfFile = new GridBagConstraints();
		gbc_btnConfFile.anchor = GridBagConstraints.WEST;
		gbc_btnConfFile.insets = new Insets(0, 0, 5, 0);
		gbc_btnConfFile.gridx = 2;
		gbc_btnConfFile.gridy = 0;
		contentPane.add(btnConfFile, gbc_btnConfFile);
		btnConfFile.addActionListener(this);

		JLabel lblMaxMemorySize = new JLabel("Max Memory Size");
		GridBagConstraints gbc_lblMaxMemorySize = new GridBagConstraints();
		gbc_lblMaxMemorySize.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxMemorySize.gridx = 0;
		gbc_lblMaxMemorySize.gridy = 1;
		contentPane.add(lblMaxMemorySize, gbc_lblMaxMemorySize);

		txtMaxMemorySize = new JTextField();
		GridBagConstraints gbc_txtMaxMemorySize = new GridBagConstraints();
		gbc_txtMaxMemorySize.anchor = GridBagConstraints.WEST;
		gbc_txtMaxMemorySize.insets = new Insets(0, 0, 5, 5);
		gbc_txtMaxMemorySize.gridx = 1;
		gbc_txtMaxMemorySize.gridy = 1;
		contentPane.add(txtMaxMemorySize, gbc_txtMaxMemorySize);
		txtMaxMemorySize.setColumns(10);

		JLabel lblMaxIteration = new JLabel("Max Iterations");
		lblMaxIteration.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblMaxIteration = new GridBagConstraints();
		gbc_lblMaxIteration.fill = GridBagConstraints.BOTH;
		gbc_lblMaxIteration.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxIteration.gridx = 0;
		gbc_lblMaxIteration.gridy = 2;
		contentPane.add(lblMaxIteration, gbc_lblMaxIteration);

		txtMaxIteration = new JTextField();
		txtMaxIteration.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_txtMaxIteration = new GridBagConstraints();
		gbc_txtMaxIteration.anchor = GridBagConstraints.WEST;
		gbc_txtMaxIteration.insets = new Insets(0, 0, 5, 5);
		gbc_txtMaxIteration.gridx = 1;
		gbc_txtMaxIteration.gridy = 2;
		contentPane.add(txtMaxIteration, gbc_txtMaxIteration);
		txtMaxIteration.setColumns(10);

		JLabel lblMaxFeasIter = new JLabel("Max Feasibility Iterations");
		GridBagConstraints gbc_lblMaxFeasIter = new GridBagConstraints();
		gbc_lblMaxFeasIter.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxFeasIter.gridx = 0;
		gbc_lblMaxFeasIter.gridy = 3;
		contentPane.add(lblMaxFeasIter, gbc_lblMaxFeasIter);
		lblMaxFeasIter.setHorizontalAlignment(SwingConstants.CENTER);

		txtMaxFeasIter = new JTextField();
		txtMaxFeasIter.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_txtMaxFeasIter = new GridBagConstraints();
		gbc_txtMaxFeasIter.anchor = GridBagConstraints.WEST;
		gbc_txtMaxFeasIter.insets = new Insets(0, 0, 5, 5);
		gbc_txtMaxFeasIter.gridx = 1;
		gbc_txtMaxFeasIter.gridy = 3;
		contentPane.add(txtMaxFeasIter, gbc_txtMaxFeasIter);
		txtMaxFeasIter.setColumns(10);

		JLabel lblPolicy = new JLabel("Selection Policy");
		lblPolicy.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblPolicy = new GridBagConstraints();
		gbc_lblPolicy.fill = GridBagConstraints.BOTH;
		gbc_lblPolicy.insets = new Insets(0, 0, 5, 5);
		gbc_lblPolicy.gridx = 0;
		gbc_lblPolicy.gridy = 4;
		contentPane.add(lblPolicy, gbc_lblPolicy);

		policyBox = new JComboBox<SelectionPolicies>();
		policyBox.setModel(new DefaultComboBoxModel<SelectionPolicies>(
				SelectionPolicies.values()));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.anchor = GridBagConstraints.WEST;
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 4;
		contentPane.add(policyBox, gbc_comboBox);

		lblNotification = new JLabel("");
		GridBagConstraints gbc_lblNotification = new GridBagConstraints();
		gbc_lblNotification.insets = new Insets(0, 0, 0, 5);
		gbc_lblNotification.gridx = 1;
		gbc_lblNotification.gridy = 5;
		contentPane.add(lblNotification, gbc_lblNotification);

		JButton btnSave = new JButton("Save");
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.anchor = GridBagConstraints.WEST;
		gbc_btnSave.gridx = 2;
		gbc_btnSave.gridy = 5;
		contentPane.add(btnSave, gbc_btnSave);
		btnSave.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {

		JButton button = (JButton) e.getSource();
		// if the load button is pressed
		if (button.getText().equals("Load")) {
			final JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(button.getParent());
			fc.setDialogTitle("Optimization PArameter Configuration File");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				setPreferenceFile(fc.getSelectedFile().getAbsolutePath());
			}
		} else if (button.getText().equals("Save")) {
			maxFeasIter = Integer.parseInt(txtMaxFeasIter.getText());
			maxIterations = Integer.parseInt(txtMaxIteration.getText());
			maxMemorySize = Integer.parseInt(txtMaxMemorySize.getText());
			policy = (SelectionPolicies) policyBox.getSelectedItem();
			this.setVisible(false);
			setSaved(true);
		}

	}

	public int getMaxFeasIter() {
		return maxFeasIter;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public int getMaxMemorySize() {
		return maxMemorySize;
	}

	public SelectionPolicies getPolicy() {
		return policy;
	}

	public String getPreferenceFile() {
		return preferenceFile;
	}

	public synchronized boolean isSaved() {
		return saved;
	}

	private void notifyFileError(String preferenceFile2) {
		lblNotification
				.setText("<html><font color='red'>Error in loading the configuration from file</font></html>");
	}

	/**
	 * Initialization of the OptEngine by means of a properties File
	 * 
	 * @param propertiesFileName
	 * @throws IOException
	 */
	private void parseFile(String propertiesFileName) throws IOException {

		InputStream fileInput = null;
		// load it from the plugin
		fileInput = this.getClass().getResourceAsStream(propertiesFileName);
		// load it from the path
		if (fileInput == null)
			fileInput = new FileInputStream(propertiesFileName);

		Properties properties = new Properties();
		properties.load(fileInput);
		fileInput.close();
		setMaxMemorySize(Integer.parseInt(properties
				.getProperty("MAXMEMORYSIZE")));
		setMaxIterations(Integer.parseInt(properties
				.getProperty("MAXITERATIONS")));
		setMaxFeasIter(Integer.parseInt(properties
				.getProperty("MAXFEASIBILITYITERATIONS")));
		setPolicy(SelectionPolicies.getPropertyFromName(properties
				.getProperty("SELECTION_POLICY")));

	}

	private void setMaxFeasIter(int maxFeasIter) {
		this.maxFeasIter = maxFeasIter;
		txtMaxFeasIter.setText("" + this.maxFeasIter);
	}

	private void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
		txtMaxIteration.setText("" + this.maxIterations);
	}

	private void setMaxMemorySize(int maxMemorySize) {
		this.maxMemorySize = maxMemorySize;
		txtMaxMemorySize.setText("" + this.maxMemorySize);
	}

	private void setPolicy(SelectionPolicies policy) {
		this.policy = policy;
		policyBox.setSelectedItem(this.policy);
	}

	public void setPreferenceFile(String preferenceFile) {
		this.preferenceFile = preferenceFile;
		txtConfFile.setText(preferenceFile);
		try {
			parseFile(preferenceFile);
		} catch (IOException e) {
			setMaxMemorySize(DEFAULT_MEMORY_SIZE);
			setMaxIterations(DEFAULT_MAX_ITERATIONS);
			setMaxFeasIter(DEFAULT_MAX_FEASIBILITY);
			setPolicy(SelectionPolicies
					.getPropertyFromName(DEFAULT_SELECTION_POLICY));
			notifyFileError(preferenceFile);
		}
	}

	private synchronized void setSaved(boolean value) {
		saved = value;
	}

}
