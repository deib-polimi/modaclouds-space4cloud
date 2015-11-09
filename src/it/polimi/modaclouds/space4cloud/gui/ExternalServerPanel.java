package it.polimi.modaclouds.space4cloud.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import it.polimi.modaclouds.space4cloud.utils.Configuration;

public class ExternalServerPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2377838960188179616L;
	private static final String PANEL_NAME = "Solver Server"; //"External Server";
	private JTextField sshHost, sshUser, sshPassword;
	
	private JRadioButton local, external;
	
	public ExternalServerPanel() {
		setName(PANEL_NAME);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{201, 201, 0};
		gridBagLayout.rowHeights = new int[]{35, 35, 35, 35, 35, 35, 35, 35, 35, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridBagLayout);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 0, 5, 5);
		add(new JLabel("Run math solver on"), c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		JPanel radioPan = new JPanel();
		ButtonGroup group = new ButtonGroup();
		local = new JRadioButton("this machine");
		radioPan.add(local);
		group.add(local);
		local.addActionListener(this);
		external = new JRadioButton("external server");
		radioPan.add(external);
		group.add(external);
		external.addActionListener(this);
		add(radioPan, c);
		
		c.gridx = 0;
		c.gridy++;
        c.insets = new Insets(0, 0, 5, 5);
		add(new JLabel("Host"), c);
		sshHost = new JTextField(10);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		add(sshHost, c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);
		add(new JLabel("User name"), c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		sshUser = new JTextField(10);
		add(sshUser, c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);
		add(new JLabel("Password"), c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		sshPassword = new JTextField(10);
		add(sshPassword, c);

	}
	
	/**
	 * Updates the values shown to the user according to those stored in the Configuration class
	 */
	public void loadConfiguration() {
		sshHost.setText(Configuration.SSH_HOST);
		if (Configuration.SSH_HOST.equals("localhost"))
			local.setSelected(true);
		else
			external.setSelected(true);
		sshUser.setText(Configuration.SSH_USER_NAME);
		sshPassword.setText(Configuration.SSH_PASSWORD);
		updateVisibility();
	}
	
	/**
	 * Updates values in the Configuration class according to those selected in the panel
	 */
	public void updateConfiguration() {
		Configuration.SSH_HOST = sshHost.getText();
		Configuration.SSH_USER_NAME = sshUser.getText();
		Configuration.SSH_PASSWORD = sshPassword.getText();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		//change also the state of all internal components
		for (Component comp:getComponents()) {
			comp.setEnabled(enabled);
		}
	}
	
	private void updateVisibility() {
		boolean visibility = !Configuration.SSH_HOST.equals("localhost");
		sshHost.setEnabled(visibility);
		sshUser.setEnabled(visibility);
		sshPassword.setEnabled(visibility);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(local))
			Configuration.SSH_HOST = "localhost";
		else if (e.getSource().equals(external) && Configuration.SSH_HOST.equals("localhost"))
			Configuration.SSH_HOST = "";
		sshHost.setText(Configuration.SSH_HOST);
		updateVisibility();
	}
	
}
