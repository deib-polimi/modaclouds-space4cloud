package it.polimi.modaclouds.space4cloud.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.polimi.modaclouds.qos_models.schema.Constraints;
import it.polimi.modaclouds.qos_models.schema.MultiCloudExtensions;
import it.polimi.modaclouds.qos_models.schema.ResourceModelExtension;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtensions;
import it.polimi.modaclouds.qos_models.util.ValidationResult;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.utils.Configuration;

public class ExtensionModelSpecificationPanel extends JPanel implements
		ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -804310895749263278L;

	private static final String PANEL_NAME = "Extensions"; // Model";
	private JTextField usageExtensionTextField;
	private JTextField resourceEnvironmentTextField;
	private JTextField constraintTextField;
	private JTextField mceTextField;
	private JButton usageExtensionButton;
	private JButton resourceEnvironmentButton;
	private JButton constraintButton;
	private JButton mceButton;

	private JLabel mceLabel;
	private JPanel usageExtenstionLabelPanel;
	private JLabel usageExtensionNotificationLabel;
	private JPanel resourceEnvironmentLabelPanel;
	private JLabel resourceEnvironmentNotificationLabel;
	private JPanel constraintLabelPanel;
	private JLabel constraintNotificationLabel;
	private JPanel multicloudExtensionLabelPanel;
	private JLabel mceNotificationLabel;

	/**
	 * Create the panel.
	 */
	public ExtensionModelSpecificationPanel() {
		setName(PANEL_NAME);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 350, 50, 0 };
		gridBagLayout.rowHeights = new int[] { 30, 30, 30, 30, 30, 30, 30, 30,
				0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 1.0 };
		setLayout(gridBagLayout);

		usageExtenstionLabelPanel = new JPanel();
		GridBagConstraints gbc_usageExtenstionLabelPanel = new GridBagConstraints();
		gbc_usageExtenstionLabelPanel.insets = new Insets(0, 0, 5, 5);
		gbc_usageExtenstionLabelPanel.fill = GridBagConstraints.BOTH;
		gbc_usageExtenstionLabelPanel.gridx = 0;
		gbc_usageExtenstionLabelPanel.gridy = 0;
		add(usageExtenstionLabelPanel, gbc_usageExtenstionLabelPanel);
		usageExtenstionLabelPanel.setLayout(new GridLayout(1, 2, 0, 0));

		JLabel usageExtensionLabel = new JLabel("Usage Model Extension");
		usageExtenstionLabelPanel.add(usageExtensionLabel);

		usageExtensionNotificationLabel = new JLabel();
		usageExtenstionLabelPanel.add(usageExtensionNotificationLabel);
		usageExtensionNotificationLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				displayValidationDetail(usageExtensionTextField.getText(),
						UsageModelExtensions.class);
				updateUsageModelValidation();
			}
		});
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

		resourceEnvironmentLabelPanel = new JPanel();
		GridBagConstraints gbc_resourceEnvironmentLabelPanel = new GridBagConstraints();
		gbc_resourceEnvironmentLabelPanel.insets = new Insets(0, 0, 5, 5);
		gbc_resourceEnvironmentLabelPanel.fill = GridBagConstraints.BOTH;
		gbc_resourceEnvironmentLabelPanel.gridx = 0;
		gbc_resourceEnvironmentLabelPanel.gridy = 2;
		add(resourceEnvironmentLabelPanel, gbc_resourceEnvironmentLabelPanel);
		resourceEnvironmentLabelPanel.setLayout(new GridLayout(1, 2, 0, 0));

		JLabel resourceEnvironmentLabel = new JLabel(
				"Resource Environment Extension");
		resourceEnvironmentLabelPanel.add(resourceEnvironmentLabel);

		resourceEnvironmentNotificationLabel = new JLabel();
		resourceEnvironmentLabelPanel.add(resourceEnvironmentNotificationLabel);
		resourceEnvironmentNotificationLabel
				.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						displayValidationDetail(
								resourceEnvironmentTextField.getText(),
								ResourceModelExtension.class);
						updateResourceEnvironemntModelValidation();
					}
				});
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

		constraintLabelPanel = new JPanel();
		GridBagConstraints gbc_constraintLabelPanel = new GridBagConstraints();
		gbc_constraintLabelPanel.insets = new Insets(0, 0, 5, 5);
		gbc_constraintLabelPanel.fill = GridBagConstraints.BOTH;
		gbc_constraintLabelPanel.gridx = 0;
		gbc_constraintLabelPanel.gridy = 4;
		add(constraintLabelPanel, gbc_constraintLabelPanel);
		constraintLabelPanel.setLayout(new GridLayout(0, 2, 0, 0));

		JLabel constraintLabel = new JLabel("Constraints");
		constraintLabelPanel.add(constraintLabel);

		constraintNotificationLabel = new JLabel();
		constraintLabelPanel.add(constraintNotificationLabel);
		constraintNotificationLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				displayValidationDetail(constraintTextField.getText(),
						Constraints.class);
				updateConstraintModelValidation();
			}
		});
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

		multicloudExtensionLabelPanel = new JPanel();
		GridBagConstraints gbc_multicloudExtensionLabelPanel = new GridBagConstraints();
		gbc_multicloudExtensionLabelPanel.insets = new Insets(0, 0, 5, 5);
		gbc_multicloudExtensionLabelPanel.fill = GridBagConstraints.BOTH;
		gbc_multicloudExtensionLabelPanel.gridx = 0;
		gbc_multicloudExtensionLabelPanel.gridy = 6;
		add(multicloudExtensionLabelPanel, gbc_multicloudExtensionLabelPanel);
		multicloudExtensionLabelPanel.setLayout(new GridLayout(0, 2, 0, 0));

		mceLabel = new JLabel(
				"Multi Cloud Extension (used only for the assessment)");
		multicloudExtensionLabelPanel.add(mceLabel);

		mceNotificationLabel = new JLabel();
		multicloudExtensionLabelPanel.add(mceNotificationLabel);
		mceNotificationLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				displayValidationDetail(mceTextField.getText(),
						MultiCloudExtensions.class);
				updateMultiCloudModelValidation();
			}
		});
		JLabel emptyLabel3 = new JLabel("");
		GridBagConstraints gbc_emptyLabel3 = new GridBagConstraints();
		gbc_emptyLabel3.fill = GridBagConstraints.BOTH;
		gbc_emptyLabel3.insets = new Insets(0, 0, 5, 0);
		gbc_emptyLabel3.gridx = 1;
		gbc_emptyLabel3.gridy = 6;
		add(emptyLabel3, gbc_emptyLabel3);

		mceTextField = new JTextField();
		GridBagConstraints gbc_mceTextField = new GridBagConstraints();
		gbc_mceTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mceTextField.insets = new Insets(0, 0, 5, 5);
		gbc_mceTextField.gridx = 0;
		gbc_mceTextField.gridy = 7;
		add(mceTextField, gbc_mceTextField);
		mceTextField.setColumns(10);

		mceButton = new JButton("Load Model");
		mceButton.addActionListener(this);
		GridBagConstraints gbc_mceButton = new GridBagConstraints();
		gbc_mceButton.insets = new Insets(0, 0, 5, 0);
		gbc_mceButton.gridx = 1;
		gbc_mceButton.gridy = 7;
		add(mceButton, gbc_mceButton);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 8;
		add(panel, gbc_panel);
		
			
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		File loadedFile = null;
		// TODO: pass the loaded file to the configuration class
		if (e.getSource().equals(usageExtensionButton)) {
			loadedFile = FileLoader.loadExtensionFile("Load Usage Extension",
					UsageModelExtensions.class);
			if (loadedFile != null) {
				usageExtensionTextField.setText(loadedFile.getAbsolutePath());
				Configuration.USAGE_MODEL_EXTENSION = loadedFile
						.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER = loadedFile.getParent()
						.toString();
				updateUsageModelValidation();
			}
		} else if (e.getSource().equals(resourceEnvironmentButton)) {
			loadedFile = FileLoader.loadExtensionFile(
					"Load Resource Environment Extension",
					ResourceModelExtension.class);
			if (loadedFile != null) {
				resourceEnvironmentTextField.setText(loadedFile
						.getAbsolutePath());
				Configuration.RESOURCE_ENVIRONMENT_EXTENSION = loadedFile
						.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER = loadedFile.getParent()
						.toString();
				updateResourceEnvironemntModelValidation();
			}
		} else if (e.getSource().equals(constraintButton)) {
			loadedFile = FileLoader.loadExtensionFile("Load Constraints",
					Constraints.class);
			if (loadedFile != null) {
				constraintTextField.setText(loadedFile.getAbsolutePath());
				Configuration.CONSTRAINTS = loadedFile.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER = loadedFile.getParent()
						.toString();
				updateConstraintModelValidation();
			}
		} else if (e.getSource().equals(mceButton)) {
			loadedFile = FileLoader.loadExtensionFile(
					"Load Multi Cloud Extension", MultiCloudExtensions.class);
			if (loadedFile != null) {
				mceTextField.setText(loadedFile.getAbsolutePath());
				Configuration.MULTI_CLOUD_EXTENSION = loadedFile
						.getAbsolutePath();
				Configuration.PROJECT_BASE_FOLDER = loadedFile.getParent()
						.toString();
				updateMultiCloudModelValidation();
			}
		}
	}

	/**
	 * Updates the values shown to the user according to those stored in the
	 * Configuration class
	 */
	public void loadConfiguration() {
		usageExtensionTextField.setText(Configuration.USAGE_MODEL_EXTENSION);
		resourceEnvironmentTextField
				.setText(Configuration.RESOURCE_ENVIRONMENT_EXTENSION);
		constraintTextField.setText(Configuration.CONSTRAINTS);
		mceTextField.setText(Configuration.MULTI_CLOUD_EXTENSION);
		updateMceVisibility();
	}

	/**
	 * Updates values in the Configuration class according to those selected in
	 * the panel
	 */
	public void updateConfiguration() {
		Configuration.USAGE_MODEL_EXTENSION = usageExtensionTextField.getText();
		Configuration.RESOURCE_ENVIRONMENT_EXTENSION = resourceEnvironmentTextField
				.getText();
		Configuration.CONSTRAINTS = constraintTextField.getText();
		Configuration.MULTI_CLOUD_EXTENSION = mceTextField.getText();
	}

	/**
	 * Updates the visibility of the multicloud extension model
	 */
	public void updateMceVisibility() {
		boolean enabled = Configuration.FUNCTIONALITY == Configuration.Operation.Assessment;
		mceTextField.setEnabled(enabled);
		mceButton.setEnabled(enabled);
		mceLabel.setEnabled(enabled);
	}

	/**
	 * Validates the usage model extension and update the status of the
	 * validation notification icon
	 */
	public void updateUsageModelValidation() {
		updateValidation(usageExtensionTextField, usageExtensionNotificationLabel, UsageModelExtensions.class);
		
//		String usageExtensionFile = usageExtensionTextField.getText();
//		String usageExtensionNotificationText = "valid";
//		String usageExtensionNotificationColor = "green";
//		if (usageExtensionFile == null || usageExtensionFile.isEmpty()) {
//			usageExtensionNotificationText = "No input file";
//			usageExtensionNotificationColor = "red";
//			return;
//		}
//		try {
//			ValidationResult result = XMLHelper.validate(
//					Paths.get(usageExtensionFile).toUri().toURL(),
//					UsageModelExtensions.class);
//			if (!result.isValid()) {
//				usageExtensionNotificationText = "Not Valid";
//				usageExtensionNotificationColor = "red";
//			}
//		} catch (MalformedURLException e) {
//			usageExtensionNotificationText = "No input file";
//			usageExtensionNotificationColor = "red";
//		} finally {
//			usageExtensionNotificationLabel.setText("<html><font color='"
//					+ usageExtensionNotificationColor + "'>"
//					+ usageExtensionNotificationText + "</font></html>");
//		}
	}

	/**
	 * Validates the resource environment extension model and update the status
	 * of the validation notification icon
	 */
	public void updateResourceEnvironemntModelValidation() {
		updateValidation(resourceEnvironmentTextField, resourceEnvironmentNotificationLabel, ResourceModelExtension.class);
		
//		String environmentExtensionFile = resourceEnvironmentTextField
//				.getText();
//		String environmentExtensionNotificationText = "valid";
//		String environmentExtensionNotificationColor = "green";
//		if (environmentExtensionFile == null
//				|| environmentExtensionFile.isEmpty()) {
//			environmentExtensionNotificationText = "No input file";
//			environmentExtensionNotificationColor = "red";
//			return;
//		}
//		try {
//			ValidationResult result = XMLHelper.validate(
//					Paths.get(environmentExtensionFile).toUri().toURL(),
//					ResourceModelExtension.class);
//			if (!result.isValid()) {
//				environmentExtensionNotificationText = "Not Valid";
//				environmentExtensionNotificationColor = "red";
//			}
//		} catch (MalformedURLException e) {
//			environmentExtensionNotificationText = "No input file";
//			environmentExtensionNotificationColor = "red";
//		} finally {
//			resourceEnvironmentNotificationLabel.setText("<html><font color='"
//					+ environmentExtensionNotificationColor + "'>"
//					+ environmentExtensionNotificationText + "</font></html>");
//		}
	}

	/**
	 * Validates the constraint extension model and update the status of the
	 * validation notification icon
	 */
	public void updateConstraintModelValidation() {
		updateValidation(constraintTextField, constraintNotificationLabel, Constraints.class);
		
//		String constraintExtensionFile = constraintTextField.getText();
//		String constraintExtensionNotificationText = "valid";
//		String constraintExtensionNotificationColor = "green";
//		if (constraintExtensionFile == null
//				|| constraintExtensionFile.isEmpty()) {
//			constraintExtensionNotificationText = "No input file";
//			constraintExtensionNotificationColor = "red";
//			return;
//		}
//		try {
//			ValidationResult result = XMLHelper.validate(
//					Paths.get(constraintExtensionFile).toUri().toURL(),
//					Constraints.class);
//			if (!result.isValid()) {
//				constraintExtensionNotificationText = "Not Valid";
//				constraintExtensionNotificationColor = "red";
//			}
//		} catch (MalformedURLException e) {
//			constraintExtensionNotificationText = "No input file";
//			constraintExtensionNotificationColor = "red";
//		} finally {
//			constraintNotificationLabel.setText("<html><font color='"
//					+ constraintExtensionNotificationColor + "'>"
//					+ constraintExtensionNotificationText + "</font></html>");
//		}
	}
	
	private <T> void updateValidation(JTextField fieldParam, JLabel labelParam, Class<T> clazzParam) {
		final JTextField field = fieldParam;
		final JLabel label = labelParam;
		final Class<T> clazz = clazzParam;
		
		new Thread() {
			public void run() {
				String file = field.getText();
				String notificationText = "Valid";
				String notificationColor = "green";
				if (file == null
						|| file.isEmpty()) {
					notificationText = "No Input File";
					notificationColor = "red";
					return;
				}
				try {
					ValidationResult result = XMLHelper.validate(
							Paths.get(file).toUri().toURL(),
							clazz);
					if (!result.isValid()) {
						notificationText = "Not Valid";
						notificationColor = "red";
					}
				} catch (MalformedURLException e) {
					notificationText = "No Input File";
					notificationColor = "red";
				} finally {
					label.setText("<html><font color='"
							+ notificationColor + "'>"
							+ notificationText + "</font></html>");
				}
			}
		}.start();
	}

	/**
	 * Validates the multicloud extension model and update the status of the
	 * validation notification icon
	 */
	public void updateMultiCloudModelValidation() {
		updateValidation(mceTextField, mceNotificationLabel, MultiCloudExtensions.class);
		
//		String multiCloudExtensionFile = mceTextField.getText();
//		String multiCloudExtensionNotificationText = "valid";
//		String multiCloudExtensionNotificationColor = "green";
//		if (multiCloudExtensionFile == null
//				|| multiCloudExtensionFile.isEmpty()) {
//			multiCloudExtensionNotificationText = "No input file";
//			multiCloudExtensionNotificationColor = "red";
//			return;
//		}
//		try {
//			ValidationResult result = XMLHelper.validate(
//					Paths.get(multiCloudExtensionFile).toUri().toURL(),
//					MultiCloudExtensions.class);
//			if (!result.isValid()) {
//				multiCloudExtensionNotificationText = "Not Valid";
//				multiCloudExtensionNotificationColor = "red";
//			}
//		} catch (MalformedURLException e) {
//			multiCloudExtensionNotificationText = "No input file";
//			multiCloudExtensionNotificationColor = "red";
//		} finally {
//			mceNotificationLabel.setText("<html><font color='"
//					+ multiCloudExtensionNotificationColor + "'>"
//					+ multiCloudExtensionNotificationText + "</font></html>");
//		}
	}

	/**
	 * Provides the user information about the validation of input file
	 * 
	 * @param filePath
	 *            path to the provided file
	 * @param clazz
	 *            the class to which the XML parsing should comply with
	 */
	private <T> void displayValidationDetail(String filePath, Class<T> clazz) {
		List<String> messages = new ArrayList<String>();
		try {
			ValidationResult result;
			if (filePath == null || filePath.isEmpty())
				messages.add("No file Selected");
			else if (!Paths.get(filePath).toFile().exists())
				messages.add("The specified file does not exist");
			else {
				result = XMLHelper.validate(
						Paths.get(filePath).toUri().toURL(), clazz);
				if (!result.isValid()) {
					messages.addAll(result.getMessages());
				}else{
					messages.add("The provided file is valid");
				}
			}
		} catch (MalformedURLException e) {
			messages.add("An error occurred while reading input file.");
			messages.add(e.getLocalizedMessage());
		} finally {
			JOptionPane.showMessageDialog(null, messages, "Validation Details",
					JOptionPane.PLAIN_MESSAGE);
		}
	}
	
}
