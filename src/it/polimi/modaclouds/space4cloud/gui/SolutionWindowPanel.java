package it.polimi.modaclouds.space4cloud.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.modaclouds.qos_models.schema.ClosedWorkload;
import it.polimi.modaclouds.qos_models.schema.ClosedWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.OpenWorkload;
import it.polimi.modaclouds.qos_models.schema.OpenWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtensions;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.chart.GenericChart;
import it.polimi.modaclouds.space4cloud.mainProgram.Space4Cloud;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Queue;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4cloud.utils.Configuration;

public class SolutionWindowPanel extends JTabbedPane{

    private static final long serialVersionUID = 4205611616209241366L;
    
    private static final Logger logger=LoggerFactory.getLogger(SolutionWindowPanel.class);

    private SolutionMulti solutionMulti;

    private GenericChart<DefaultCategoryDataset> populations;

    private HashMap<String, GenericChart<DefaultCategoryDataset>> workloads;
    private HashMap<String, GenericChart<DefaultCategoryDataset>> allocations;
    
    private File usageModelExtension;

    private boolean alreadyUpdating = false;

    private JPanel informationPanel;
    private JLabel informationLabel = new JLabel();

    private HashMap<String, JPanel> informationPanels;
    private HashMap<String, JLabel> informationLabels;
    
    public static void show(SolutionMulti solutionMulti) {
    	JFrame gui = new JFrame();
    	gui.setTitle("Solution Viewer");
    	gui.setMinimumSize(new Dimension(900, 600));
    	gui.setLocationRelativeTo(null);
		gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		gui.add(new SolutionWindowPanel(solutionMulti));
		
		gui.setVisible(true);
    }
    
    public static void show(Solution solution) {
    	SolutionMulti solutionMulti = new SolutionMulti();
    	solutionMulti.add(solution);
    	show(solutionMulti);
    }

    public SolutionWindowPanel(SolutionMulti solutionMulti, File usageModelExtension) {
        super();

        this.solutionMulti = solutionMulti;
        this.usageModelExtension = usageModelExtension;
        
        populations = new GenericChart<DefaultCategoryDataset>(null, "Hour", "Population");
        populations.dataset = new DefaultCategoryDataset();
        populations.defaultRange = true;
        populations.labelsVisible = false;
        populations.pointsVisible = false;
        
        workloads = new HashMap<String, GenericChart<DefaultCategoryDataset>>();

        allocations = new HashMap<String, GenericChart<DefaultCategoryDataset>>();

        informationPanels = new HashMap<String, JPanel>();
        informationLabels = new HashMap<String, JLabel>();

        setData(solutionMulti);

    }

    /**
     * @wbp.parser.constructor
     */
    public SolutionWindowPanel(SolutionMulti solutionMulti) {
        this(solutionMulti, null);
    }

    public void setData(SolutionMulti solutionMulti) {      
        if (!solutionMulti.isEvaluated())
            return;

        StringBuffer infoAll = new StringBuffer();
		
		infoAll.append("<html>\n<body style='font-size: 13;'>\n");
		infoAll.append("<table style='width: 500px; border: 1px solid black; margin: 30px; padding: 2px' border=1>\n");
		infoAll.append("<tr><th style='border: 0; background: black; color: white;'>Information</th><th style='border:0; background: black; color: white;'>Value</th></tr>\n");
		infoAll.append("<tr><td>Total Cost</td><td>" + solutionMulti.getCost() + "</td></tr>\n");
		if (Configuration.CONTRACTOR_TEST) {
			File f = Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY, it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_NAME + it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_EXTENSION).toFile();
			if (f.exists())
				infoAll.append("<tr><td>Total Cost considering Contracts</td><td>" + SolutionMulti.getCost(f) + "</td></tr>\n");
		}
		infoAll.append("<tr><td>Evaluation Time</td><td>" + Space4Cloud.durationToString(solutionMulti.getGenerationTime()) + "</td></tr>\n");
		infoAll.append("<tr><td>Providers</td><td><ul>\n");
		for (int i = 0; i < solutionMulti.size(); ++i) {
			String provider = solutionMulti.get(i).getProvider();
			infoAll.append("<li>" +  provider + "</li>\n");
		}
		infoAll.append("</ul></td></tr>\n");
		infoAll.append("<tr><td>Feasible</td><td>" + String.valueOf(solutionMulti.isFeasible()) + "</td></tr>\n");
		
		boolean set = false;
		
		for (int i = 0; i < solutionMulti.size(); ++i) {
			String provider = solutionMulti.get(i).getProvider();
			Solution solution = solutionMulti.get(provider);
			if (solution == null)
				continue;
			
			GenericChart<DefaultCategoryDataset> workload = new GenericChart<DefaultCategoryDataset>(null, "Hour", "Workload Percentage") {
				private static final long serialVersionUID = -8965200786509511753L;
				@Override
				public String getFormattedValue(double number) {
					return (int)(number * 100) + "%";
				}
			};
			workload.dataset = new DefaultCategoryDataset();
			workload.exactMax = 1.1;
			workload.exactMin = -0.1;
			
			GenericChart<DefaultCategoryDataset> allocation = new GenericChart<DefaultCategoryDataset>(null, "Hour", "Allocation");
			allocation.dataset = new DefaultCategoryDataset();
			allocation.shownValsAboveMax = 1;
			allocation.shownValsBelowMin = 1;
			
			for (int hour = 0; hour < 24; ++hour) {
				workload.dataset.addValue(solution.getPercentageWorkload(hour), provider, "" + hour);
				for (Tier t : solution.getApplication(hour).getTiers()) {
					allocation.dataset.addValue(getReplicas(t), t.getPcmName(), "" + hour);
				}
			}
			
			workloads.put(provider, workload);
			allocations.put(provider, allocation);
			
			if (!set) {
				infoAll.append("<tr><td>Components</td><td><ul>\n");
				for (Tier t : solution.getApplication(0).getTiers()) {
					infoAll.append("<li>" + t.getPcmName() + "</li>\n");
				}
				infoAll.append("</ul></td></tr>\n");
				infoAll.append("</table>\n</body>\n</html>\n");
				
				informationLabel.setText(infoAll.toString());
				set = true;
			}
			
			JLabel informationLabel = new JLabel();
			informationLabels.put(provider, informationLabel);
			
			StringBuffer info = new StringBuffer();
			info.append("<html>\n<body style='font-size: 13;'>\n");
			info.append("<table style='width: 500px; border: 1px solid black; margin: 30px; padding: 2px' border=1>\n");
			info.append("<tr><th style='border: 0; background: black; color: white;'>Information</th><th style='border:0; background: black; color: white;'>Value</th></tr>\n");
			info.append("<tr><td>Cost</td><td>" + solution.getCost() + "</td></tr>\n");
//			info.append("<tr><td>Evaluation Time</td><td>" + Space4Cloud.durationToString(solution.getEvaluationTime()) + " ms</td></tr>\n");
			info.append("<tr><td>Provider</td><td>" + provider + "</td></tr>\n");
			if (solution.getRegion() != null)
				info.append("<tr><td>Region</td><td>" + solution.getRegion() + "</td></tr>\n");
			info.append("<tr><td>Feasible</td><td>" + String.valueOf(solution.isFeasible()) + "</td></tr>\n");
			info.append("<tr><td>Solution per Component</td><td><ul>\n");
			for (Tier t : solution.getApplication(0).getTiers()) {
				info.append("<li>" + t.getPcmName() + "\n<ul>\n");
				info.append("<li>" + t.getCloudService().getServiceName() + "</li>\n");
				info.append("<li>" + t.getCloudService().getResourceName() + "</li>\n");
				if (t.getCloudService() instanceof Queue)
					info.append("<li>Daily requests: " + solution.getDailyRequestsByTier(t.getId()) * ((Queue)t.getCloudService()).getMultiplyingFactor() + "</li>\n");
				else
					info.append("<li>Daily requests: " + solution.getDailyRequestsByTier(t.getId()) + "</li>\n");
				info.append("</ul>\n</li>\n");
			}
			info.append("</ul></td></tr>\n");
			info.append("</table>\n</body>\n</html>\n");
			informationLabel.setText(info.toString());
		}

        if (usageModelExtension != null && usageModelExtension.exists())
            setPopulation(usageModelExtension);
        else if (Configuration.USAGE_MODEL_EXTENSION != null && Configuration.USAGE_MODEL_EXTENSION.length() > 0)
            setPopulation(new File(Configuration.USAGE_MODEL_EXTENSION));

        updateGraph();

        initialize();
    }
    
    private int getReplicas(Tier t) {
		return t.getCloudService().getReplicas();
	}

    public void setPopulation(File usageModelExtension) {
        populations.clear();

        try {
            UsageModelExtensions umes = XMLHelper.deserialize(usageModelExtension
                    .toURI().toURL(), UsageModelExtensions.class);

            //      String name = "Var " + Space4Cloud.getMaxPopulation(usageModelExtension);

            ClosedWorkload cw = umes.getUsageModelExtension().getClosedWorkload();
            if (cw != null) {
                for (ClosedWorkloadElement we : cw.getWorkloadElement())
                    populations.dataset.addValue(we.getPopulation(), "Population", "" + we.getHour());
            } else {

                OpenWorkload ow = umes.getUsageModelExtension().getOpenWorkload();
                if (ow != null) {
                    for (OpenWorkloadElement we : ow.getWorkloadElement())
                        populations.dataset.addValue(we.getPopulation(), "Population", "" + we.getHour());
                } else
                    return;
            }
        } catch (Exception e) {
            logger.error("Error while setting the population file.", e);
        }
    }

    public void initialize() {

        // listener to resize images
        addComponentListener(new ComponentListener() {

            @Override
            public void componentHidden(ComponentEvent e) { }

            @Override
            public void componentMoved(ComponentEvent e) { }

            @Override
            public void componentResized(ComponentEvent e) {
                updateImages();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                updateImages();
            }
        });

        JPanel pan = new JPanel(new GridLayout(2, 1));
		addTab("Generic", pan);
		
		pan.add(populations);
		populations.setBorder(new TitledBorder(null, "Population Graph",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		informationPanel = new JPanel();
		
		JScrollPane pane = new JScrollPane(informationPanel);
		
		pan.add(pane);
		
		informationPanel.add(informationLabel);
		
		for (int i = 0; i < solutionMulti.size(); ++i) {
			String provider = solutionMulti.get(i).getProvider();
			pan = new JPanel(new GridLayout(2, 1));
							
			addTab(provider, pan);
			if(i==0)
				setSelectedComponent(pan);
			
			JTabbedPane inTab = new JTabbedPane();
			pan.add(inTab);
			
			inTab.addTab("Population Percentage", workloads.get(provider));
			
			inTab.addTab("Allocations", allocations.get(provider));
			inTab.setSelectedComponent(allocations.get(provider));
			
			JPanel informationPanel = new JPanel();
			informationPanels.put(provider, informationPanel);
			JLabel informationLabel = informationLabels.get(provider);
			
			JScrollPane paneIn = new JScrollPane(informationPanel);
			
			pan.add(paneIn);
			
			informationPanel.add(informationLabel);
		}

		validate();
		
    }

    private void updateGraph() {
    	if (alreadyUpdating)
    		return;
    	
    	alreadyUpdating = true;
    	
    	populations.updateGraph();
    	
    	for (int i = 0; i < solutionMulti.size(); ++i) {
			String provider = solutionMulti.get(i).getProvider();
			
			GenericChart<DefaultCategoryDataset> workload = workloads.get(provider);
			GenericChart<DefaultCategoryDataset> allocation = allocations.get(provider);
			
			if (workload != null)
				workload.updateGraph();
			if (allocation != null)
				allocation.updateGraph();
    	}
    	
    	alreadyUpdating = false;
    }

    private void updateImages() {
        if (alreadyUpdating)
            return;

        alreadyUpdating = true;

        populations.updateImage();
    	
    	for (int i = 0; i < solutionMulti.size(); ++i) {
			String provider = solutionMulti.get(i).getProvider();
			
			GenericChart<DefaultCategoryDataset> workload = workloads.get(provider);
			GenericChart<DefaultCategoryDataset> allocation = allocations.get(provider);
			
			if (workload != null)
				workload.updateImage();
			if (allocation != null)
				allocation.updateImage();
    	}
        
        alreadyUpdating = false;
    }





}