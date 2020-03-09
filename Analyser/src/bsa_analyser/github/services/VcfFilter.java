package bsa_analyser.github.services;

import java.util.ArrayList;
import java.util.HashMap;

public class VcfFilter {
	public VcfFilter() {

	}

	/**
	 * Finds the unique variants between two vcfs files, thus filtering out the
	 * common variants shared between the two files. This is achieved by using a
	 * hashmap and filling it with the positions from the first file. The positions
	 * are removed if they are found in the second file and the remaining positions
	 * in the second file are added to the hashmap. Variants unmapped are not
	 * filtered out even if matched as they can be from any chromosome and thus
	 * could not be a true match.
	 * 
	 * 
	 * @param vcf1 the first vcf file with one phenotype
	 * @param vcf2 the second vcf file with contrasting phenotype
	 */
	private static HashMap<String, String> findCommonVars(ArrayList<String> vcf1, ArrayList<String> vcf2) {
		// Holds a comparison pool made from the first pool given.
		HashMap<String, String> mutCompVcf = new HashMap<String, String>();

		for (int i = 0; i < vcf1.size(); i++) {
			String[] line = vcf1.get(i).split("\t");
			// 1. Get all the lines that are not containing metadata.
			if (!(line[0].startsWith("#"))) {
				// 1.1 Get the proper label as there could be duplicate positions on the chromos
				// include them in the key as well as the position coords.
				String key = line[0].split("ch")[1] + "-" + line[1];
				// 1.2 Get the observed variant and the alternate.
				String value = line[3] + ">" + line[4];

				mutCompVcf.put(key, value);
			}
		}

		// 2. Compare the 2nd pool to the 1st for every position and if there is a match
		// remove them from the mutCompPool map as if they are found in both pools it
		// can't be the casual mutation and is just noise.
		for (int i = 0; i < vcf2.size(); i++) {
			String[] line = vcf2.get(i).split("\t");
			if (!(line[0].startsWith("#"))) {
				String match = line[0].split("ch")[1] + "-" + line[1];
				// 2.1 See if the key matches the position in the 2nd pool and if so
				// remove it; however, only remove it if it's not unmapped as one unmapped
				// in one vcf doesn't mean it's unmapped in the other.
				if (mutCompVcf.containsKey(match) && !(match.startsWith("00"))) {
					mutCompVcf.remove(match);
					// 3. Add in the remaining positions from the second array list to the HashMap.
				} else {
					String key = line[0].split("ch")[1] + "-" + line[1];

					mutCompVcf.put(match, key);
				}
			}
		}
		return mutCompVcf;
	}
}
