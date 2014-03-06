/*
 * 
 */
package it.polimi.modaclouds.space4cloud.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

// TODO: Auto-generated Javadoc
/**
 * Provides an helper method to copy files.
 * 
 * @author Davide Franceschelli
 * 
 */
public class CopyFile {

	/**
	 * Instantiates a new copy file.
	 */
	private CopyFile() {
	}

	/**
	 * Makes a file copy.
	 * 
	 * @param f1
	 *            is the File to copy.
	 * @param f2
	 *            is the new copied File.
	 */
	public static void copy(File f1, File f2) {
		try {
			InputStream in = new FileInputStream(f1);
			OutputStream out = new FileOutputStream(f2, true);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			System.out.println("File copied.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
