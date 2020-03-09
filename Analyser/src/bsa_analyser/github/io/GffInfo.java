package bsa_analyser.github.io;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

/**
 * Stores information of the gff file/files and stores them into a linkedList.
 * This class assumes that the file has been validated previously and holds true
 * to standard gff/gff3 format. As such no validation checks are done here, only
 * information storage.
 * 
 * @author Kathryn Kananen
 *
 */
public class GffInfo {
	private static ArrayList<String> gffsLST;

	public GffInfo(ArrayList<String> givenGffs) {
		gffsLST = givenGffs;
	}

	/**
	 * Gets the information from a desired gff file and stores it into a list.
	 * 
	 * @param givenGff a gff file
	 * @return LinkedList containing the file information
	 */
	public static ArrayList<String> getFileInfo(String givenGff) {
		ArrayList<String> out = new ArrayList<String>();
		// 1. Make a BufferedReader for each file.
		if (givenGff == null) {
			return out;
		} else if (givenGff.length() != 0) {
			BufferedReader br = InfoPrep(givenGff);
			try {
				String line = br.readLine();

				while (line != null) {
					out.add(line);
					line = br.readLine();
				}
				return out;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e);
			}
		}
		return out;
	}

	/**
	 * Makes a buffered reader for for a given gff file.
	 * 
	 * @param givenVcf a given gff file called by a class method
	 * @return a BufferedReader of the givenVcf file
	 */
	private static BufferedReader InfoPrep(String givenGff) {
		if (givenGff == null) {
			return null;
		} else if (givenGff.length() != 0) {
			try {
				FileReader fr = new FileReader(givenGff);
				BufferedReader br = new BufferedReader(fr);
				return br;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e);
			}
		}
		return null;
	}

	/**
	 * The getter method for the gff Files.
	 * 
	 * @return List of gff files.
	 */
	public static ArrayList<String> getGffLST() {
		return gffsLST;
	}

	/**
	 * The setter method for the gffs Files.
	 * 
	 * @param gffsLST
	 */
	public static void setVcfsLST(ArrayList<String> gffsLST) {
		GffInfo.gffsLST = gffsLST;
	}
}
