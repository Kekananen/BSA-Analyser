package bsa_analyser.github.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class contains a single method that finds the common alleles between two
 * vcfs and filters them. The order in which the filters should be run one each
 * of the methods starts always with the filterUnMapped, followed by the
 * appropriate filter for the selected method. Common variant filter must be
 * done after homozygous regions are found for the methods.
 * 
 * @author Kathryn Kananen
 *
 */
public class VcfFilter {

	private static int MQThreshold = 40;
	private static int FSThreshold = 60;
	private static double QDThreshold = 2.0;

	public VcfFilter() {

	}

	/**
	 * Filters out the unmapped variants and calls that don't have enough DP with
	 * variant equal to ./. from the list. Use the GATK recommendations for
	 * filtering on MQ < 40.0 (Mapping Quality), FS > 60.0 (Phred score Fisher’s
	 * test p-value for strand bias), QD < 2.0 (Variant Quality / depth of non-ref
	 * samples).
	 * 
	 * @param vcf1 the first vcf file with one phenotype
	 * @param vcf2 the second vcf file with contrasting phenotype
	 * @return a HashMap<String, String> that contains a list of lines that meets
	 *         the filter criteria.
	 */
	public static ArrayList<String> filterUnMapped(ArrayList<String> vcf) {
		// Holds a comparison pool made from the first pool given.
		ArrayList<String> filtVcf = new ArrayList<String>();

		for (int i = 0; i < vcf.size(); i++) {
			String[] line = vcf.get(i).split("\t");
			// 1. Get all the lines that are not containing metadata.
			if (!(line[0].startsWith("#"))) {
				// Remove lines that have not enough coverage to have variant information.
				if (!(line[7].contains("DP=0") || line[9].contains("./.") || line[0].split("ch")[1].equals("00"))) {
					// Filter for the Mapping Quality, Filter for the Phred score Fisher’s test
					// p-value for strand bias, Variant Quality / depth of non-ref samples. Some
					// lines may not contain all three variables and are cut at this point.
					if (line[7].contains("MQ") && line[7].contains("FS") && line[7].contains("QD")) {
						int MQindex = 0;
						int FSindex = 0;
						int QDindex = 0;

						String[] filtLine = line[7].split(";");
						// Finds the index where the filtering variable is in the line without relying
						// on indexes in case the positions change.
						for (int j = 0; j < Arrays.asList(filtLine).size(); j++) {
							if (Arrays.asList(filtLine).get(j).startsWith("MQ=")) {
								MQindex = Arrays.asList(filtLine).indexOf(Arrays.asList(filtLine).get(j));
							}
						}
						for (int j = 0; j < Arrays.asList(filtLine).size(); j++) {
							if (Arrays.asList(filtLine).get(j).startsWith("FS=")) {
								FSindex = Arrays.asList(filtLine).indexOf(Arrays.asList(filtLine).get(j));
							}
						}
						for (int j = 0; j < Arrays.asList(filtLine).size(); j++) {
							if (Arrays.asList(filtLine).get(j).startsWith("QD=")) {
								QDindex = Arrays.asList(filtLine).indexOf(Arrays.asList(filtLine).get(j));
							}
						}

						if (Double.parseDouble(filtLine[MQindex].split("=")[1]) >= MQThreshold
								&& Double.parseDouble(filtLine[FSindex].split("=")[1]) <= FSThreshold
								&& Double.parseDouble(filtLine[QDindex].split("=")[1]) >= QDThreshold) {
							filtVcf.add(vcf.get(i));
						}
					}
				}
			}
		}

		return filtVcf;
	}

	/**
	 * 
	 * @param vcf
	 * @return
	 */
	public static ArrayList<String> homoRunFinder(ArrayList<String> vcf) {
		ArrayList<String> homoRuns = new ArrayList<String>();
		ArrayList<String> homoRegions = new ArrayList<String>();
		// 1. Find the positons that are the inversrse of the homozygous positions and
		// add them to an arrayList.
		for (int i = 0; i < vcf.size(); i++) {
			String[] line = vcf.get(i).split("\t");
			if (!(line[0].startsWith("#"))) {
				// Either Homozygous or Heterozygous variant based on GT in column 9 variable 1.
				String type = line[9].split(":")[0];
				// If it is a Heterozygous allele then add the position to the ArrayList.
				if (!(type.split("/")[0].equals(type.split("/")[1]))) {
					homoRuns.add(line[0].split("ch")[1] + "-" + line[1]);
				}
			}
		}

		// 2. Look through the ArrayList and find the distance between all of the
		// variables and if it is less than 1000bp remove it. The distance of the
		// current allele i left side and right side are checked to see if they add up
		// to 1000 or greater before removal.
		ArrayList<String> heteroHits = new ArrayList<String>();
		for (int i = 0; i < homoRuns.size(); i++) {
			// Avoid the out of index error at the end.
			if (i + 2 < homoRuns.size()) {
				String[] nextInfo = homoRuns.get(i + 1).split("-");
				String[] curInfo = homoRuns.get(i).split("-");

				String chromNext = nextInfo[0];
				String posNext = nextInfo[1];
				String chrom = curInfo[0];
				String pos = curInfo[1];

				String posThird = homoRuns.get(i + 2).split("-")[1];

				// Make sure that the positions don't have an edge case with chromosomes not
				// matching but positions are calculated using one another.
				if (chromNext.equals(chrom)) {
					int left = Integer.parseInt(posNext) - Integer.parseInt(pos);
					int right = Integer.parseInt(posThird) - Integer.parseInt(posNext);

					// Add the regions that have more than 1000bp to a new list.
					if (left + right >= 1000) {
						heteroHits.add(homoRuns.get(i));
					}
				}
			}
		}

		// 2.2 Get the front edge of the region to find to start of the first homozygous
		// position later by grabbing the first heterozygous position that fails this
		// case. All homozygous positions after this are by default the region before
		// the first "good" candidate for the casual mutation.
		// Add the first position that will be the starting point.
		ArrayList<String> homoLociF = new ArrayList<String>();
		ArrayList<String> heteroLociF = new ArrayList<String>();
		ArrayList<String> homoLociL = new ArrayList<String>();
		ArrayList<String> heteroLociL = new ArrayList<String>();
		String firstPos = heteroHits.get(0);
		String lastPos = heteroHits.get(heteroHits.size() - 1);

		// Just go through the file and get the possible positions for the first
		// heterozygote.
		for (int i = 0; i < vcf.size(); i++) {
			String[] line = vcf.get(i).split("\t");
			if (!(line[0].startsWith("#"))) {
				// 2.3 Fill the ArrayLists until the first position is found with both
				// homozygous and heterozygous loci. While the chromosome is the same and the
				// vcf's position is less than that of the list see if it is heterozygous or
				// homozgous and add it to the linkened list.
				if ((line[0].split("ch")[1].equals(firstPos.split("-")[0]))
						&& Integer.parseInt(line[1]) < Integer.parseInt((firstPos.split("-")[1]))) {
					// If it is homozygous add it to homoLoci list otherwise to the heterozygous
					// list
					if (line[9].split(":")[0].split("/")[0].equals(line[9].split(":")[0].split("/")[1])) {
						homoLociF.add(line[0].split("ch")[1] + "-" + line[1]);
					} else {
						heteroLociF.add(line[0].split("ch")[1] + "-" + line[1]);
					}
				} else {
					// Save computational time by breaking out once criteria is met.
					break;
				}
			}
		}
		// Do the same for the final position.
		for (int i = 0; i < vcf.size(); i++) {
			String[] line = vcf.get(i).split("\t");
			if (!(line[0].startsWith("#"))) {
				// 2.4 Fill the ArrayLists until the last position is found with both
				// homozygous and heterozygous.
				if (line[0].split("ch")[1].equals(lastPos.split("-")[0])
						&& Integer.parseInt(line[1]) > Integer.parseInt((lastPos.split("-")[1]))) {
					// If it is homozygous add it to homoLoci list otherwise to the heterozygous
					// list.
					if (line[9].split(":")[0].split("/")[0].equals(line[9].split(":")[0].split("/")[1])) {
						homoLociL.add(line[0].split("ch")[1] + "-" + line[1]);
					} else {
						heteroLociL.add(line[0].split("ch")[1] + "-" + line[1]);
					}
				}
			} else {
				// Save computational time by breaking out once criteria is met.
				break;
			}
		}

		// 2.3 Calculate the starting distance on the left and right side for the
		// firstEdge case and add it to the list as the first position if it is truly
		// valid.
		if (heteroLociF.size() != 0 && homoLociF.size() == 0) {
			String[] hetInfo = heteroLociF.get(heteroLociF.size() - 1).split("-");
			String pos = hetInfo[heteroLociF.size() - 1];

			int left = Integer.parseInt(firstPos.split("-")[1]) - Integer.parseInt(pos);
			int right = Integer.parseInt(heteroHits.get(1).split("-")[1]) - Integer.parseInt(firstPos.split("-")[1]);

			// Add the regions that have more than 1000bp to a new list.
			if (left + right >= 1000) {
				heteroHits.add(0, pos);
			}
		} else if (homoLociF.size() != 0 && heteroLociF.size() == 0) {
			String[] homoInfo = homoLociF.get(homoLociF.size() - 1).split("-");
			String pos = homoInfo[homoInfo.length - 1];

			int left = Integer.parseInt(firstPos.split("-")[1]) - Integer.parseInt(pos);
			int right = Integer.parseInt(heteroHits.get(1).split("-")[1]) - Integer.parseInt(firstPos.split("-")[1]);

			if (left + right >= 1000) {
				// Since it is a homozygous point, it should be included later and is thus
				// marked for later when searching.
				heteroHits.add(0, pos + "HOMO");
			}
		}

		// 2.4 Calculate the starting distance on the left and right side for the
		// backEdge case and add it to the list as the first position if it is truly
		// valid.
		if (heteroLociL.size() != 0 && homoLociL.size() == 0) {
			String[] hetInfo = heteroLociL.get(heteroLociL.size() - 1).split("-");
			String pos = hetInfo[1];

			int left = Integer.parseInt(heteroHits.get(1).split("-")[1]) - Integer.parseInt(firstPos.split("-")[1]);
			int right = Integer.parseInt(heteroLociL.get(heteroLociL.size() - 1).split("-")[1])
					- Integer.parseInt(lastPos.split("-")[1]);

			// Add the regions that have more than 1000bp to a new list.
			if (left + right >= 1000) {
				heteroHits.add(0, pos);
			}
		} else if (homoLociL.size() != 0 && heteroLociL.size() == 0) {
			String[] homoInfo = homoLociF.get(homoLociF.size() - 1).split("-");
			String pos = homoInfo[homoInfo.length - 1];

			int left = Integer.parseInt(firstPos.split("-")[1]) - Integer.parseInt(pos);
			int right = Integer.parseInt(firstPos.split("-")[1])
					- Integer.parseInt(heteroHits.get(heteroHits.size() - 1).split("-")[1]);

			if (left + right >= 1000) {
				// Since it is a homozygous point, it should be included later and is thus
				// marked for later when searching.
				heteroHits.add(heteroHits.size() - 1, pos + "+HOMO");
			}
		}

		// 3. Find all the regions between the pairs of positions that correlate to the
		// heterozygous hits.
		ArrayList<String> region = new ArrayList<String>();
		for (int i = 0; i < heteroHits.size(); i++) {
			for (int j = 0; j < vcf.size(); j++) {
				String[] line = vcf.get(j).split("\t");
				if (!(line[0].startsWith("#"))) {
					int hit = 0;
					if (j + 2 <= heteroHits.size()) {
						String[] hetNextInfo = heteroHits.get(j + 1).split("-");
						String[] hetInfo = heteroHits.get(j).split("-");
						// If it is a homozygous position to start with.
						if (hetNextInfo[1].contains("+HOMO") || hetInfo[1].contains("+HOMO")) {
							
							if (hetNextInfo[0].split("+")[0].equals(hetInfo[0].split("+")[0])) {
								if (hetInfo[1].equals(line[1])) {
									hit++;
								}
								// Add the regions in between the two regions
								region.add(vcf.get(j));

								if (hit == 2) {
									hit = 0;
									region = new ArrayList<String>();
									homoRegions.addAll(region);
								}
							}
						}
					}
				}
			}
		}

		return homoRuns;
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
	 * @return an ArrayList containing two filtered ArrayLists.
	 */
	@SuppressWarnings("rawtypes")
	public static ArrayList[] filtVars(ArrayList<String> vcf1, ArrayList<String> vcf2) {
		// Holds a comparison pool made from the first pool given.
		ArrayList[] mutCompVcf = new ArrayList[2];

		ArrayList<String> out1 = new ArrayList<String>();
		ArrayList<String> out2 = new ArrayList<String>();

		HashMap<String, String> mutCompMap1 = filtVarsMapMaker(vcf1);
		HashMap<String, String> mutCompMap2 = filtVarsMapMaker(vcf2);

		ArrayList<HashMap<String, String>> run1 = filtVarsMapUpdate(mutCompMap1, mutCompMap2, vcf1);
		mutCompMap1 = run1.get(0);
		mutCompMap2 = run1.get(1);

		ArrayList<HashMap<String, String>> run2 = filtVarsMapUpdate(mutCompMap2, mutCompMap1, vcf2);
		mutCompMap2 = run2.get(0);
		mutCompMap1 = run2.get(1);

		Object[] keys1 = mutCompMap1.keySet().toArray();
		for (int i = 0; i < mutCompMap1.size(); i++) {
			out1.add(mutCompMap1.get(keys1[i]));
		}
		mutCompVcf[0] = out1;

		Object[] keys2 = mutCompMap2.keySet().toArray();
		for (int i = 0; i < mutCompMap2.size(); i++) {
			out2.add(mutCompMap2.get(keys2[i]));
		}
		mutCompVcf[1] = out2;

		return mutCompVcf;
	}

	/**
	 * Turns an ArrayList containing the HashMap information into a HashMap.
	 * 
	 * @param vcf
	 * @return a HashMap made from the vcf file in an ArrayList.
	 */
	private static HashMap<String, String> filtVarsMapMaker(ArrayList<String> vcf) {
		HashMap<String, String> mutCompMap = new HashMap<String, String>();

		for (int i = 0; i < vcf.size(); i++) {
			String[] line = vcf.get(i).split("\t");
			// 1. Get all the lines that are not containing metadata.
			if (!(line[0].startsWith("#"))) {
				// 1.1 Get the proper label as there could be duplicate positions on the chromos
				// include them in the key as well as the position coords.
				String key = line[0].split("ch")[1] + "-" + line[1];
				mutCompMap.put(key, vcf.get(i));
			}
		}

		return mutCompMap;
	}

	/**
	 * Updates the HashMaps to not contain the same variants as the other.
	 * 
	 * @param map1 first map to be compared containing vcf values
	 * @param map2 first map to be compared containing vcf values
	 * @param vcf
	 * @return an ArrayList of size 2 with the two updated HashMaps with the first
	 *         given map1 at position 0 and the second at position1.
	 */
	private static ArrayList<HashMap<String, String>> filtVarsMapUpdate(HashMap<String, String> map1,
			HashMap<String, String> map2, ArrayList<String> vcf) {
		ArrayList<HashMap<String, String>> out = new ArrayList<HashMap<String, String>>();
		// 2. Compare the 2nd pool to the 1st for every position and if there is a match
		// remove them from the mutCompPool map as if they are found in both pools it
		// can't be the casual mutation and is just noise.
		for (int i = 0; i < vcf.size(); i++) {
			String[] line = vcf.get(i).split("\t");
			if (!(line[0].startsWith("#"))) {
				String match = line[0].split("ch")[1] + "-" + line[1];
				// 2.1 See if the key matches the position in the 2nd pool and if so
				// remove it; however, only remove it if it's not unmapped as one unmapped
				// in one vcf doesn't mean it's unmapped in the other.
				for (int j = 0; j < map1.size(); j++) {
					if (map1.containsKey(match)) {
						if (map1.get(match).split("\t")[4].equals(map2.get(match).split("\t")[4])) {
							map1.remove(match);
						} else {
							// 3. Add in the remaining positions from the second array list to the HashMap.
							map2.put(match, map2.get(match));
						}
					}
				}
			}
		}
		out.add(map1);
		out.add(map2);

		return out;
	}

	/**
	 * Finds the alleles shared between the two parents given there are multiple
	 * variants and removes them from both of the parents. An example of this case
	 * is the alt in the norm being A,T and the alt being A in the mutant.
	 * 
	 * @param mut  the mutant parent
	 * @param norm the normal parent
	 * @return a list of type ArrayList that is of size two with the first position
	 *         being the mut and the second position being the norm.
	 */
	@SuppressWarnings("rawtypes")
	public static ArrayList[] parentFilter(ArrayList<String> mut, ArrayList<String> norm) {
		// Holds the list to be output.
		ArrayList[] out = new ArrayList[2];
		// 1. Look through the mutant list and split the list to find the chromosomes
		// and positions for a later comparison to the normal parent type.
		for (int i = 0; i < mut.size(); i++) {
			String[] line = mut.get(i).split("\t");
			if (!(line[0].startsWith("#"))) {
				String mutMatch = line[0].split("ch")[1] + "-" + line[1];
				// 2. Look through the normal parent and compare it to the mutant parent.
				for (int j = 0; j < norm.size(); j++) {
					String[] compline = norm.get(j).split("\t");
					if (!(compline[0].startsWith("#"))) {
						String normMatch = compline[0].split("ch")[1] + "-" + compline[1];
						// 2.1 If the mutant parent matches the chromosome and postion of the normal
						// parent then look at these lines only.
						if (mutMatch.equals(normMatch)) {
							// 2.2 If the lines contain multiple alts then look to see if the alts match
							// with either of the other alts in the two parent pools. If they do then they
							// need to be removed as they are in common and thus can't be the casual
							// mutation. (might want to look at the penetration of the mutation in the pool
							// later before remove. May add this later on).
							if (line[4].contains(",") && !(compline[4].contains(","))) {
								String[] alts = line[4].split(",");
								for (int k = 0; k < alts.length; k++) {
									if (alts[k].equals(compline[4])) {
										mut.remove(i);
										norm.remove(j);
									}
								}
							}

							if (compline[4].contains(",") && !(line[4].contains(","))) {
								System.out.println("hit");
								String[] alts = compline[4].split(",");
								for (int k = 0; k < alts.length; k++) {
									if (alts[k].equals(line[4])) {
										mut.remove(i);
										norm.remove(j);
									}
								}
							}

							// If both have alternates then two lists must be iterated through to make sure
							// none are in common. in general this case should probably be removed but we
							// will keep it for now since it survived the previous filters.
							if (line[4].contains(",") && compline[4].contains(",")) {
								String[] altsComp = compline[4].split(",");
								String[] altsLine = line[4].split(",");
								for (int k = 0; k < altsComp.length; k++) {
									for (int l = 0; l < altsLine.length; l++) {
										if (altsComp[k].equals(altsLine[l])) {
											mut.remove(i);
											norm.remove(j);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		out[0] = mut;
		out[1] = norm;

		return out;
	}

	public int getMQThreshold() {
		return MQThreshold;
	}

	public void setMQThreshold(int mQThreshold) {
		MQThreshold = mQThreshold;
	}

	public int getFSThreshold() {
		return FSThreshold;
	}

	public void setFSThreshold(int fSThreshold) {
		FSThreshold = fSThreshold;
	}

	public double getQDThreshold() {
		return QDThreshold;
	}

	public void setQDThreshold(double qDThreshold) {
		QDThreshold = qDThreshold;
	}

}
