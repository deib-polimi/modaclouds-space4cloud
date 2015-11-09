package it.polimi.modaclouds.space4cloud.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.osgi.framework.FrameworkUtil;

import it.polimi.modaclouds.space4cloud.optimization.OptimizationEngine;

public class BestSolutionExplorer extends WindowAdapter implements ActionListener, PropertyChangeListener {

	private static JFrame frmBestSolutionsExplorer;
	private SolutionWindowPanel solutionWindowTab;	
	private int current;
	private JButton btnPrevious;
	private JButton btnNext;
	private GridBagConstraints gbc_solutionPanel;
	
	private OptimizationEngine engine = null;
	

	/**
	 * Create the application.
	 */
	private BestSolutionExplorer(OptimizationEngine engine) {
		this.engine = engine;
		addPropertyChangeListener(engine);
		engine.addPropertyChangeListener(this);
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmBestSolutionsExplorer = new JFrame();
		frmBestSolutionsExplorer.setTitle("Best Solutions Explorer");
//		frmBestSolutionsExplorer.setBounds(100, 100, 450, 300);
		frmBestSolutionsExplorer.setMinimumSize(new Dimension(900, 600));
		frmBestSolutionsExplorer.setLocationRelativeTo(null);
		frmBestSolutionsExplorer.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // .DISPOSE_ON_CLOSE);
		frmBestSolutionsExplorer.addWindowListener(this);
		Image favicon = new ImageIcon(FrameworkUtil.getBundle(ConfigurationWindow.class).getEntry("icons/Cloud.png")).getImage();
		frmBestSolutionsExplorer.setIconImage(favicon);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 30, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		frmBestSolutionsExplorer.getContentPane().setLayout(gridBagLayout);
		
		current = engine.getBestSolutions().size()-1;
		solutionWindowTab = new SolutionWindowPanel(engine.getBestSolutions().get(current));
		gbc_solutionPanel = new GridBagConstraints();
		gbc_solutionPanel.insets = new Insets(0, 0, 5, 0);
		gbc_solutionPanel.fill = GridBagConstraints.BOTH;
		gbc_solutionPanel.gridx = 0;
		gbc_solutionPanel.gridy = 0;
		frmBestSolutionsExplorer.getContentPane().add(solutionWindowTab, gbc_solutionPanel);


		JPanel commandPanel = new JPanel();
		GridBagConstraints gbc_commandPanel = new GridBagConstraints();
		gbc_commandPanel.fill = GridBagConstraints.BOTH;
		gbc_commandPanel.gridx = 0;
		gbc_commandPanel.gridy = 1;
		frmBestSolutionsExplorer.getContentPane().add(commandPanel, gbc_commandPanel);

		btnPrevious = new JButton("Previous");
		btnPrevious.addActionListener(this);
		if(current == 0)
			btnPrevious.setEnabled(false);
		commandPanel.add(btnPrevious);

		btnNext = new JButton("Next");
		btnNext.addActionListener(this);
		btnNext.setEnabled(false);
		commandPanel.add(btnNext);
		
		prepared = true;
	}
	
	public static void show() {
		if (shown || !prepared)
			return;
		
		frmBestSolutionsExplorer.setVisible(true);
		shown = true;
	}

	private static boolean shown = false;
	private static boolean prepared = false;

	/**
	 * Shows the window with the set of solutions specified. If the window has already shown it updates  the window according to the specified solutions
	 * @param solutions
	 */
	public static void show(OptimizationEngine engine){
		prepare(engine);
		
		show();
			
	}
	
	/**
	 * Create the window with the set of solutions specified without showing it.
	 * @param solutions
	 */
	public static void prepare(OptimizationEngine engine){
		if (!prepared)
			new BestSolutionExplorer(engine);
	}



	private void next(){
		if(current < engine.getBestSolutions().size()-1){
			current++;			
			frmBestSolutionsExplorer.getContentPane().remove(solutionWindowTab);			
			solutionWindowTab = new SolutionWindowPanel(engine.getBestSolutions().get(current));
			frmBestSolutionsExplorer.getContentPane().add(solutionWindowTab, gbc_solutionPanel);
			frmBestSolutionsExplorer.getContentPane().validate();
			if(current==engine.getBestSolutions().size()-1)
				btnNext.setEnabled(false);
			if(current!=0)
				btnPrevious.setEnabled(true);
		}
	}

	private void previous(){
		if(current > 0){
			current--;
			frmBestSolutionsExplorer.getContentPane().remove(solutionWindowTab);			
			solutionWindowTab = new SolutionWindowPanel(engine.getBestSolutions().get(current));
			frmBestSolutionsExplorer.getContentPane().add(solutionWindowTab, gbc_solutionPanel);
			frmBestSolutionsExplorer.getContentPane().validate();
			if(current<engine.getBestSolutions().size()-1)
				btnNext.setEnabled(true);
			if(current==0)
				btnPrevious.setEnabled(false);
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(btnNext)){
			next();
		}else if(e.getSource().equals(btnPrevious)){
			previous();
		}
		
	}
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public void addPropertyChangeListener(PropertyChangeListener listener){
		pcs.addPropertyChangeListener(listener);
	}
	
	public static final String PROPERTY_WINDOW_CLOSED = "BSEWindowClosed";
	public static final String PROPERTY_ADDED_VALUE = "BSEAddedValue";
	
	@Override
	public void windowClosing(WindowEvent e) {		
		super.windowClosing(e);
		frmBestSolutionsExplorer.dispose();
		shown = false;
		prepared = false;
		pcs.firePropertyChange(PROPERTY_WINDOW_CLOSED, false, true);		
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PROPERTY_ADDED_VALUE)) {
			if(current==engine.getBestSolutions().size()-1)
				btnNext.setEnabled(false);
			else
				btnNext.setEnabled(true);
			if(current!=0)
				btnPrevious.setEnabled(true);
		}
		
	}
	
	public static void close() {
		if(shown)
			frmBestSolutionsExplorer.dispatchEvent(new WindowEvent(frmBestSolutionsExplorer, WindowEvent.WINDOW_CLOSING));
	}

}
