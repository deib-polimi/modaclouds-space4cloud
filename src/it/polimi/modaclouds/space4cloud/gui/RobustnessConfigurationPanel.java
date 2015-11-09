package it.polimi.modaclouds.space4cloud.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import it.polimi.modaclouds.space4cloud.utils.Configuration;

public class RobustnessConfigurationPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2986951359583819299L;
	private static final String PANEL_NAME = "Robustness"; // Configuration";
	private JTextField peakFrom, peakTo, stepSize, attempts, variability, q, g, h;
	
	public RobustnessConfigurationPanel() {
		setName(PANEL_NAME);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{201, 201, 0};
		gridBagLayout.rowHeights = new int[]{35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridBagLayout);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridx = 0;
		c.gridy = 0;
        c.insets = new Insets(0, 0, 5, 5);
		add(new JLabel("Peak from"), c);
		peakFrom = new JTextField(10);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		add(peakFrom, c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);
		add(new JLabel("Peak to"), c);
		c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 0, 5, 0);
		peakTo = new JTextField(10);
		add(peakTo, c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);
		add(new JLabel("Step size"), c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		stepSize = new JTextField(10);
		add(stepSize, c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);
		
		add(new JLabel("Variability (%)"), c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		variability = new JTextField(10);
		add(variability, c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);

		add(new JLabel("Attempts per test"), c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		attempts = new JTextField(10);
		add(attempts, c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);
		add(new JLabel("Days considered"), c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		h = new JTextField(10);
		add(h, c);

		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);
		add(new JLabel("Percentage of spot instances"), c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		q = new JTextField(10);
		add(q, c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);
		add(new JLabel("G"), c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		g = new JTextField(10);
		add(g, c);

	}
	
	/**
	 * Updates the values shown to the user according to those stored in the Configuration class
	 */
	public void loadConfiguration() {
		peakFrom.setText(Integer.toString(Configuration.ROBUSTNESS_PEAK_FROM));
		peakTo.setText(Integer.toString(Configuration.ROBUSTNESS_PEAK_TO));
		stepSize.setText(Integer.toString(Configuration.ROBUSTNESS_STEP_SIZE));
		attempts.setText(Integer.toString(Configuration.ROBUSTNESS_ATTEMPTS));

		String tmp = "";
		for (int i : Configuration.ROBUSTNESS_VARIABILITIES)
			tmp += i + ";";
		variability.setText(tmp.substring(0, tmp.length() - 1));
		
		q.setText(Double.toString(Configuration.ROBUSTNESS_Q));
		
		tmp = "";
		for (int i : Configuration.ROBUSTNESS_GS)
			tmp += i + ";";
		g.setText(tmp.substring(0, tmp.length() - 1));
		h.setText(Integer.toString(Configuration.ROBUSTNESS_H));

	}
	
	/**
	 * Updates values in the Configuration class according to those selected in the panel
	 */
	public void updateConfiguration() {
		try{
			Configuration.ROBUSTNESS_PEAK_FROM = Integer.parseInt(peakFrom.getText());
		} catch (NumberFormatException e){
			Configuration.ROBUSTNESS_PEAK_FROM = -1;
		}
		try{
			Configuration.ROBUSTNESS_PEAK_TO = Integer.parseInt(peakTo.getText());
		} catch (NumberFormatException e){
			Configuration.ROBUSTNESS_PEAK_TO = -1;
		}
		try{
			Configuration.ROBUSTNESS_STEP_SIZE = Integer.parseInt(stepSize.getText());
		} catch (NumberFormatException e){
			Configuration.ROBUSTNESS_STEP_SIZE = -1;
		}
		try{
			Configuration.ROBUSTNESS_ATTEMPTS = Integer.parseInt(attempts.getText());
		} catch (NumberFormatException e){
			Configuration.ROBUSTNESS_ATTEMPTS = -1;
		}

		try{
			String[] tmp = variability.getText().split(";");
			Configuration.ROBUSTNESS_VARIABILITIES = new int[tmp.length];
			for (int i = 0; i < tmp.length; ++i)
				Configuration.ROBUSTNESS_VARIABILITIES[i] = Integer.parseInt(tmp[i]);
		} catch (NumberFormatException e){
			Configuration.ROBUSTNESS_VARIABILITIES = new int[] {-1};
		}
		
		try{
			Configuration.ROBUSTNESS_Q = Double.parseDouble(q.getText());
		} catch (NumberFormatException e){
			Configuration.ROBUSTNESS_Q = -1;
		}
		try{
			String[] tmp = g.getText().split(";");
			Configuration.ROBUSTNESS_GS = new int[tmp.length];
			for (int i = 0; i < tmp.length; ++i)
				Configuration.ROBUSTNESS_GS[i] = Integer.parseInt(tmp[i]);
		} catch (NumberFormatException e){
			Configuration.ROBUSTNESS_GS = new int[] {-1};
		}
		try{
			Configuration.ROBUSTNESS_H= Integer.parseInt(h.getText());
		} catch (NumberFormatException e){
			Configuration.ROBUSTNESS_H= -1;
		}

	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		//change also the state of all internal components
		for (Component comp:getComponents()) {
			comp.setEnabled(enabled);
		}
	}
	
	public static void main(String[] args) {
		JFrame gui = new JFrame();
		gui.setMinimumSize(new Dimension(900,500));
		gui.setLocationRelativeTo(null);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // .DISPOSE_ON_CLOSE);
		
		RobustnessConfigurationPanel panel = new RobustnessConfigurationPanel();
		gui.add(panel);
		
		gui.setVisible(true);
	}

}
