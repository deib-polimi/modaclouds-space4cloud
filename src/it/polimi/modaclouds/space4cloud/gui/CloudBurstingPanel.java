package it.polimi.modaclouds.space4cloud.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import it.polimi.modaclouds.space4cloud.optimization.bursting.Host;
import it.polimi.modaclouds.space4cloud.utils.Configuration;

public class CloudBurstingPanel extends JPanel implements ActionListener, MouseListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8829215569879059215L;
	private static final String PANEL_NAME = "Bursting"; //"Cloud Bursting";
	
	private JList<Host> list;
	private static DefaultListModel<Host> lm;
	private JButton add, remove;
	private JButton save, load;
	private JTextField configuration;
	
	public CloudBurstingPanel() {
		setName(PANEL_NAME);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{201, 201, 0};
		gridBagLayout.rowHeights = new int[]{35, 35, 35, 35, 35, 35, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridBagLayout);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridx = 0;
		c.gridy = 0;
        c.insets = new Insets(0, 0, 5, 5);
		add(new JLabel("Hosts to be used"), c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		add(new JLabel(""), c);
		
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		int oldIpady = c.ipady;
		c.ipady = 250;
		double oldWeigthx = c.weightx;
		c.weightx = 0.0;
		c.insets = new Insets(0, 0, 5, 5);
		
		lm = new DefaultListModel<Host>();
		list = new JList<Host>(lm);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addMouseListener(this);
		
		JScrollPane scrollPane = new JScrollPane(list);
		
		add(scrollPane, c);
		
		c.ipady = oldIpady;
		c.weightx = oldWeigthx;
		c.gridwidth = 1;
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);
		add(new JLabel(""), c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		
		add = new JButton("Add new");
		add.addActionListener(this);
		remove = new JButton("Remove selected");
		remove.addActionListener(this);
		
		JPanel pan = new JPanel(new FlowLayout());
		pan.add(add);
		pan.add(remove);
		
		add(pan, c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);
		add(new JLabel("External configuration"), c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		add(new JLabel(""), c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 0, 5, 5);
		configuration = new JTextField(10);
		add(configuration, c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 5, 0);
		
		save = new JButton("Save");
		save.addActionListener(this);
		load = new JButton("Load");
		load.addActionListener(this);
		
		pan = new JPanel(new FlowLayout());
		pan.add(save);
		pan.add(load);
		
		add(pan, c);
		
	}
	
	/**
	 * Updates the values shown to the user according to those stored in the Configuration class
	 */
	public void loadConfiguration() {
		String file = Configuration.PRIVATE_CLOUD_HOSTS;
		if (file != null && !file.equals("") && Files.exists(Paths.get(file)))
			loadFile(Paths.get(file).toFile());
	}
	
	private void loadFile(File f) {
		configuration.setText(f.getAbsolutePath());
		
		lm.clear();
		
		List<Host> hosts = Host.getFromFile(f);
		for (Host h : hosts)
			addElement(h);
	}
	
	public static void saveFile(File f) {
		List<Host> hosts = new ArrayList<Host>();
		for (int i = 0; i < lm.size(); ++i) {
			hosts.add(lm.get(i));
		}
		
		Host.writeToFile(f, hosts);
	}
	
	/**
	 * Updates values in the Configuration class according to those selected in the panel
	 */
	public void updateConfiguration() {
		Configuration.PRIVATE_CLOUD_HOSTS = configuration.getText();		
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
		
		CloudBurstingPanel panel = new CloudBurstingPanel();
		gui.add(panel);
		
		gui.setVisible(true);
	}
	
	public void addElement(Host host) {
		lm.addElement(host);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(add)) {
			new SetHostWindow(this);
		} else if (e.getSource().equals(remove)) {
			int sel = list.getSelectedIndex();						
			if (sel > -1) {
				Host host = lm.get(sel);
				Object[] options = {"Yes",
	                    "No"};
				int n = JOptionPane.showOptionDialog(null,
				    "You are going to remove this host:\n" +
				    	host.name + 
						"\nAre you sure?",
				    "Removing of a Host",
				    JOptionPane.YES_NO_CANCEL_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    null,
				    options,
				    options[1]);
				if (n == 0)
					lm.removeElementAt(sel);
			}
		} else if (e.getSource().equals(save)) {
			File f = FileLoader.saveFile("Configuration file for the Private Hosts", "properties");
			if (f != null) {
				configuration.setText(f.getAbsolutePath());
				saveFile(f);
			}
		} else if (e.getSource().equals(load)) {
			File f = FileLoader.loadFile("Configuration file for the Private Hosts", "properties");
			if (f != null) {
				configuration.setText(f.getAbsolutePath());
				loadFile(f);
			}
		} 
	}

	@Override
	public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            int sel = list.getSelectedIndex();
            
            Host host = lm.get(sel);
            new SetHostWindow(this, host);
            lm.removeElementAt(sel);
        }
    }
	
	public static boolean hasHosts(){
		return !CloudBurstingPanel.lm.isEmpty();
	}

	@Override
	public void mousePressed(MouseEvent e) { }

	@Override
	public void mouseReleased(MouseEvent e) { }

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }

}
