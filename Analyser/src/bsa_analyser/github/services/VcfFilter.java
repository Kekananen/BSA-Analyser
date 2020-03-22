package bsa_analyser.github.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

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
		ArrayList<String> mappedVcf = new ArrayList<String>();

		for (int i = 0; i < vcf.size(); i++) {
			String[] line = vcf.get(i).split("\t");
			// 1. Get all the lines that are not containing metadata.
			if (!(line[0].startsWith("#"))) {
				// Remove lines that have not enough coverage to have variant information.
				if (!(line[7].contains("DP=0") || line[9].contains("./."))) {
					String[] filtLine = line[7].split(";");
					// Filter for the Mapping Quality.
					if (line[7].contains("MQ")) {
						// Filter for the Phred score Fisher’s test p-value for strand bias.
						if (line[7].contains("FS")) {
							// Variant Quality / depth of non-ref samples).
							if (line[7].contains("QD")) {
								int MQindex = 0;
								for (int j = 0; j < Arrays.asList(filtLine).size(); j++) {
									if (Arrays.asList(filtLine).get(j).startsWith("MQ")) {
										MQindex = Arrays.asList(filtLine).indexOf(Arrays.asList(filtLine).get(j));
									}
								}
								int FSindex = 0;
								for (int j = 0; j < Arrays.asList(filtLine).size(); j++) {
									if (Arrays.asList(filtLine).get(j).startsWith("FS")) {
										FSindex = Arrays.asList(filtLine).indexOf(Arrays.asList(filtLine).get(j));
									}
								}
								int QDindex = 0;
								for (int j = 0; j < Arrays.asList(filtLine).size(); j++) {
									if (Arrays.asList(filtLine).get(j).startsWith("QD")) {
										QDindex = Arrays.asList(filtLine).indexOf(Arrays.asList(filtLine).get(j));
									}
								}

								if (Double.parseDouble(filtLine[MQindex].split("=")[1]) < MQThreshold
										&& Double.parseDouble(filtLine[FSindex].split("=")[1]) > FSThreshold
										&& Double.parseDouble(filtLine[QDindex].split("=")[1]) < QDThreshold) {
									// 1.1 Get the proper label as there could be duplicate positions on the chromos
									// include them in the key as well as the position coords.
									String chrom = line[0].split("ch")[1];
									if (!(chrom.equals("00"))) {
										mappedVcf.add(vcf.get(i));
									}
								}
							}
						}
					}
				}
			}
		}

		return mappedVcf;
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
	public static HashMap<String, String> filtVarsHM(ArrayList<String> vcf1, ArrayList<String> vcf2) {
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

	public static HashMap<String, String> filtVars(ArrayList<String> vcf1, ArrayList<String> vcf2) {
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

	@SuppressWarnings("unchecked")
	public static HashMap<String, ArrayList<String>> parentFilter(HashMap<String, ArrayList<String>> mut,
			HashMap<String, ArrayList<String>> norm) {
		// Holds two HashMaps that contain the parents information after filtering.
		HashMap<String, ArrayList<String>> parentMap = new HashMap<String, ArrayList<String>>();
		// Holds the larger of the selected maps.
		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
		// Holds the smaller of the selected maps.
		HashMap<String, ArrayList<String>> other = new HashMap<String, ArrayList<String>>();
		// 1. Check that there are the same amount of chromosomes in the HashMaps as if
		// all the alleles are filtered out, or no positions are read, etc then there
		// may be a mismatch which will cause an error. If that is the case correct for
		// this by adding a extra chromosome for each missing value as a dummy value
		// that matches the missing value to keep the program from crashing.
		Object[] chroms = chromAdjuster(mut, norm);

		// 2. Find the larger of the two pools as to not cause a null pointer error
		// later. If doesn't matter which pool is chosen as both have the same number of
		// chromosomes.
		int[] poolLen = getMapSize(mut, norm);
		int pool1Len = poolLen[0];
		int pool2Len = poolLen[1];
		// 2.1 Select the larger of the two arrayLists based on the counts.
		Object[] mapInfo = getMapListLens(mut, norm, pool1Len, pool2Len, chroms);
		selected = (HashMap<String, ArrayList<String>>) mapInfo[0];
		other = (HashMap<String, ArrayList<String>>) mapInfo[1];
		ArrayList<Integer> chromLens1 = (ArrayList<Integer>) mapInfo[2];
		ArrayList<Integer> chromLens2 = (ArrayList<Integer>) mapInfo[3];

		// 3. Use the chromosome keys to find the matching positions in the two parents.
		// The index here can also be used to iterate through the chromLens arrayList.
		for (int i = 0; i < chroms.length; i++) {

			ArrayList<String> posInfo = new ArrayList<String>();
			ArrayList<String> varInfo = new ArrayList<String>();
			ArrayList<String> freqInfo = new ArrayList<String>();

			// 3.1 Look through the ArrayList and get the values for each of the separate
			// information types stored for the larger of the HashMaps overall ArrayList
			// size wise.
			for (int j = 0; j < chromLens1.get(i); j += 3) {
				posInfo.add(selected.get(chroms[i]).get(j));
				varInfo.add(selected.get(chroms[i]).get(j + 1));
				freqInfo.add(selected.get(chroms[i]).get(j + 2));
			}

			// Create iterators for each of the value types.
			Iterator<String> posIter = posInfo.listIterator();
			Iterator<String> varIter = varInfo.listIterator();
			Iterator<String> freqIter = freqInfo.listIterator();

			String pos = posIter.next();
			String var = varIter.next();
//			String freq = freqIter.next();

			// Holds the region to be output for the array.
			ArrayList<String> region = new ArrayList<String>();

			while (posIter.hasNext()) {
				for (int j = 0; j < chromLens2.get(i); j += 3) {
					if (!(parentMap.containsKey(chroms[i]))) {
						parentMap.put((String) chroms[i], new ArrayList<String>());
					} else {
						if (var.contains(",")) {
							String[] vars = var.split(",");
							for (int k = 0; k < vars.length; k++) {
								if (!(vars[k].equals(other.get(chroms[i]).get(j)))) {
									region = parentMap.get(chroms[i]);
									if (!(region.contains(other.get(chroms[i]).get(j)))) {
										region.add(other.get(chroms[i]).get(j));
										region.add(other.get(chroms[i]).get(j + 1));
										region.add(other.get(chroms[i]).get(j + 2));
										region.add(freqIter.next());

										parentMap.put((String) chroms[i], region);
									}
								}
							}
						}

//						if (!(other.get(chroms[i]).get(j).equals(pos) && other.get(chroms[i]).get(j + 1).equals(var))) {
//							if (!(region.contains(other.get(chroms[i]).get(j)))) {
//								region = parentMap.get(chroms[i]);
//								region.add(other.get(chroms[i]).get(j));
//								region.add(other.get(chroms[i]).get(j + 1));
//								region.add(other.get(chroms[i]).get(j + 2));
//								region.add(freqIter.next());
//
//								parentMap.put((String) chroms[i], region);
//							}
//						}

					}
				}

				var = varIter.next();
				pos = posIter.next();
			}
			System.out.println(parentMap);

//			System.out.println(parentMap);
//			
//			int cnt = 0;
//			while(freqIter.hasNext()) {
//				parentMap.get(chroms[i]).add(3, freq);
//				freq = freqIter.next();
//				cnt++;
//			}

		}

		System.out.println(parentMap);
		return parentMap;
	}

	/**
	 * Finds the larger of the two of the length of all the ArrayLists in the two
	 * HashMaps and also finds the larger of the two HashMaps. The largest is placed
	 * in the front most index.
	 * 
	 * @param pool1    HashMap representing pool1
	 * @param pool2    HashMap representing pool2
	 * @param pool1Len the lengths of the ArraysLists in pool1
	 * @param pool2Len the lengths of the ArraysLists in pool2
	 * @param chroms   the list of chromosomes
	 * @return An arrayList containing four objects - pool1 and pool2 as HashMaps,
	 *         their corresponding list of ArrayList value lengths.
	 */
	@SuppressWarnings("unused")
	private static Object[] getMapListLens(HashMap<String, ArrayList<String>> pool1,
			HashMap<String, ArrayList<String>> pool2, int pool1Len, int pool2Len, Object[] chroms) {
		// ArrayList containing four objects - pool1 and pool2 as HashMaps, their
		// corresponding list of ArrayList value lengths.
		Object[] out = new Object[4];
		// Holds the larger of the selected maps.
		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
		// Holds the smaller of the selected maps.
		HashMap<String, ArrayList<String>> other = new HashMap<String, ArrayList<String>>();

		ArrayList<Integer> chromLens1 = new ArrayList<Integer>();
		ArrayList<Integer> chromLens2 = new ArrayList<Integer>();
		// 1.2 Select the larger of the two arrayLists based on the counts.
		for (int i = 0; i < chroms.length; i++) {
			if (pool1Len > pool2Len) {
				selected = pool2;
				other = pool1;

				chromLens1.add(pool2.get(chroms[i]).size());
				chromLens2.add(pool1.get(chroms[i]).size());
			} else {
				selected = pool1;
				other = pool2;

				chromLens1.add(pool1.get(chroms[i]).size());
				chromLens2.add(pool2.get(chroms[i]).size());
			}
		}
		out[0] = selected;
		out[1] = other;
		out[2] = chromLens1;
		out[3] = chromLens2;

		return out;
	}

	/**
	 * Gets the size of the HashMaps containing arraylists as the values and returns
	 * them as an list of two integer values with pool1 at index 0 and pool2 at
	 * index 1.
	 * 
	 * @param pool1 HashMap representing pool1
	 * @param pool2 HashMap representing pool2
	 * @return an int[] of size 2.
	 */
	@SuppressWarnings({ "unchecked" })
	private static int[] getMapSize(HashMap<String, ArrayList<String>> pool1,
			HashMap<String, ArrayList<String>> pool2) {
		int[] poolLen = new int[2];
		int pool1Len = 0, pool2Len = 0;
		for (int i = 0; i < pool1.size(); i++) {
			// 1.1 Add the lengths of each of the values stored in the chromosomes to the
			// length counts.
			ArrayList<Object> temp = (ArrayList<Object>) pool1.values().toArray()[i];
			ArrayList<Object> temp2 = (ArrayList<Object>) pool2.values().toArray()[i];

			pool1Len = pool1Len + temp.size();
			pool2Len = pool2Len + temp2.size();
		}
		poolLen[0] = pool1Len;
		poolLen[1] = pool2Len;

		return poolLen;
	}

	/**
	 * Adjusts the chromosomes index to match if the two pools don't have the same
	 * amount of chromosomes. For later comparisons this allows the two pools to
	 * avoid various null pointer errors and redundant checking.
	 * 
	 * @param pool1 HashMap representing pool1
	 * @param pool2 HashMap representing pool2
	 * @return an Object[] that holds all the chromosomes shared between the two
	 *         lists.
	 */
	private static Object[] chromAdjuster(HashMap<String, ArrayList<String>> pool1,
			HashMap<String, ArrayList<String>> pool2) {
		// Holds chromosome from the organism.
		Object[] chroms = pool1.keySet().toArray();
		// 1. Check that there are the same amount of chromosomes in the HashMaps as if
		// all the alleles are filtered out, or no positions are read, etc then there
		// may be a mismatch which will cause an error. If that is the case correct for
		// this by adding a extra chromosome for each missing value as a dummy value
		// that matches the missing value to keep the program from crashing.
		if (!(chroms.equals(pool1.keySet().toArray()))) {
			// Case 1: They are of different sizes
			// Case 2: They have different values
			Object[] temp = pool1.keySet().toArray();

			// Find the intersect of the chromosomes.
			ArrayList<Object> intersect = new ArrayList<Object>(Arrays.asList(temp));
			intersect.retainAll(Arrays.asList(chroms));

			for (int i = 0; i < intersect.size(); i++) {
				// Add the extra chromosomes to the chroms if they are not already there. The
				// resulting chroms list is used as the base for both lists later so no else
				// statement is necessary in this case.
				if (!(Arrays.asList(chroms).contains(intersect.get(i)))) {
					chroms = new Object[chroms.length + 1];
					for (int j = 0; j < pool2.keySet().size(); j++) {
						chroms[j] = pool2.keySet().toArray()[j];
					}
					// Adds the chromosome at the end of the array.
					chroms[chroms.length] = intersect.get(i);
				}

			}
		}

		return chroms;
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
