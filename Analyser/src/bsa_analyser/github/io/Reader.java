package bsa_analyser.github.io;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class deals solely with reading in and selecting the correct files for
 * the program. The global variables stored here for every new analysis include
 * one fasta file representing the reference genome, ~two vcf representing the
 * parents, ~four vcfs representing the child bulk populations, and the gff
 * annotation file.
 *
 * @author Kathryn Kananen, Tolulope Balogun
 *
 */
public class Reader {

	static String selectedFile = "";

	// public static Files[];
	private static String fastaFile;
	private static ArrayList<String> vcfsParLST, vcfsChildLST;
	// Allow for multiple gffs for if the user wants to see how multiple annotations
	// are implemented from their NGS pipeline or another's.
	private static ArrayList<String> gffLST;

	public Reader(File[] givenFasta) {
		fastaReader(givenFasta);
		vcfsReader(givenFasta);
		gffReader(givenFasta);
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
			// 1. Gather the fasta files names in a parentvcflist
			String[] endings = { ".fasta", "fa" };
			ArrayList<String> fastaLST = fileSorter(givenFasta, endings);

			// 3. If more than 1 fasta was uploaded the user must chose which fasta they
			// meant to chose for the analysis they are running a the moment.
			if (fastaLST.size() > 1) {
				// Continually ask the user for the correct file until they provide one or exit.
				while (!fastaLST.contains(selectedFile)) {
					fileReaderHelper(givenFasta, endings);
					JPanel jp = new JPanel();

					for (String f : fastaLST) {
						jp.add(new JButton(f.trim()));
					}
					// Used button so that the user will be able to click the fasta file of choice
					// instead of typing it, as the user might be prone to error.
					for (int i = 0; i < jp.getComponentCount(); i++) {
						String name = ((JButton) (jp.getComponent(i))).getText();
						((JButton) (jp.getComponent(i))).addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								selectedFile = name;
								System.out.println(selectedFile);
								SwingUtilities.getWindowAncestor(jp).setVisible(false);

							}
						});

					}
					JOptionPane.showOptionDialog(null, jp,
							String.valueOf(fastaLST.size()) + " fasta files given instead of 1. "
									+ "\nPlease type the reference genome file you want for the analysis below",
							JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new Object[] {}, null);

					if (selectedFile == null) {
						selectedFile = givenFasta[0].getName();

						fastaFile = "";
					} else {
						fastaFile = selectedFile;
					}
				}
			} else if (fastaLST.size() == 1) {
				// Get the only file in the parentvcflist
				fastaFile = fastaLST.get(0);
			} else {
				fastaFile = "";
			}
		}
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
	@SuppressWarnings("unchecked")
	private static void vcfsReader(File[] givenVcfs) {
		if (givenVcfs.length != 0) {
			// 1. Gather the vcf files names in a parentvcflist
			String[] endings = { ".vcf" };
			ArrayList<String> vcfsLST = fileSorter(givenVcfs, endings);

			// 2. Check which files are the parent by asking the user to specify. By default
			// the remaining ones are treated as children of the parents.
			if (vcfsLST.size() >= 3) {
				// Holds the intersect of all the vcf files and the ones specified by the user
				// as the parents which allows for them to be validated in the loop.
				ArrayList<String> intersect = new ArrayList<String>();
				// Holds the children which are all files minus the intersect
				ArrayList<String> children = new ArrayList<String>();
				while (intersect.size() != 2) {

					// Used Jlist for the VCF file, so that the user would be able to click and
					// choose the parent VCF, instead of typing.
					ArrayList<String> parentvcflist = new ArrayList<String>();

					String vcfs = "";

					for (String file : vcfsLST) {
						{
							vcfs = file.trim();
						}
						parentvcflist.add(vcfs);
					}

					// 3. Create a Jlist containing all of the user selected vcf files.
					@SuppressWarnings({ "serial", "rawtypes" })
					JList list2 = new JList(parentvcflist.toArray()) {

						@Override
						public Dimension getPreferredScrollableViewportSize() {
							Dimension dim = super.getPreferredScrollableViewportSize();
							dim.width = 35;

							return dim;

						}
					};

					// Holds the selected files.
					List<String> selectedVcfFiles = new ArrayList<String>();

					list2.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

					switch (JOptionPane.showOptionDialog(null, new JScrollPane(list2), "Please choose the parent files",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, 0)) {
					case JOptionPane.OK_OPTION:

						break;

					}

					selectedVcfFiles = list2.getSelectedValuesList();
					intersect.addAll(selectedVcfFiles);

					// If user hits cancel, exit, or ok on an empty string is set as the fastaFile
					// and the loop is exited.
					if ((selectedVcfFiles == null) || (selectedVcfFiles.isEmpty())) {
						// Add dummy values to the intersect to end loop.
						intersect.add("hit1");
						intersect.add("hit2");

						vcfsParLST = new ArrayList<String>();
						vcfsChildLST = new ArrayList<String>();
					} else if(selectedVcfFiles.size() == 2) {
						// Set the global parentvcflist for parents to be empty so values can be added
						// fresh.
						vcfsParLST = new ArrayList<String>();

						ArrayList<String> selectedVcfs = new ArrayList<String>();
						String[] parVcfs = selectedVcfFiles.toArray(new String[selectedVcfFiles.size()]);

						for (int i = 0; i < parVcfs.length; i++) {
							selectedVcfs.add(parVcfs[i]);
							vcfsParLST.add(parVcfs[i]);
						}

						// Find the intersect of the vcf lists.
						intersect = new ArrayList<String>(selectedVcfs);
						intersect.retainAll(vcfsLST);
						// Find the children vcf files.
						children = new ArrayList<String>(vcfsLST);
						children.removeAll(selectedVcfs);

						vcfsChildLST = children;
					} 
				}
			} else {
				JOptionPane.showMessageDialog(null, "not enough vcf files were given to account "
						+ "for both the children and parents. Please supply at least three vcf files");
				vcfsParLST = new ArrayList<String>();
				vcfsChildLST = new ArrayList<String>();
			}
		}
	}

	/**
	 * Sorts through a list of user given files and finds the gff or gff3 files. The
	 * files are added to the global variable gffLST.
	 *
	 * @param givenGffs a File[] containing various file types the user selects
	 */
	private static void gffReader(File[] givenGffs) {
		if (givenGffs.length != 0) {
			// 1. Gather the gff files names in a parentvcflist
			String[] endings = { ".gff", ".gff3" };
			ArrayList<String> gffsLST = fileSorter(givenGffs, endings);

			// 2. Add all the found gff files to the gff parentvcflist.
			if (gffsLST.size() >= 1) {
				gffLST = new ArrayList<String>();

				for (int i = 0; i < gffsLST.size(); i++) {
					gffLST.add(gffsLST.get(i));
				}
			} else {
				gffLST = new ArrayList<String>();
			}
		}
	}

	/**
	 * Helper method for reader methods in Reader class. Finds the names for each
	 * file that matches the type list and returns them as a string that is ready
	 * for display on the JOptionPane.
	 *
	 * @param givenFiles a parentvcflist of user selected files
	 * @param types      a parentvcflist of endings to the files desired to be
	 *                   selected
	 * @return a string representing names of all vcf files
	 */
	private static String fileReaderHelper(File[] givenFiles, String[] types) {
		String out = "";
		if (givenFiles.length != 0) {
			for (int i = 0; i < givenFiles.length; i++) {
				for (int j = 0; j < types.length; j++) {
					if (givenFiles[i].getName().endsWith(types[j])) {
						if (!(out.equals("")) && i % 3 == 0) {
							out = out + " " + givenFiles[i].getName() + "\n";
						} else if (!(out.equals("") && i % 3 == 0)) {
							out = out + " " + givenFiles[i].getName();
						} else {
							out = givenFiles[i].getName();
						}
					}
				}
			}
		}
		return out;
	}

	/**
	 * Takes in a list of files and returns a new list of files based upon the other
	 * given list of specified file ending types. This method is locally used in
	 * finding the gff, vcf, and fasta files.
	 *
	 * @param givenfiles a parentvcflist of user selected files
	 * @param types      a parentvcflist of endings to the files desired to be
	 *                   selected
	 * @return a LinkedList<String> of filtered parentvcflist of all files found
	 *         with those endings
	 */
	private static ArrayList<String> fileSorter(File[] givenfiles, String[] types) {
		ArrayList<String> filesLST = new ArrayList<String>();
		// For every file in a user selected parentvcflist and the selected types, if
		// the ending
		// matches add it to the return parentvcflist.
		for (int i = 0; i < givenfiles.length; i++) {
			for (int j = 0; j < types.length; j++) {
				if (givenfiles[i].getName().endsWith(types[j])) {
					filesLST.add(givenfiles[i].getName());
				}
			}
		}
		return filesLST;
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

	/**
	 * The getter method for the vcf children files.
	 *
	 * @return vcf children files.
	 */
	public static ArrayList<String> getVcfsChildLST() {
		return vcfsChildLST;
	}

	/**
	 * The setter method for the vcfs Children Files. Sets the variable for the vcf
	 * children files.
	 *
	 * @param vcfsChildLST
	 */
	public static void setVcfsChildLST(ArrayList<String> vcfsChildLST) {
		Reader.vcfsChildLST = vcfsChildLST;
	}

	/**
	 * The getter method for the vcf parent files.
	 *
	 * @return vcf parent files.
	 */
	public static ArrayList<String> getVcfsParLST() {
		return vcfsParLST;
	}

	/**
	 * The setter method for the vcfs Parent Files. Sets the variable for the vcf
	 * parent files selected by the user.
	 *
	 * @param vcfsParLST
	 */
	public static void setVcfsParLST(ArrayList<String> vcfsParLST) {
		Reader.vcfsParLST = vcfsParLST;
	}

	/**
	 * The getter method for the gff Files selected by the user.
	 *
	 * @return gff file selected by user.
	 */
	public static ArrayList<String> getGffLST() {
		return gffLST;
	}

	/**
	 * The setter method for the gff Files. Sets the variable for the gff children
	 * files.
	 *
	 * @param gffLST
	 */
	public static void setGffLST(ArrayList<String> gffLST) {
		Reader.gffLST = gffLST;
	}
}
