package bsa_analyser.github.io;

import java.io.File;
import java.util.LinkedList;

import javax.swing.JOptionPane;

/**
 * This class deals solely with reading in and selecting the correct files for
 * the program. The global variables stored here for every new analysis include
 * one fasta file representing the reference genome, ~two vcf representing the
 * parents, ~four vcfs representing the child bulk populations, and the gff
 * annotation file.
 * 
 * Last updated 3-2-2020
 * 
 * @author Kathryn Kananen
 *
 */
public class Reader {

	private static String fastaFile;

	public Reader() {
		
	}
	/**
	 * Sorts through a list of user given files and finds the fasta files. Since
	 * only one file is permitted per analysis, the user is given the opportunity to
	 * correct a multiple file upload mistake and choose the correct file. The file
	 * selected is then assigned to a global String variable. This method invokes a
	 * helper method, fastaReaderHelper(File[] givenFasta). If no files are selected
	 * of type fasta, then the global fasta variable is set to be "".
	 * 
	 * @param givenFasta a File[] containing various file types the user selects
	 */
	private static void fastaReader(File[] givenFasta) {
		if (givenFasta.length != 0) {
			// 1. Check if only one fasta file has been loaded and if not then ask the user
			// which fasta file they want to choose via a JOptionPane dialog box.
			// 2. Gather the fasta files names in a list
			LinkedList<String> fastaLST = new LinkedList<String>();
			// If the list of fasta files selected if larger than 1 then make the user
			// chose, else then take the only file
			for (int i = 0; i < givenFasta.length; i++) {
				if (givenFasta[i].getName().endsWith(".fasta") || givenFasta[i].getName().endsWith(".fa")) {
					// Need to implement panel that selects which file is the correct out of
					// fasta selected.
					fastaLST.add(givenFasta[i].getName());
				}
			}

			// 3. If more than 1 fasta was uploaded the user must chose which fasta they
			// meant to chose for the analysis they are running a the moment.
			if (fastaLST.size() > 1) {
				String selectedFile = "";
				// Continually ask the user for the correct file until they provide one or exit.
				while (!fastaLST.contains(selectedFile)) {
					selectedFile = JOptionPane.showInputDialog(String.valueOf(fastaLST.size())
							+ " fasta files given instead of 1.\n " + fastaReaderHelper(givenFasta)
							+ "\nPlease type the reference genome file you want for the analysis below");
					// If user hits cancel or ok on an empty string is set as the fastaFile and the
					// loop is exited.
					if (selectedFile == null) {
						selectedFile = givenFasta[0].getName();

						fastaFile = "";
					} else {
						fastaFile = selectedFile;
					}
				}
			} else if (fastaLST.size() == 1) {
				// Get the only file in the list
				fastaFile = fastaLST.get(0);
			} else {
				fastaFile = "";
			}
		}
	}

	/**
	 * Helper method for fastaReader(File[] givenFasta) finds the names for each
	 * fasta and returns them as a string that is ready for display on the
	 * JOptionPane.
	 * 
	 * @param givenFasta
	 * @return String representing names of all fasta files.
	 */
	private static String fastaReaderHelper(File[] givenFasta) {
		String out = "";
		if (givenFasta.length != 0) {
			for (int i = 0; i < givenFasta.length; i++) {
				if (givenFasta[i].getName().endsWith(".fasta") || givenFasta[i].getName().endsWith(".fa")) {
					if (!(out.equals(""))) {
						out = out + " " + givenFasta[i].getName();
					} else {
						out = givenFasta[i].getName();
					}
				}
			}
		}
		return out;
	}
	
	

	/**
	 * The getter method for the fastaFile selected by the user.
	 * 
	 * @return fasta file selected by user.
	 */
	public static String getFastaFile() {
		return fastaFile;
	}

	/**
	 * The setter method for the fastaFile. Sets the variable for the fasta file.
	 * 
	 * @param fastaFile
	 */
	public static void setFastaFile(String fastaFile) {
		Reader.fastaFile = fastaFile;
	}

}