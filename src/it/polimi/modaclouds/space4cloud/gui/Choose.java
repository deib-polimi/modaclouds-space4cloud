/*******************************************************************************
 * Copyright 2014 Giovanni Paolo Gibilisco
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * 
 */
package it.polimi.modaclouds.space4cloud.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import de.uka.ipd.sdq.pcmsolver.runconfig.MessageStrings;

// TODO: Auto-generated Javadoc
/**
 * The Class Choose.
 */
public class Choose extends JDialog {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3482519292345715872L;

	/** The content panel. */
	private final JPanel contentPanel = new JPanel();

	/** The yes. */
	private boolean yes;

	/** The solver. */
	private String solver;

	/** True if the user choose what to do */
	private boolean chosen;

	/**
	 * Create the dialog.
	 * 
	 * @param title
	 *            the title
	 * @param text
	 *            the text
	 * @param yesDefault
	 *            the yes default
	 */
	public Choose(String title, String text) {
		setAlwaysOnTop(true);
		setModal(true);
		setResizable(false);
		setType(Type.UTILITY);
		setTitle(title);
		setBounds(100, 100, 370, 130);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JTextPane txtpnDoYouWant = new JTextPane();
		txtpnDoYouWant.setFont(new Font("Tahoma", Font.BOLD, 13));
		txtpnDoYouWant.setBackground(UIManager.getColor("Panel.background"));
		txtpnDoYouWant.setText(text);
		contentPanel.add(txtpnDoYouWant, BorderLayout.CENTER);

		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton LQNSButton = new JButton("LQNS");
		JButton PerfEngineButton = new JButton("LINE");
		JButton SimucomEngineButton = new JButton("Simucom");

		LQNSButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				solver = MessageStrings.LQNS_SOLVER;
				chosen = true;
				dispose();
			}
		});

		PerfEngineButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				solver = MessageStrings.PERFENGINE_SOLVER;
				chosen = true;
				dispose();
			}
		});

		SimucomEngineButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				solver = "Simucom";
				chosen = true;
				dispose();
			}
		});

		buttonPane.add(LQNSButton);
		buttonPane.add(PerfEngineButton);
		buttonPane.add(SimucomEngineButton);
		getRootPane().setDefaultButton(LQNSButton);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		center();
		setVisible(true);
	}

	/**
	 * Create the dialog.
	 * 
	 * @param title
	 *            the title
	 * @param text
	 *            the text
	 * @param yesDefault
	 *            the yes default
	 */
	public Choose(String title, String text, String button1Text,
			String button2Text, boolean yesDefault) {
		setAlwaysOnTop(true);
		yes = yesDefault;
		setResizable(false);
		setModal(true);
		setType(Type.UTILITY);
		setTitle(title);
		setBounds(100, 100, 370, 130);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JTextPane txtpnDoYouWant = new JTextPane();
		txtpnDoYouWant.setFont(new Font("Tahoma", Font.BOLD, 13));
		txtpnDoYouWant.setBackground(UIManager.getColor("Panel.background"));
		txtpnDoYouWant.setText(text);
		contentPanel.add(txtpnDoYouWant, BorderLayout.CENTER);

		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton okButton = new JButton(button1Text);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				yes = true;
				chosen = true;
				dispose();
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		JButton cancelButton = new JButton(button2Text);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				yes = false;
				chosen = true;
				dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		center();
		setVisible(true);
	}

	/**
	 * Centers the specified frame.
	 * 
	 */
	private void center() {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		int screenHeight = screenSize.height;
		int screenWidth = screenSize.width;
		int height = getHeight();
		int width = getWidth();
		setLocation((screenWidth - width) / 2, (screenHeight - height) / 2);
	}

	public String getSolver() {
		return solver;
	}

	public boolean isChosen() {
		return chosen;
	}

	/**
	 * Checks if is yes.
	 * 
	 * @return true, if is yes
	 */
	public boolean isTrue() {
		return yes;
	}
}
