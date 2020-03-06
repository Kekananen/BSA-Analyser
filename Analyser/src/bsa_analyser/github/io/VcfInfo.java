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
	 * @param givenVcf a vcf file
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

	/**
	 * Gets the information from a desired vcf file and stores it into a list.
	 * 
	 * @param givenVcf a vcf file
	 * @return LinkedList containing the file information
	 */
	public static LinkedList<String> getFileInfo(String givenVcf) {
		LinkedList<String> out = new LinkedList<String>();
		// 1. Make a BufferedReader for each file.
		BufferedReader br = InfoPrep(givenVcf);
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
		return out;
	}

	/**
	 * Finds the instertion mutations in the vcf by looking through each line and
	 * only counting the variants that are of a lesser length.
	 * 
	 * @param givenVcf a vcf file
	 * @return Integer representing the number of substitution mutations in the vcf
	 *         file
	 */
	public static int getInsMutCnt(String givenVcf) {
		int cnt = 0;
		// 1. Make a BufferedReader for each file.
		BufferedReader br = InfoPrep(givenVcf);
		try {
			String line = br.readLine();
			while (line != null) {
				// 2. Count all lines that are not containing metadata
				if (line.startsWith("#") == false) {
					String obs = line.split("\t")[3];
					String alt = line.split("\t")[4];

					// If they are the same length then it is a deletion mutation.
					if (obs.length() < alt.length() && !(obs.equals(alt)) && (alt.indexOf(",") == -1)) {
						cnt++;
					}

					// Some of the variants have two possible observations so both must be checked.
					if ((alt.indexOf(",") != -1)) {
						String[] variants = alt.split(",");
						for (int i = 0; i < variants.length; i++) {
							if (obs.length() < variants[i].length() && !(obs.equals(variants[i]))) {
								cnt++;
							}
						}
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
	 * Finds the deletion mutations in the vcf by looking through each line and only
	 * counting the variants that are of a greater length.
	 * 
	 * @param givenVcf a vcf file
	 * @return Integer representing the number of substitution mutations in the vcf
	 *         file
	 */
	public static int getDelMutCnt(String givenVcf) {
		int cnt = 0;
		// 1. Make a BufferedReader for each file.
		BufferedReader br = InfoPrep(givenVcf);
		try {
			String line = br.readLine();
			while (line != null) {
				// 2. Count all lines that are not containing metadata
				if (line.startsWith("#") == false) {
					String obs = line.split("\t")[3];
					String alt = line.split("\t")[4];

					// If they are the same length then it is a deletion mutation.
					if (obs.length() > alt.length() && !(obs.equals(alt)) && (alt.indexOf(",") == -1)) {
						cnt++;
					}

					// Some of the variants have two possible observations so both must be checked.
					if ((alt.indexOf(",") != -1)) {
						String[] variants = alt.split(",");
						for (int i = 0; i < variants.length; i++) {
							if (obs.length() > variants[i].length() && !(obs.equals(variants[i]))) {
								cnt++;
							}
						}
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
	 * Finds the substitution mutations in the vcf by looking through each line and
	 * only counting the variants that are of the same length, and are not made up
	 * of identical bases. This will catch even substitutions greater than length 1
	 * though the likelihood is extremely low.
	 * 
	 * @param givenVcf a vcf file
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
					String obs = line.split("\t")[3];
					String alt = line.split("\t")[4];

					// If they are the same length then it is a substitution mutation.
					if (obs.length() == alt.length() && !(obs.equals(alt)) && (alt.indexOf(",") == -1)) {
						cnt++;
					}

					// Some of the variants have two possible observations so both must be checked.
					if ((alt.indexOf(",") != -1)) {
						String[] variants = alt.split(",");
						for (int i = 0; i < variants.length; i++) {
							if (obs.length() == variants[i].length() && !(obs.equals(variants[i]))) {
								cnt++;
							}
						}
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
