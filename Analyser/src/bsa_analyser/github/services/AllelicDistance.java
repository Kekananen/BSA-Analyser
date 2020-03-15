package bsa_analyser.github.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AllelicDistance {
	public AllelicDistance() {

	}

	/**
	 * Finds the frequency that the variant is found in the given pool at that
	 * specific locus. Each pool's information is stored in a HashMap with the key
	 * as each chromosome in the organism and the values as an ArrayList in the
	 * order of [Position, Variant, frequency, ..., ..., ...] and it simply repeats
	 * for each line.
	 * 
	 * @param pool as an ArrayList containing the file information.
	 * @return a HashMap with key as a String and Values as an ArrayList<String>
	 * @return null if no PL or GT value is found in column 10
	 */
	public static HashMap<String, ArrayList<String>> freqFinder(ArrayList<String> pool) {
		// to the HashMap list relating to that chromosome with the helper function.
		HashMap<String, ArrayList<String>> regionMap = new HashMap<String, ArrayList<String>>();
		// Holds the values to be placed in the HashMap.
		ArrayList<String> region = new ArrayList<String>();

		// 1. Look through the pool and for every chromosome add the region, depth, and
		// frequency to the ArrayList.
		for (int i = 0; i < pool.size(); i++) {
			String[] line = pool.get(i).split("\t");
			if (!(line[0].startsWith("#"))) {
				// 1.1 Get the chromosome number for the line.
				String chrom = line[0].split("ch")[1];
				// 1.2 Check if the HashMap contains the key and if not then add the chromosome
				// as the key with an empty ArrayList.
				if (!(regionMap.containsKey(chrom))) {
					regionMap.put(chrom, new ArrayList<String>());
				} else {
					// 1.3 Set the position to be the region for the chromosome, else the
					// chromosomes are all put into the same list.
					region = regionMap.get(chrom);
					// Add the position and the variant.
					region.add(line[1]);
					region.add(line[4]);

					// 2. Calculates the variant allele frequency.
					// Holds the variant allele frequency.
					double vaf = 0;
					// If there isn't enough information to calculate what the variant is then set
					// the vaf to be 0 otherwise continue.
					if (!(line[9].equals("./."))) {
						// If there are not 0 variants of the observed seen in that line then calculate
						// the variant allele frequency for each of the cases.
						if (line[7].contains("DP=0") != true) {

							// 2.1 Calculate where in the vcf the GT and PL are kept and at what index.
							String[] varInfo = line[8].split(":");

							int GTindex = Arrays.asList(varInfo).indexOf("GT");
							int PLindex = Arrays.asList(varInfo).indexOf("PL");
							int ADindex = Arrays.asList(varInfo).indexOf("AD");
							int DPindex = Arrays.asList(varInfo).indexOf("DP");

							String GT = "", PL = "", AD = "", DP = "";
							// If the index is not 0 then get the values.

							if (GTindex != -1) {
								GT = varInfo[GTindex];
							} else {
								return null;
							}
							if (PLindex != -1) {
								PL = line[9].split(":")[PLindex];
							} else {
								return null;
							}
							if (ADindex != -1) {
								AD = varInfo[ADindex];
							} else {
								System.out.println("hit");
								return null;
							}
							if (DPindex != -1) {
								DP = varInfo[ADindex];
							} else {
								System.out.println("hit1");
								return null;
							}
							
							// The variant calculated from the PL variable taking into account both
							// heterozygous counts and homozygous counts for the variant allele.
							String minor = "";
							if(Double.parseDouble(PL.split(",")[2]) > Double.parseDouble(PL.split(",")[0])) {
								Double var = Double.parseDouble(PL.split(",")[2])
										+ Double.parseDouble(PL.split(",")[1]) / 2;
								vaf = var / (Double.parseDouble(PL.split(",")[0]) + var);
							} else {
								Double var = Double.parseDouble(PL.split(",")[0])
										+ Double.parseDouble(PL.split(",")[1]) / 2;
								vaf = var / (Double.parseDouble(PL.split(",")[0]) + var);
							}
							
						} else {
							vaf = 0;
						}
					} else {
						vaf = 0;
					}

					region.add(String.valueOf(vaf));
					regionMap.put(chrom, region);
				}
			}
		}
		
		return regionMap;
	}

	/**
	 * 
	 * @param pool1
	 * @param pool2
	 * @return
	 */
	public static HashMap<String, ArrayList<String>> SimalityFinder(HashMap<String, ArrayList<String>> pool1,
			HashMap<String, ArrayList<String>> pool2) {
		// The HashMap that is to be output with the average similarity frequencies
		// found at each matching position.
		HashMap<String, ArrayList<String>> simMap = new HashMap<String, ArrayList<String>>();
		// The keys of the HashMap which are the chromosomes.
		Object[] chroms = pool1.keySet().toArray();
		// Get the positions, Frequencies, and Alleles.
		ArrayList<String> pos, vars, freq = new ArrayList<String>();

		HashMap<String, ArrayList<String>> selected, other = new HashMap<String, ArrayList<String>>();
		// 1. Find the larger of the two pools as to not cause a null pointer error
		// later. If doesn't matter which pool is chosen as both have the same number of
		// chromosomes.
		int pool1Len = 0, pool2Len = 0;
		for (int i = 0; i < pool1.size(); i++) {
			// 1.1 Add the lengths of each of the values stored in the chromosomes to the
			// length counts.
			ArrayList<Object> temp = (ArrayList<Object>) pool1.values().toArray()[i];
			ArrayList<Object> temp2 = (ArrayList<Object>) pool2.values().toArray()[i];
			pool1Len = pool1Len + temp.size();
			pool2Len = pool2Len + temp2.size();
		}
		// 1.2 Select the larger of the two arrayLists based on the counts.
		if (pool1Len > pool2Len) {
			selected = pool1;
			other = pool2;
		} else {
			selected = pool2;
			other = pool1;
		}

		// 2. Search through the larger of the pools and compare the positions with the
		// smaller of the two pools.
		for (int i = 0; i < chroms.length; i++) {
			// Holds the information of the chromosome.
			ArrayList<String> chromInfo = selected.get(chroms[i]);
			if (chroms[i].equals("01")) {
				System.out.println(chromInfo);
			}

//			ArrayList<String> region = new ArrayList<String>();
//			for (int j = 0; j < chromInfo.size(); j += 3) {
//				System.out.println(chromInfo.get(j));
//			}
		}

		System.out.println(simMap);
		return simMap;
	}
}

//		// 2. Search through the larger of the pools and compare the positions with the
//		// smaller of the two pools.
//		for (int i = 0; i < chroms.length; i++) {
//			ArrayList<String> chromInfo = selected.get(chroms[i]);
//			ArrayList<String> region = new ArrayList<String>();
//			for (int j = 0; j < selected.size(); j += 3) {
//				System.out.println(j);
////				for(int k = 0; k < other.size(); k+= 3) {
////					// 2.1 Build the simMap.
////					if (!(simMap.containsKey(chroms[i]))) {
////						simMap.put((String) chroms[i], new ArrayList<String>());
////					} else {						
////						//2.2 if the positions match on the chromosome the add them to the list.
////						if(other.get(k).equals(chromInfo.get(j))) {
////							region.addAll(other.get(k));
////						} else {
////							break;
////						}
////					}
////				}
//			}
//			
//			
//			
////			for (int j = 1; j < chromInfo.size(); j += 3) {
////				vars.add(chromInfo.get(j));
////			}
////			for (int j = 2; j < chromInfo.size(); j += 3) {
////				freq.add(chromInfo.get(j));
////			}						
//			simMap.put((String) chroms[i], region);
//		}
//
////		// Holds a list of the position matches between the two vcfs.
////		ArrayList<String> posMatches = new ArrayList<String>();
////		
////		for (int i = 0; i < chroms.length; i++) {
////			ArrayList<String> chromInfo = pool2.get(chroms[i]);
////			for (int j = 0; j < chromInfo.size(); j += 3) {
////				for(int k = 0; k < pos.size(); k++) {
////					if(chromInfo.get(j).equals(pos.get(k))) {
////						posMatches.add(chromInfo.get(j));
////					} else if(Integer.parseInt(chromInfo.get(j)) > Integer.parseInt(pos.get(k))) {
////						break;
////					}
////				}
////			}
////		}
//		System.out.println(simMap);
//		return simMap;
//	}
//}