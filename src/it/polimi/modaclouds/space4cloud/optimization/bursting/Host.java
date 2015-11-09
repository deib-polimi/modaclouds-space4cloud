package it.polimi.modaclouds.space4cloud.optimization.bursting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import it.polimi.modaclouds.resourcemodel.cloud.CloudFactory;
import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;
import it.polimi.modaclouds.resourcemodel.cloud.Cost;
import it.polimi.modaclouds.resourcemodel.cloud.CostProfile;
import it.polimi.modaclouds.resourcemodel.cloud.CostUnitType;
import it.polimi.modaclouds.space4cloud.utils.EMF;

public class Host {
	public final static double DEFAULT_HOURLY_COST = 10.0;
	
	public String name;
	
	public int cpu_cores; 		// number
	public double cpu_speed; 	// MHz
	public int ram; 			// MB
	public int storage; 		// GB
	public double density;		
	
	public String toString() {
		return name + " (CPU: " + cpu_cores + "x" + cpu_speed + " MHz, RAM: " + ram + " MB, Disk: " + storage + " GB, Density: " + density + ")";
	}
	
	public CostProfile energyCost;
	
	public List<CloudResource> hostedOn;
	
	public List<String> tiersOn;
	
	public List<Boolean> isOn;
	
	public Host(String name, int cpu_cores, double cpu_speed, int ram, int storage, double density, double[] hourlyCosts) {
		this.name = name;
		
		this.cpu_cores = cpu_cores;
		this.cpu_speed = cpu_speed;
		this.ram = ram;
		this.storage = storage;
		this.density = density;
		
		hostedOn = new ArrayList<CloudResource>();
		setHourlyCosts(hourlyCosts);
		
		isOn = new ArrayList<Boolean>();
		for (int h = 0; h < 24; ++h)
			isOn.add(false);
		
		tiersOn = new ArrayList<String>();
	}
	
	public void addResource(CloudResource resource) {
		hostedOn.add(resource);
	}
	
	public void setHourlyCost(double hourlyCost) {
		double[] hourlyCosts = new double[24];
		for (int i = 0; i < hourlyCosts.length; ++i)
			hourlyCosts[i] = hourlyCost;
		
		setHourlyCosts(hourlyCosts);
		
	}
	
//	public void setHourlyCosts(double[] hourlyCosts) {
//		for (CloudResource cr : hostedOn)
//			setHourlyCosts(cr, hourlyCosts);
//	}
	
	private void setHourlyCosts(/*CloudResource cr, */double[] hourlyCosts) {
		EMF emf = new EMF();
		CloudFactory cf = emf.getCloudFactory();
		energyCost = cf.createCostProfile();
		
		energyCost.setId((int)(Math.random()*10000));
		energyCost.setDescription("Private Host");
//		energyCost.setAssociatedToCloudElement(cr);
		
		int startingId = (int)(Math.random()*10000);
		
		for (int i = 0; i < hourlyCosts.length; ++i) {
			Cost cost = cf.createCost();
//			cost.setAssociatedToCloudElement(cr);
			cost.setId(startingId++);
			cost.setDescription("Energy Cost");
			cost.setUnit(CostUnitType.PER_HOUR);
			cost.setValue(hourlyCosts[i]);
			cost.setPeriod(i);
			cost.setDefinedOn(null);
			cost.setLowerBound(-1);
			cost.setUpperBound(-1);
			energyCost.getComposedOf().add(cost);
		}
	}
	
	public static List<Host> getFromFile(File f) {
		List<Host> hosts = new ArrayList<Host>();
		
		Properties prop = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
			prop.load(fis);
		} catch (Exception e) {
			return hosts;
		}
		
		boolean goOn = true;
		int i = 0;
		
		while (goOn) {
			try {
				String name = prop.getProperty(i + "name");
				int cpu_cores = Integer.parseInt(prop.getProperty(i + "cpu_cores"));
				double cpu_speed = Double.parseDouble(prop.getProperty(i + "cpu_speed"));
				int ram = Integer.parseInt(prop.getProperty(i + "ram"));
				int storage = Integer.parseInt(prop.getProperty(i + "storage"));
				double density = Double.parseDouble(prop.getProperty(i + "density"));
				
				double hourlyCosts[] = new double[24];
				for (int h = 0; h < hourlyCosts.length; ++h) {
					hourlyCosts[h] = Double.parseDouble(prop.getProperty(i + "hourlyCost-" + h));
				}
				Host host = new Host(name, cpu_cores, cpu_speed, ram, storage, density, hourlyCosts);
				hosts.add(host);
				
				i++;
			} catch (Exception e) {
				goOn = false;
			}
		}
		
		try {
			fis.close();
		} catch (IOException e) { }
		return hosts;
	}
	
	public static void writeToFile(File f, List<Host> hosts) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f);
		} catch (Exception e) {
			return;
		}
		Properties prop = new Properties();
		
		for (int i = 0; i < hosts.size(); ++i) {
			Host host = hosts.get(i);
			
			prop.put(i + "name", host.name);
			prop.put(i + "cpu_cores", Integer.toString(host.cpu_cores));
			prop.put(i + "cpu_speed", Double.toString(host.cpu_speed));
			prop.put(i + "ram", Integer.toString(host.ram));
			prop.put(i + "storage", Integer.toString(host.storage));
			prop.put(i + "density", Double.toString(host.density));
			
			List<Cost> costs = host.energyCost.getComposedOf();
			int h = 0;
			for (; h < 24 && i < costs.size(); ++h) {
				Cost c = costs.get(i);
				prop.put(i + "hourlyCost-" + h, Double.toString(c.getValue()));
			}
			for (; h < 24; ++h)
				prop.put(i + "hourlyCost-" + h, Double.toString(DEFAULT_HOURLY_COST));
		}
		
		try {
			prop.store(fos, "Private Cloud configuration properties");
			fos.flush();
			fos.close();
		} catch (IOException e) { }
	}
	
}
