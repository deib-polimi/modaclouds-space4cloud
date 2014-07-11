package it.polimi.modaclouds.space4cloud.gui;

import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.utils.Configuration;

import java.io.File;
import java.net.MalformedURLException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class FileLoader {

	private static final Logger logger = LoggerFactory.getLogger(FileLoader.class);	

	public static  <T> File loadExtensionFile(String title, Class<T> clazz) {
		JFileChooser fileChooser;
		File file=null;
		if(Configuration.PROJECT_BASE_FOLDER!= null)
			fileChooser = new JFileChooser(Configuration.PROJECT_BASE_FOLDER);
		else
			fileChooser = new JFileChooser();

		FileNameExtensionFilter filter = new FileNameExtensionFilter("XML files", "xml");
		fileChooser.setFileFilter(filter);
		fileChooser.setDialogTitle(title);
		// keep asking the file until a valid one is provided or the user
		// presses cancel
		boolean cancelled = false;
		boolean validSelection = false;
		while(!cancelled && !validSelection){
			int returnVal = fileChooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fileChooser.getSelectedFile();
				//save the base directory
				if(Configuration.PROJECT_BASE_FOLDER==null)
					Configuration.PROJECT_BASE_FOLDER=file.getParent();
				try{
					T deserializtion = XMLHelper.deserialize(file.toURI().toURL(), clazz);
					if(deserializtion == null){
						logger.error("Deserialization of file ("+file+") did not succeded.");
						file=null;
					}else{
						validSelection = true;
					}
				}catch(JAXBException | SAXException | MalformedURLException e) {
					logger.error("The specified file (" + file+ ") is not valid ", e);
					file = null;					
				}				
				//check validity of the file with respect to the expected class
			}else if(returnVal == JFileChooser.CANCEL_OPTION){
				cancelled = true;
			}
		}
						
		if (cancelled)
			return null;

		if(Configuration.PROJECT_BASE_FOLDER==null)
			Configuration.PROJECT_BASE_FOLDER=file.getParent();


		return file;
	}


	public static  File loadFile(String title){
		JFileChooser fileChooser;
		File file=null;
		if(Configuration.PROJECT_BASE_FOLDER!= null)
			fileChooser = new JFileChooser(Configuration.PROJECT_BASE_FOLDER);
		else
			fileChooser = new JFileChooser();

		fileChooser.setDialogTitle(title);
		int returnVal = fileChooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			if(Configuration.PROJECT_BASE_FOLDER==null)
				Configuration.PROJECT_BASE_FOLDER=file.getParent();
		}
		return file;
	}
	

	public static File saveFile(String title) {
		JFileChooser fileChooser;
		File file=null;
		if(Configuration.PROJECT_BASE_FOLDER!= null)
			fileChooser = new JFileChooser(Configuration.PROJECT_BASE_FOLDER);
		else
			fileChooser = new JFileChooser();

		fileChooser.setDialogTitle(title);
		int returnVal = fileChooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			if(Configuration.PROJECT_BASE_FOLDER==null)
				Configuration.PROJECT_BASE_FOLDER=file.getParent();
		}
		return file;
	}



}
