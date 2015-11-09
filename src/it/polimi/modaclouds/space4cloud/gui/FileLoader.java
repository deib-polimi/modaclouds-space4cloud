package it.polimi.modaclouds.space4cloud.gui;

import java.awt.Image;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.osgi.framework.FrameworkUtil;

import it.polimi.modaclouds.space4cloud.utils.Configuration;

public class FileLoader {

	public static <T> File loadExtensionFile(String title, JFrame caller,
			Class<T> clazz) {
		JFileChooser fileChooser;
		File file = null;
		if (Configuration.PROJECT_BASE_FOLDER != null)
			fileChooser = new JFileChooser(Configuration.PROJECT_BASE_FOLDER);
		else
			fileChooser = new JFileChooser();

		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"XML files", "xml");
		fileChooser.setFileFilter(filter);
		fileChooser.setDialogTitle(title);
		// keep asking the file until a valid one is provided or the user
		// presses cancel

		int returnVal = fileChooser.showOpenDialog(caller);
		if (returnVal == JFileChooser.CANCEL_OPTION)
			return null;

		if (returnVal == JFileChooser.APPROVE_OPTION)
			file = fileChooser.getSelectedFile();

		// save the base directory
		if (Configuration.PROJECT_BASE_FOLDER == null)
			Configuration.PROJECT_BASE_FOLDER = file.getParent();

		return file;
	}

	public static File loadFile(String title, JFrame caller, String... ext) {
		JFileChooser fileChooser;
		File file = null;
		if (Configuration.PROJECT_BASE_FOLDER != null)
			fileChooser = new JFileChooser(Configuration.PROJECT_BASE_FOLDER);
		else
			fileChooser = new JFileChooser();

		if (ext != null && ext.length > 0) {
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					ext[0].toUpperCase() + " files", ext);
			fileChooser.setFileFilter(filter);
		}
		fileChooser.setDialogTitle(title);
		int returnVal = fileChooser.showOpenDialog(caller);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			if (Configuration.PROJECT_BASE_FOLDER == null)
				Configuration.PROJECT_BASE_FOLDER = file.getParent();
		}
		return file;
	}

	public static File saveFile(String title, JFrame caller, String... ext) {
		JFileChooser fileChooser;
		File file = null;
		if (Configuration.PROJECT_BASE_FOLDER != null)
			fileChooser = new JFileChooser(Configuration.PROJECT_BASE_FOLDER);
		else
			fileChooser = new JFileChooser();

		if (ext != null && ext.length > 0) {
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					ext[0].toUpperCase() + " files", ext);
			fileChooser.setFileFilter(filter);
		}
		fileChooser.setDialogTitle(title);
		int returnVal = fileChooser.showSaveDialog(caller);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			if (Configuration.PROJECT_BASE_FOLDER == null)
				Configuration.PROJECT_BASE_FOLDER = file.getParent();
		}
		return file;
	}

	public static <T> File loadExtensionFile(String title, Class<T> clazz) {
		return loadExtensionFile(title, frameWithIcon, clazz);
	}

	public static File loadFile(String title, String... ext) {
		return loadFile(title, frameWithIcon, ext);
	}

	public static File saveFile(String title, String... ext) {
		return saveFile(title, frameWithIcon, ext);
	}

	private static JFrame frameWithIcon;
	static {
		frameWithIcon = new JFrame();
		Image favicon = new ImageIcon(FrameworkUtil.getBundle(
				ConfigurationWindow.class).getEntry("icons/Cloud.png"))
				.getImage();
		frameWithIcon.setIconImage(favicon);
	}

}
