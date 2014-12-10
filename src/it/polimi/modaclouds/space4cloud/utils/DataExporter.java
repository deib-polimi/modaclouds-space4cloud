package it.polimi.modaclouds.space4cloud.utils;

import it.polimi.modaclouds.space4cloud.db.DatabaseConnector;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DataExporter {
	
	public static final int DEFAULT_T = 24;
//	public static final int DEFAULT_C = 6;
	
	private static final Logger logger = LoggerFactory.getLogger(DataExporter.class);
	private Path sourcesBasePath;
	
	public static List<File> perform(Path sourcesBasePath) {
		DataExporter data = new DataExporter(sourcesBasePath);
		return data.exportAll();
	}
	
	public static List<File> perform(Path sourcesBasePath, int size) {
		DataExporter data = new DataExporter(sourcesBasePath);
		return data.export(size);
	}
	
	public DataExporter(Path sourcesBasePath) {
		this.sourcesBasePath = sourcesBasePath;
	}
	
	public List<File> exportAll() {
		List<File> res = new ArrayList<File>();
		
		for (int size = Configuration.ROBUSTNESS_PEAK_FROM; size <= Configuration.ROBUSTNESS_PEAK_TO; size += Configuration.ROBUSTNESS_STEP_SIZE)
			res.addAll(export(size));
		
		return res;
	}
	
	public List<File> export(int size) {
		if (Configuration.ROBUSTNESS_VARIABILITY <= 0)
			return new ArrayList<File>();
		
		File nominal = Paths.get(sourcesBasePath.toString(), Configuration.SOLUTION_FILE_NAME + "-" + size + Configuration.SOLUTION_FILE_EXTENSION).toFile();
		File lower = Paths.get(sourcesBasePath.toString(), Configuration.SOLUTION_FILE_NAME + "-" + (size / 100 * (100 - Configuration.ROBUSTNESS_VARIABILITY)) + Configuration.SOLUTION_FILE_EXTENSION).toFile();
		File upper = Paths.get(sourcesBasePath.toString(), Configuration.SOLUTION_FILE_NAME + "-" + (size / 100 * (100 + Configuration.ROBUSTNESS_VARIABILITY)) + Configuration.SOLUTION_FILE_EXTENSION).toFile();
			
		if (nominal.exists() && lower.exists() && upper.exists())
			return export(nominal, lower, upper, size);
		
		return new ArrayList<File>();
	}
	
	private List<File> export(File nominal, File lower, File upper, int nominalSize) {
		List<File> res = new ArrayList<File>();
		
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
		otherSymbols.setDecimalSeparator('.');
		DecimalFormat form = new DecimalFormat("0.#####", otherSymbols);
		
		Map<String, Integer[]> nominalReplicasMap = getReplicas(nominal);
		Map<String, Integer[]> lowerReplicasMap = getReplicas(lower);
		Map<String, Integer[]> upperReplicasMap = getReplicas(upper);
		
		ResourceEnvironmentExtensionParser resourceEnvParser = null;
	
			try {
				resourceEnvParser = new ResourceEnvironmentExtensionParser();
			} catch (ResourceEnvironmentLoadingException e) {
				logger.error("Error exporting the solution",e);
			}
	
		
		List<String> totalKeyset = new ArrayList<String>(nominalReplicasMap.keySet());
		for (String s : lowerReplicasMap.keySet())
			if (!totalKeyset.contains(s))
				totalKeyset.add(s);
		for (String s : upperReplicasMap.keySet())
			if (!totalKeyset.contains(s))
				totalKeyset.add(s);
		
		for (String key : totalKeyset) {
			Integer[] nominalReplicas = nominalReplicasMap.get(key);
			Integer[] lowerReplicas = lowerReplicasMap.get(key);
			Integer[] upperReplicas = upperReplicasMap.get(key);
			
			if (nominalReplicas == null) {
				nominalReplicas = new Integer[24];
				for (int i = 0; i < nominalReplicas.length; ++i)
					nominalReplicas[i] = 0;
			}
			if (lowerReplicas == null) {
				lowerReplicas = new Integer[24];
				for (int i = 0; i < lowerReplicas.length; ++i)
					lowerReplicas[i] = 0;
			}
			if (upperReplicas == null) {
				upperReplicas = new Integer[24];
				for (int i = 0; i < upperReplicas.length; ++i)
					upperReplicas[i] = 0;
			}
			
			try {
//				StringWriter sw = new StringWriter();
//				PrintWriter out = new PrintWriter(sw);
				
				String resource = null;
				String provider = null;
				String region = null;
				
				{
					String[] ss = key.split("@");
					resource = ss[0];
					provider = ss[1];
					region = resourceEnvParser.getRegion(provider);
				}
				
				// For now we can only handle Amazon :(
				if (!provider.equals("Amazon"))
					continue;
				

				Path path = null;
				if (totalKeyset.size() > 1)
					path = Paths.get(sourcesBasePath.toString(), "costs-" + nominalSize +  "-" + (resource.replace('.', '_')).replaceAll("_", "") + ".txt");
				else
					path = Paths.get(sourcesBasePath.toString(), "costs-" + nominalSize + ".txt");
				
				PrintWriter out = new PrintWriter(new FileWriter(path.toFile()));
				
//				System.out.println("============================\n" + path.toString() + "\n============================");
				
				Map<String, Map<String, Double>> costs = getCosts(provider, resource, region);
				
				out.printf("T %d\r\n", DEFAULT_T);
				out.printf("C %d\r\n", costs.size() - 2);
				out.printf("Q %s\r\n", form.format(Configuration.ROBUSTNESS_Q));
				out.printf("G %d\r\n", Configuration.ROBUSTNESS_G);
				
				double onDemand = costs.get("On-Demand").get("hourly"); // 0.154;
				
				out.printf("D %s\r\n", form.format(onDemand));
				out.printf("H %d\r\n", Configuration.ROBUSTNESS_H);
				
				out.println("\r\nN");
				
				for (int i = 0; i < DEFAULT_T; ++i) {
					int diffA = Math.abs(nominalReplicas[i] - lowerReplicas[i]);
					int diffB = Math.abs(nominalReplicas[i] - upperReplicas[i]);
					
					out.printf("%d %d\r\n", nominalReplicas[i], diffA > diffB ? diffA : diffB);
				}
				
				out.println("\r\nS");
				
				double spot = costs.get("Spot").get("hourly");
				
				for (int i = 0; i < DEFAULT_T; ++i)
					out.printf("%s\r\n", form.format(spot));
				
				out.println("\r\nF");
				
				for (String s : costs.keySet()) {
					if (s.indexOf("Reserved") == -1)
						continue;
					
					out.printf("%s\r\n", form.format(costs.get(s).get("initial")));
				}
				
				out.println("\r\nR");
				
				for (String s : costs.keySet()) {
					if (s.indexOf("Reserved") == -1)
						continue;
					
					out.printf("%s\r\n", form.format(costs.get(s).get("hourly")));
				}
				
				out.flush();
				out.close();
				
				res.add(path.toFile());
//				System.out.println(sw.toString());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
		}
		
		return res;
	}
	
	private static Map<String, Map<String, Map<String, Double>>> costs = new HashMap<String, Map<String, Map<String, Double>>>();
	
	private Map<String, Map<String, Double>> getCosts(String provider, String resource, String region) {
		if (DataExporter.costs.containsKey(resource + "@" + provider)) {
			return DataExporter.costs.get(resource + "@" + provider);
		}
		
		Map<String, Map<String, Double>> costs = new LinkedHashMap<String, Map<String, Double>>();
		
		if (resource == null)
			return costs;
		
		String query =
				"SELECT 'On-Demand' as 'contract type', AVG(value) as 'cost', unit, 0 as 'initial cost'\n" +
				"FROM cost WHERE description LIKE 'On-Demand XXXXXXXXXX%' AND region = 'YYYYYYYYYY'\n" +
				"UNION\n" +
				"SELECT 'Spot', AVG(value), unit, 0\n" +
				"FROM cost WHERE description LIKE 'Spot ZZZZZZZZZZ%'\n" +
				"UNION\n" +
				"SELECT 'Reserved 1year light', AVG(value), unit,\n" +
				"(SELECT AVG(value) FROM cost WHERE description LIKE 'Reserved 1year light XXXXXXXXXX%' AND region = 'YYYYYYYYYY')\n" +
				"FROM cost WHERE description LIKE 'Reserved light XXXXXXXXXX%' AND region = 'YYYYYYYYYY' \n" +
				"AND value IN (SELECT MAX(value) FROM cost WHERE description LIKE 'Reserved light XXXXXXXXXX%' AND region = 'YYYYYYYYYY' GROUP BY description)\n" +
				"UNION\n" +
				"SELECT 'Reserved 3year light', AVG(value), unit,\n" +
				"(SELECT AVG(value) FROM cost WHERE description LIKE 'Reserved 3year light XXXXXXXXXX%' AND region = 'YYYYYYYYYY')\n" +
				"FROM cost WHERE description LIKE 'Reserved light XXXXXXXXXX%' AND region = 'YYYYYYYYYY' \n" +
				"AND value IN (SELECT MIN(value) FROM cost WHERE description LIKE 'Reserved light XXXXXXXXXX%' AND region = 'YYYYYYYYYY' GROUP BY description)\n" +
				"UNION\n" +
				"SELECT 'Reserved 1year medium', AVG(value), unit,\n" +
				"(SELECT AVG(value) FROM cost WHERE description LIKE 'Reserved 1year medium XXXXXXXXXX%' AND region = 'YYYYYYYYYY')\n" +
				"FROM cost WHERE description LIKE 'Reserved medium XXXXXXXXXX%' AND region = 'YYYYYYYYYY' \n" +
				"AND value IN (SELECT MAX(value) FROM cost WHERE description LIKE 'Reserved medium XXXXXXXXXX%' AND region = 'YYYYYYYYYY' GROUP BY description)\n" +
				"UNION\n" +
				"SELECT 'Reserved 3year medium', AVG(value), unit,\n" +
				"(SELECT AVG(value) FROM cost WHERE description LIKE 'Reserved 3year medium XXXXXXXXXX%' AND region = 'YYYYYYYYYY')\n" +
				"FROM cost WHERE description LIKE 'Reserved medium XXXXXXXXXX%' AND region = 'YYYYYYYYYY' \n" +
				"AND value IN (SELECT MIN(value) FROM cost WHERE description LIKE 'Reserved medium XXXXXXXXXX%' AND region = 'YYYYYYYYYY' GROUP BY description)\n" +
				"UNION\n" +
				"SELECT 'Reserved 1year heavy', AVG(value), unit,\n" +
				"(SELECT AVG(value) FROM cost WHERE description LIKE 'Reserved 1year heavy XXXXXXXXXX%' AND region = 'YYYYYYYYYY')\n" +
				"FROM cost WHERE description LIKE 'Reserved heavy XXXXXXXXXX%' AND region = 'YYYYYYYYYY' \n" +
				"AND value IN (SELECT MAX(value) FROM cost WHERE description LIKE 'Reserved heavy XXXXXXXXXX%' AND region = 'YYYYYYYYYY' GROUP BY description)\n" +
				"UNION\n" +
				"SELECT 'Reserved 3year heavy', AVG(value), unit,\n" +
				"(SELECT AVG(value) FROM cost WHERE description LIKE 'Reserved 3year heavy XXXXXXXXXX%' AND region = 'YYYYYYYYYY')\n" +
				"FROM cost WHERE description LIKE 'Reserved heavy XXXXXXXXXX%' AND region = 'YYYYYYYYYY' \n" +
				"AND value IN (SELECT MIN(value) FROM cost WHERE description LIKE 'Reserved heavy XXXXXXXXXX%' AND region = 'YYYYYYYYYY' GROUP BY description);";
		
		String resourceBis = "Medium";
		if (resource.indexOf("xlarge") > -1)
			resourceBis = "Extra Large";
		else if (resource.indexOf("large") > -1)
			resourceBis = "Large";
		else if (resource.indexOf("medium") > -1)
			resourceBis = "Medium";
		else if (resource.indexOf("small") > -1)
			resourceBis = "Small";
		else if (resource.indexOf("micro") > -1)
			resourceBis = "Micro";
		
		query = query.replaceAll("XXXXXXXXXX", resource).replaceAll("ZZZZZZZZZZ", resourceBis);
		
		if (region == null)
			query = query.replaceAll(" AND region = 'YYYYYYYYYY'", "");
		else
			query = query.replaceAll("YYYYYYYYYY", region);
		
		try {
			Connection db = DatabaseConnector.getConnection();
			ResultSet rs = db.createStatement().executeQuery(query);
			
			while (rs.next()) {
				String contract = rs.getString(1);
				Double cost = rs.getDouble(2);
				Double initialCost = rs.getDouble(4);
				
				Map<String, Double> costInstance = new HashMap<String, Double>();
				costInstance.put("hourly", cost);
				costInstance.put("initial", initialCost);
				
				costs.put(contract, costInstance);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			costs.clear();
		}
		
		DataExporter.costs.put(resource + "@" + provider, costs);
		
		return costs;
	}
	
	private Map<String, Integer[]> getReplicas(File solution) {
		Map<String, Integer[]> replicasMap = new HashMap<String, Integer[]>();
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(solution);
			doc.getDocumentElement().normalize();

			NodeList tiers = doc.getElementsByTagName("Tier");

			for (int i = 0; i < tiers.getLength(); ++i) {
				Node n = tiers.item(i);

				if (n.getNodeType() != Node.ELEMENT_NODE)
					continue;

				Element tier = (Element) n;
				String provider = tier.getAttribute("providerName");
				String resourceName = tier.getAttribute("resourceName");

				NodeList hourAllocations = tier
						.getElementsByTagName("HourAllocation");
				
				Integer replicas[] = replicasMap.get(resourceName + "@" + provider);
				if (replicas == null) {
					replicas = new Integer[24];
					for (int h = 0; h < replicas.length; ++h)
						replicas[h] = 0;
				}

				for (int j = 0; j < hourAllocations.getLength(); ++j) {
					Node m = hourAllocations.item(j);

					if (m.getNodeType() != Node.ELEMENT_NODE)
						continue;

					Element hourAllocation = (Element) m;
					int hour = Integer.parseInt(hourAllocation
							.getAttribute("hour"));
					int allocation = Integer.parseInt(hourAllocation
							.getAttribute("allocation"));
					
					replicas[hour] += allocation;
				}
				
				replicasMap.put(resourceName + "@" + provider, replicas);
			}
		} catch (Exception e) {
			e.printStackTrace();
			replicasMap.clear();
		}
		
		return replicasMap;
	}
}
