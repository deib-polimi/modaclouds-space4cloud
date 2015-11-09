package it.polimi.modaclouds.space4cloud.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import it.polimi.modaclouds.space4cloud.milp.Solver;

public class MILPEvaluator {

	protected Solver solver;

	protected File resourceEnvExt = null, solution = null,
			multiCloudExt = null;

	protected boolean computed = false;

	protected int cost = -1;

	protected long evaluationTime = -1L;
	
	private static final Logger logger = LoggerFactory.getLogger(MILPEvaluator.class);
	
	public MILPEvaluator() {
		Path tempConf;
		try {
			tempConf = Files.createTempFile("conf", ".properties");
			Configuration.saveConfiguration(tempConf.toString());
			solver = new Solver(tempConf.toString());
		} catch (Exception e) {
			logger.error("Error in initializing the MILP tool!", e);
		}
	}
	
	public final static int MAX_ATTEMPTS = 2; 

	public void eval() throws Exception {
		for (int attempt = 1; attempt <= MAX_ATTEMPTS; ++attempt) {
			resourceEnvExt = solver.getResourceModelExt();
			solution = solver.getSolution();
			multiCloudExt = solver.getMultiCloudExt();
			cost = -1;
			evaluationTime = -1L;
			computed = true;
			
			if (resourceEnvExt != null && solution != null && multiCloudExt != null
					&& solution.exists() && getCost() > -1)
				attempt = MAX_ATTEMPTS;
			else {
				logger.info("I'll try again in 5 seconds...");
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (resourceEnvExt == null || solution == null || multiCloudExt == null
				|| !solution.exists() || getCost() == -1) {
			reset();
			throw new Exception(
					"Error! It's impossible to generate the solution! Are you connected?");
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

	public File getMultiCloudExt() {
		return multiCloudExt;
	}

	public File getResourceEnvExt() {
		return resourceEnvExt;
	}

	public File getSolution() {
		return solution;
	}

	private void parseResults() {
		if (!computed || solution == null || cost > -1 || evaluationTime > -1L)
			return;

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(solution);
			doc.getDocumentElement().normalize();

			NodeList nl = doc.getElementsByTagName("SolutionMultiResult");

			// if (nl.getLength() == 0)
			// nl = doc.getElementsByTagName("SolutionResult");

			Element root = (Element) nl.item(0);
			cost = (int) Math.round(Double.parseDouble(root
					.getAttribute("cost")));
			evaluationTime = Long.parseLong(root.getAttribute("time"));

		} catch (Exception e) {
			cost = -1;
			evaluationTime = -1L;
			return;
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

	public void setProviders(String... provider) {
		solver.setProviders(provider);
		reset();
	}

	public void setRegions(String... region) {
		solver.setRegions(region);
		reset();
	}

	public void setStartingSolution(File f) {
		solver.setStartingSolution(f);
		reset();
	}

}