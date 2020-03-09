package bsa_analyser.github.io;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

/**
 * Stores information of the fasta file given by the user into a linkedList.
 * This class assumes that the file has been validated previously and holds true
 * to standard fasta format. As such no validation checks are done here, only
 * information storage.
 * 
 * @author Kathryn Kananen
 *
 */
public class FastaInfo {
	private static ArrayList<String> fastaInfoLST;

	public FastaInfo(ArrayList<String> givenFasta) {
		fastaInfoLST = givenFasta;
	}

	/**
	 * Gets the information from a desired fasta file and stores it into a list.
	 * 
	 * @param givenFasta a given fasta file
	 * @return LinkedList containing the file information
	 */
	public static ArrayList<String> getFileInfo(String givenFasta) {
		ArrayList<String> out = new ArrayList<String>();
		if (givenFasta == null) {
			return null;
		} else if (givenFasta.length() != 0) {
			// 1. Make a BufferedReader for each file.
			BufferedReader br = InfoPrep(givenFasta);
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
	 * Makes a buffered reader for a given fasta file.
	 * 
	 * @param givenFasta a given fasta file
	 * @return a BufferedReader of the given fasta file
	 */
	private static BufferedReader InfoPrep(String givenFasta) {
		if (givenFasta == null) {
			return null;
		} else if (givenFasta.length() != 0) {
			try {
				FileReader fr = new FileReader(givenFasta);
				BufferedReader br = new BufferedReader(fr);
				return br;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e);
			}
		}
		return null;
	}

	/**
	 * The getter method for the fasta File.
	 * 
	 * @return List containing the information of the fasta file.
	 */
	public static ArrayList<String> getFastaLST() {
		return fastaInfoLST;
	}

	/**
	 * The setter method for the fasta File. Allows for the setting of the
	 * givenFasta list variable.
	 * 
	 * @param givenFasta a given fasta file
	 */
	public static void setVcfsLST(ArrayList<String> givenFasta) {
		FastaInfo.fastaInfoLST = givenFasta;
	}
}
