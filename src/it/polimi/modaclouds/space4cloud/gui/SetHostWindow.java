package it.polimi.modaclouds.space4cloud.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.modaclouds.resourcemodel.cloud.Cost;
import it.polimi.modaclouds.space4cloud.optimization.bursting.Host;

public class SetHostWindow {
	
	private static final String FRAME_NAME = "Details about the Host";
	
	private static final Logger logger=LoggerFactory.getLogger(SetHostWindow.class);
	
	private JFrame frame;
	
	private Host host;
	
	private String name;
	private int cpu_cores;
	private double cpu_speed;
	private int ram;
	private int storage;
	private double density;
	private double[] hourlyCosts;
	
	private JTextField tfName, tfCores, tfSpeed, tfRam, tfStorage, tfDensity;
	private JTable tbCosts;
	
	private CloudBurstingPanel cbp;
	
	public static final double DEFAULT_HOURLY_COST = 0.01;
	
	public SetHostWindow(CloudBurstingPanel cbp) {
		this(cbp, null);
	}
	
	public SetHostWindow(CloudBurstingPanel cbp, Host host) {
		this.cbp = cbp;
		
		if (host != null) {
			this.host = host;
			name = host.name;
			cpu_cores = host.cpu_cores;
			cpu_speed = host.cpu_speed;
			ram = host.ram;
			storage = host.storage;
			density = host.density;
			
			hourlyCosts = new double[24];
			List<Cost> costs = host.energyCost.getComposedOf();
			int i = 0;
			for (; i < hourlyCosts.length && i < costs.size(); ++i) {
				Cost c = costs.get(i);
				hourlyCosts[i] = c.getValue();
			}
			for (; i < hourlyCosts.length; ++i)
				hourlyCosts[i] = DEFAULT_HOURLY_COST;
				
		} else {
			this.host = null;
			name = "Host";
			cpu_cores = 4;
			cpu_speed = 3.0 * 1000;
			ram = 16 * 1024;
			storage = 500;
			density = 1.0;
			
			hourlyCosts = new double[24];
			for (int i = 0; i < hourlyCosts.length; ++i)
				hourlyCosts[i] = DEFAULT_HOURLY_COST;
		}
		
		initialize();
	}
	
	public Host getHost() {
		return host;
	}
	
	private class TableModel extends AbstractTableModel {
		private static final long serialVersionUID = -7618478753251037048L;
		
		private String[] columnNames = {"Hour", "Cost for the energy"};
		private Object[][] data = new Object[24][2];
		
		public TableModel(double[] hourlyCosts) {
			for (int i = 0; i < 24; ++i) {
				data[i][0] = i;
				data[i][1] = hourlyCosts[i];
			}
		}
		
		@Override
		public void setValueAt(Object value, int row, int col) {
	        data[row][col] = Double.parseDouble((String)value);
//	        fireTableCellUpdated(row, col);
	    }

		@Override
		public boolean isCellEditable(int row, int column) {
			return (column == 1);
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public int getColumnCount() {
			return data[0].length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex];
		}
		
		@Override
		public String getColumnName(int col) {
	        return columnNames[col];
	    }
	}
	
	private void save() {
		if (tfName.getText() != null && tfName.getText().length() > 0)
			name = tfName.getText();
		
		try {
			cpu_cores = Integer.parseInt(tfCores.getText());
		} catch (NumberFormatException e) { }
		
		try {
			cpu_speed = Double.parseDouble(tfSpeed.getText());
		} catch (NumberFormatException e) { }
		
		try {
			ram = Integer.parseInt(tfRam.getText());
		} catch (NumberFormatException e) { }
		
		try {
			storage = Integer.parseInt(tfStorage.getText());
		} catch (NumberFormatException e) { }
		
		try {
			density = Double.parseDouble(tfDensity.getText());
		} catch (NumberFormatException e) { }
		
		hourlyCosts = new double[24];
		TableModel dtm = (TableModel) tbCosts.getModel();
		
		for (int i = 0; i < hourlyCosts.length; ++i) {
			try {
				hourlyCosts[i] = (Double)dtm.getValueAt(i, 1);
			} catch (Exception e) { }
		}
		
		host = new Host(name, cpu_cores, cpu_speed, ram, storage, density, hourlyCosts);
		
		if (cbp != null) {
			cbp.addElement(host);
		}
		
		frame.dispose();
		
//		System.exit(0);
	}
	
	private void initialize() {
		frame = new JFrame();
//		frame.setBounds(100, 100, 701, 431);		
		frame.setMinimumSize(new Dimension(500,700));
		frame.setLocationRelativeTo(null);
		frame.setTitle(FRAME_NAME);
		Image favicon = new ImageIcon(FrameworkUtil.getBundle(ConfigurationWindow.class).getEntry("icons/Cloud.png")).getImage();
		frame.setIconImage(favicon);
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
		    	save();
		    }
		});
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{201, 201, 0};
		gridBagLayout.rowHeights = new int[]{35, 35, 35, 35, 35, 35, 35, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		GridBagConstraints c = new GridBagConstraints();
		frame.setLayout(gridBagLayout);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridx = 0;
		c.gridy = 0;
        c.insets = new Insets(10, 10, 15, 15);
		frame.add(new JLabel("Name"), c);
		tfName = new JTextField(name, 10);
		c.gridx = 1;
		c.insets = new Insets(10, 10, 15, 10);
		frame.add(tfName, c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 10, 15, 15);
		frame.add(new JLabel("Number of cores"), c);
		c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 10, 15, 10);
		tfCores = new JTextField(Integer.toString(cpu_cores), 10);
		frame.add(tfCores, c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 10, 15, 15);
		frame.add(new JLabel("Speed of each core"), c);
		c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 10, 15, 10);
		tfSpeed = new JTextField(Double.toString(cpu_speed), 10);
		frame.add(tfSpeed, c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 10, 15, 15);
		frame.add(new JLabel("RAM size"), c);
		c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 10, 15, 10);
		tfRam = new JTextField(Integer.toString(ram), 10);
		frame.add(tfRam, c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 10, 15, 15);
		frame.add(new JLabel("Storage size"), c);
		c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 10, 15, 10);
		tfStorage = new JTextField(Integer.toString(storage), 10);
		frame.add(tfStorage, c);
		
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(0, 10, 15, 15);
		frame.add(new JLabel("Density"), c);
		c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 10, 15, 10);
		tfDensity = new JTextField(Double.toString(density), 10);
		frame.add(tfDensity, c);
		
//		c.gridx = 0;
//		c.gridy++;
//		c.insets = new Insets(0, 10, 15, 15);
//		frame.add(new JLabel("Hourly costs for the energy"), c);
//		c.gridx = 1;
//		c.gridwidth = GridBagConstraints.REMAINDER;
//		c.insets = new Insets(0, 10, 15, 10);
//		frame.add(new JLabel(""), c);
		
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		c.ipady = 400;
		c.weightx = 0.0;
		c.insets = new Insets(0, 10, 15, 10);
		
		tbCosts = new JTable(new TableModel(hourlyCosts));
		JScrollPane scrollPane = new JScrollPane(tbCosts);
		tbCosts.setPreferredScrollableViewportSize(new Dimension(500, 70));
		
		frame.add(scrollPane, c);
		
		frame.setVisible(true);
	}

	
	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new SetHostWindow(null);
				} catch (Exception e) {
					logger.error("Error while drawing the GUI.", e);
				}
			}
		});
	}
}
