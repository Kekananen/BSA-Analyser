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
	private static LinkedList<String> vcfsParLST, vcfschildLST;
	// Allow for multiple gffs for if the user wants to see how multiple annotations
	// are implemented from their NGS pipeline or another's.
	private static LinkedList<String> gffLST;

	public Reader(File[] givenFasta) {
		fastaReader(givenFasta);
		vcfsReader(givenFasta);
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
	 * Sorts through a list of user given files and finds the vcfs files. The
	 * parents are found by asking the user to specify which two files they are and
	 * the children (F2) are found by taking the remaining files by default. files
	 * are selected of type vcf, then the global vcf variable is set to be set to be
	 * empty. If the parents are specified by not enough files are remaining for the
	 * children, then another prompt appears asking the user to reselect their files
	 * as not enough vcf were given.
	 * 
	 * @param givenVcfs a File[] containing various file types the user selects
	 */
	private static void vcfsReader(File[] givenVcfs) {
		if (givenVcfs.length != 0) {
			// 1. Gather the vcf files names in a list
			LinkedList<String> vcfsLST = new LinkedList<String>();
			for (int i = 0; i < givenVcfs.length; i++) {
				if (givenVcfs[i].getName().endsWith(".vcf")) {
					// Need to implement panel that selects which file is the correct out of
					// fasta selected.
					vcfsLST.add(givenVcfs[i].getName());
				}
			}

			// 2. Check which files are the parent by asking the user to specify. By default
			// the remaining ones are treated as children of the parents.
			if (vcfsLST.size() > 3) {
				// Holds the intersect of all the vcf files and the ones specified by the user
				// as the parents which allows for them to be validated in the loop.
				LinkedList<String> intersect = new LinkedList<String>();
				while (intersect.size() != 2) {
					String selectedFile = JOptionPane.showInputDialog(String.valueOf(vcfsLST.size())
							+ " Select the parents (if any) out of the following files\n " + vcfsReaderHelper(givenVcfs)
							+ "\n enter the files below seperated by a space i.e (p1.vcf p2.vcf)");

					// If user hits cancel or ok on an empty string is set as the fastaFile and the
					// loop is exited.
					if (selectedFile == null) {
						// Add dummy values to the intersect to end loop
						intersect.add("hit1");
						intersect.add("hit2");

						vcfsParLST = new LinkedList<String>();
					} else {
						// Set the global list for parents to be empty so values can be added fresh.
						vcfsParLST = new LinkedList<String>();

						LinkedList<String> selectedVcfs = new LinkedList<String>();
						String[] vcfs = selectedFile.split(" ");

						for (int i = 0; i < vcfs.length; i++) {
							selectedVcfs.add(vcfs[i]);
							vcfsParLST.add(vcfs[i]);
						}

						// Find the intersect of the vcf lists.
						intersect = new LinkedList<String>(selectedVcfs);
						intersect.retainAll(vcfsLST);
					}
				}
			} else {
				JOptionPane.showMessageDialog(null, "not enough vcf files were given to account "
						+ "for both the children and parents. Please supply at least three vcf files");
				vcfsParLST = new LinkedList<String>();
				vcfschildLST = new LinkedList<String>();
			} 
		}
	}

	/**
	 * Helper method for vcfsReader(File[] givenVcfs) finds the names for each vcf
	 * and returns them as a string that is ready for display on the JOptionPane.
	 * 
	 * @param givenVcfs
	 * @return String representing names of all vcf files.
	 */
	private static String vcfsReaderHelper(File[] givenvcfs) {
		String out = "";
		if (givenvcfs.length != 0) {
			for (int i = 0; i < givenvcfs.length; i++) {
				if (givenvcfs[i].getName().endsWith(".vcf")) {
					if (!(out.equals(""))) {
						out = out + " " + givenvcfs[i].getName();
					} else {
						out = givenvcfs[i].getName();
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