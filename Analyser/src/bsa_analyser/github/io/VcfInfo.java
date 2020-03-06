package bsa_analyser.github.io;

import java.io.BufferedReader;
import java.io.File;
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
	public int getVariantCnt(String givenVcf) {
		int cnt = 0;
		// 1. Make a BufferedReader for each file.
		BufferedReader br = InfoPrep(givenVcf);
		try {
			while (br.readLine() != null) {
				// 2. Count all lines that are not containing metadata
				if (br.readLine().startsWith("#") == false) {
					System.out.println(br.readLine());
					cnt++;
				}
				br.readLine();
			}

			return cnt;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e);
		}

		return 0;
	}

	public String getFileName(String givenVcf) {
		// Todo
		return null;
	}

	public String[] getFileInfo(String givenVcf) {
		// Todo
		return null;
	}
	
	public String getInsMutCnt(String givenVcf) {
		// Todo
		return null;
	}
	
	public String getDelMutCnt(String givenVcf) {
		// Todo
		return null;
	}
	
	public String getSubMutCnt(String givenVcf) {
		// Todo
		return null;
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
