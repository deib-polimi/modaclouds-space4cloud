package it.polimi.modaclouds.space4cloud.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


public class ConfigurationHandler {


	protected Properties conf;
	protected Path confFilePath;
	protected Constants c;

	public ConfigurationHandler(Path filePath) {

		conf = new Properties();
		confFilePath = filePath;
		c = Constants.getInstance();
	}

	public void saveConfiguration(){

		conf.setProperty("ALLOCATION_MODEL", c.ALLOCATION_MODEL);
		conf.setProperty("ABSOLUTE_WORKING_DIRECTORY", c.ABSOLUTE_WORKING_DIRECTORY);
		conf.setProperty("LAUNCH_CONFIG", c.LAUNCH_CONFIG);
		conf.setProperty("PERFORMANCE_RESULTS_FOLDER", c.PERFORMANCE_RESULTS_FOLDER);
		conf.setProperty("PROJECT_NAME", c.PROJECT_NAME);
		conf.setProperty("PROJECT_PATH", c.PROJECT_PATH);
		conf.setProperty("REL_WORKING_DIRECTORY", c.REL_WORKING_DIRECTORY);
		conf.setProperty("RESOURCE_MODEL", c.RESOURCE_MODEL);
		conf.setProperty("WORKING_DIRECTORY", c.WORKING_DIRECTORY);
		conf.setProperty("SOLVER", c.SOLVER);
		conf.setProperty("LINE_PROPERTIES", c.LINE_PROPERTIES_FILE);
		conf.setProperty("REPOSITORY_MODEL", c.REPOSITORY_MODEL);
		conf.setProperty("USAGE_MODEL", c.USAGE_MODEL);

		File confFile = confFilePath.toFile();
		try{
			Path workingDir = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY);
			if(!Files.exists(workingDir))
				Files.createDirectories(workingDir);
			confFile.createNewFile();

			conf.store(new FileOutputStream(confFilePath.toString()), null);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void loadConfiguration(){

		try {
			conf.load(new FileInputStream(confFilePath.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		c.ALLOCATION_MODEL= conf.getProperty("ALLOCATION_MODEL");
		c.ABSOLUTE_WORKING_DIRECTORY= conf.getProperty("ABSOLUTE_WORKING_DIRECTORY");
		c.LAUNCH_CONFIG= conf.getProperty("LAUNCH_CONFIG");
		c.PERFORMANCE_RESULTS_FOLDER= conf.getProperty("PERFORMANCE_RESULTS_FOLDER");
		c.PROJECT_NAME= conf.getProperty("PROJECT_NAME");
		c.PROJECT_PATH= conf.getProperty("PROJECT_PATH");
		c.REL_WORKING_DIRECTORY= conf.getProperty("REL_WORKING_DIRECTORY");
		c.RESOURCE_MODEL= conf.getProperty("RESOURCE_MODEL");
		c.USAGE_MODEL= conf.getProperty("USAGE_MODEL");
		c.WORKING_DIRECTORY= conf.getProperty("WORKING_DIRECTORY");
		c.SOLVER= conf.getProperty("SOLVER");
		c.LINE_PROPERTIES_FILE= conf.getProperty("LINE_PROPERTIES");
		c.REPOSITORY_MODEL = conf.getProperty("REPOSITORY_MODEL");
		

	}

	public static void cleanFolders(String path) {	
		File directory = new File(path);
		if(directory.exists())
			deleteDirectory(directory);		
	}


	/**
	 * Cleans the folders that store lqn files and results. leaves only the latest lqn file
	 * @param path
	 */
	public void removeOldLQNFiles() {	

		File perfFolder = new File( c.ABSOLUTE_WORKING_DIRECTORY+c.PERFORMANCE_RESULTS_FOLDER);
		if(perfFolder.exists())
			for(File folder:perfFolder.listFiles()){
				if(folder.exists() && folder.isDirectory()){
					//Delete all non xml files or xml files that are result of LINE (_res.xml)  
					for(File f:folder.listFiles())
						if(f.exists() && !f.getName().endsWith(".xml") || f.getName().endsWith("_res.xml"))
							f.delete();
						else if(f.isDirectory()){
							for(File f1:f.listFiles())
								f1.delete();
							f.delete();
						}

					//Now we should have only lqn model files in the directory
					for(int i=0;i<folder.listFiles().length-1;i++)
						folder.listFiles()[i].delete();

				}
			}


	}


	public static boolean deleteDirectory(File directory) {
		if(directory.exists()){
			File[] files = directory.listFiles();
			if(null!=files){
				for(int i=0; i<files.length; i++) {
					if(files[i].isDirectory()) {
						deleteDirectory(files[i]);
					}
					else {
						files[i].delete();
					}
				}
			}
		}
		return(directory.delete());
	}
}
