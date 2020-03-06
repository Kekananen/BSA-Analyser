package bsa_analyser.github.io;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JOptionPane;

/**
 * Reads the information of the vcf files and creates some basic statistics
 * about the file such as size and same. This class assumes that files are
 * valid.
 * 
 * Last updated 3-6-20
 * 
 * @author Kathryn Kananen
 *
 */
public class VcfInfo {
	private static LinkedList<String> vcfsLST;

	public VcfInfo(LinkedList<String> linkedList) {
		vcfsLST = linkedList;
	}

	/**
	 * Finds the total number of variants in the file.
	 * 
	 * @param givenVcf
	 * @return an integer representing the size of the file
	 */
	public static int getVariantCnt(String givenVcf) {
		int cnt = 0;
		// 1. Make a BufferedReader for each file.
		BufferedReader br = InfoPrep(givenVcf);
		try {
			String line = br.readLine();
			while (line != null) {
				// 2. Count all lines that are not containing metadata
				if (line.startsWith("#") == false) {
					cnt++;
				}
				line = br.readLine();
			}

			return cnt;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e);
		}

		return 0;
	}

	public static String[] getFileInfo(String givenVcf) {
		// Todo
		return null;
	}

	public static String getInsMutCnt(String givenVcf) {
		// Todo
		return null;
	}

	public static String getDelMutCnt(String givenVcf) {
		// Todo
		return null;
	}

	/**
	 * Finds the substitution mutations in the vcf by looking through each line and
	 * only counting the variants that are of the same length, and are not made up
	 * of identical bases. This will catch even substitutions greater than length 1
	 * though the likelihood is extremely low.
	 * 
	 * AC.raw.vcf WSS1849.raw.vcf
	 * 
	 * @param givenVcf
	 * @return Integer representing the number of substitution mutations in the vcf
	 *         file
	 */
	public static int getSubMutCnt(String givenVcf) {
		int cnt = 0;
		// 1. Make a BufferedReader for each file.
		BufferedReader br = InfoPrep(givenVcf);
		try {
			String line = br.readLine();
			while (line != null) {
				// 2. Count all lines that are not containing metadata
				if (line.startsWith("#") == false) {
					String obs1 = line.split("\t")[3];
					String obs2 = line.split("\t")[4];

					// If they are the same length then it is a substitution mutation.
					if (obs1.length() == obs2.length() && !(obs1.equals(obs2))) {
						System.out.println(line);
						cnt++;
					}
				}
				line = br.readLine();
			}

			return cnt;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e);
		}

		return 0;
	}

	/**
	 * Makes a buffered reader for for a given vcf file.
	 * 
	 * @param givenVcf a given vcf file called by a class method
	 * @return a BufferedReader of the givenVcf file
	 */
	private static BufferedReader InfoPrep(String givenVcf) {
		try {
			FileReader fr = new FileReader(givenVcf);
			BufferedReader br = new BufferedReader(fr);
			return br;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e);
		}
		return null;
	}

	/**
	 * The getter method for the vcf Files.
	 * 
	 * @return List of vcf files.
	 */
	public static LinkedList<String> getVcfsLST() {
		return vcfsLST;
	}

	/**
	 * The setter method for the vcf Files. Sets the variable for the vcf children
	 * files.
	 * 
	 * @param vcfsLST
	 */
	public static void setVcfsLST(LinkedList<String> vcfsLST) {
		VcfInfo.vcfsLST = vcfsLST;
	}

}
