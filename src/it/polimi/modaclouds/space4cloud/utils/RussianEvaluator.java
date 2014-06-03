package it.polimi.modaclouds.space4cloud.utils;

import it.polimi.modaclouds.space4clouds.milp.Solver;

import java.io.File;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RussianEvaluator {
	
	protected Constants c = Constants.getInstance();
	
	protected Solver s;
	
	protected File resourceEnvExt = null, solution = null, multiCloudExt = null;
	
	protected boolean computed = false;
	
	protected int cost = -1;
	
	protected long evaluationTime = -1L;
	
	public RussianEvaluator(File usageModelExtFile, File constraintFile) {
		s = new Solver(c.PROJECT_PATH, c.WORKING_DIRECTORY,
				c.RESOURCE_MODEL, c.USAGE_MODEL, c.ALLOCATION_MODEL, c.REPOSITORY_MODEL,
				Paths.get(Paths.get(c.RESOURCE_MODEL).getParent().toString(), "default.system").toString(),
				constraintFile.getAbsolutePath(), usageModelExtFile.getAbsolutePath());
		
		s.getOptions().SqlDBUrl="jdbc:mysql://localhost:3306/";
		s.getOptions().DBName = "cloud_full";
		s.getOptions().DBUserName="moda";
		s.getOptions().DBPassword="modaclouds";
	}
	
	public void setProviders(String... provider) {
		s.setProviders(provider);
		reset();
	}
	
	public void setRegions(String... region) {
		s.setRegions(region);
		reset();
	}
	
	public void setMinimumNumberOfProviders(int num) {
		s.setMinimumNumberOfProviders(num);
		reset();
	}
	
	public void setStartingSolution(File f) {
		s.setStartingSolution(f);
		reset();
	}
	
	public void eval() throws Exception {
		resourceEnvExt = s.getResourceModelExt();
		solution = s.getSolution();
		multiCloudExt = s.getMultiCloudExt();
		cost = -1;
		evaluationTime = -1L;
		computed = true;
		
		if (resourceEnvExt == null || solution == null || multiCloudExt == null || !solution.exists()) {
			reset();
			throw new Exception("Error! It's impossible to generate the solution! Are you connected?");
		}
	}
	
	public void reset() {
		resourceEnvExt = null;
		solution = null;
		multiCloudExt = null;
		computed = false;
		cost = -1;
		evaluationTime = -1L;
	}
	
	private void parseResults() {
		if (!computed || solution == null || cost > -1 || evaluationTime > -1L)
			return;
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(solution);
			doc.getDocumentElement().normalize();
			
			NodeList nl = doc.getElementsByTagName("SolutionMultiResult");
			
//			if (nl.getLength() == 0)
//				nl = doc.getElementsByTagName("SolutionResult");
			
			Element root = (Element) nl.item(0);
			cost = (int)Math.round(Double.parseDouble(root.getAttribute("cost")) * 1000);
			evaluationTime = Long.parseLong(root.getAttribute("time"));
			
		} catch (Exception e) {
			cost = -1;
			evaluationTime = -1L;
			return;
		}
	}
	
	public int getCost() {
		parseResults();
		return cost;
	}
	
	public long getEvaluationTime() {
		parseResults();
		return evaluationTime;
	}

	public File getResourceEnvExt() {
		return resourceEnvExt;
	}

	public File getSolution() {
		return solution;
	}

	public File getMultiCloudExt() {
		return multiCloudExt;
	}
	
}
