package bsa_analyser.github.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

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
							if (Double.parseDouble(PL.split(",")[2]) > Double.parseDouble(PL.split(",")[0])) {
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

	public static HashMap<String, ArrayList<String>> parentFilter(HashMap<String, ArrayList<String>> mut,
			HashMap<String, ArrayList<String>> norm) {
		// Holds two HashMaps that contain the parents information after filtering.
		HashMap<String, ArrayList<String>> parentMap = new HashMap<String, ArrayList<String>>();
		// Holds chromosome from the organism.
		Object[] chroms = mut.keySet().toArray();
		// 1. Check that there are the same amount of chromosomes in the HashMaps as if
		// all the alleles are filtered out, or no positions are read, etc then there
		// may be a mismatch which will cause an error. If that is the case correct for
		// this by adding a extra chromosome for each missing value as a dummy value
		// that matches the missing value to keep the program from crashing.
		if (!(chroms.equals(norm.keySet().toArray()))) {
			// Case 1: They are of different sizes
			// Case 2: They have different values
			Object[] temp = norm.keySet().toArray();

			// Find the intersect of the chromosomes.
			ArrayList<Object> intersect = new ArrayList<Object>(Arrays.asList(temp));
			intersect.retainAll(Arrays.asList(chroms));

			for (int i = 0; i < intersect.size(); i++) {
				// Add the extra chromosomes to the chroms if they are not already there. The
				// resulting chroms list is used as the base for both lists later so no else
				// statement is necessary in this case.
				if (!(Arrays.asList(chroms).contains(intersect.get(i)))) {
					chroms = new Object[chroms.length + 1];
					for (int j = 0; j < mut.keySet().size(); j++) {
						chroms[j] = mut.keySet().toArray()[j];
					}
					// Adds the chromosome at the end of the array.
					chroms[chroms.length] = intersect.get(i);
				}

			}

		}

		HashMap<String, ArrayList<String>> selected = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> other = new HashMap<String, ArrayList<String>>();
		int pool1Len = 0, pool2Len = 0;
		// 2. Find the larger of the two pools as to not cause a null pointer error
		// later. If doesn't matter which pool is chosen as both have the same number of
		// chromosomes.
		for (int i = 0; i < mut.size(); i++) {
			// 1.1 Add the lengths of each of the values stored in the chromosomes to the
			// length counts.
			ArrayList<Object> temp = (ArrayList<Object>) mut.values().toArray()[i];
			ArrayList<Object> temp2 = (ArrayList<Object>) norm.values().toArray()[i];
			pool1Len = pool1Len + temp.size();
			pool2Len = pool2Len + temp2.size();
		}

		// This list is in the same order as the HashMap keys and thus when iterated
		// through the values are as well by default.
		ArrayList<Integer> chromLens1 = new ArrayList<Integer>();
		ArrayList<Integer> chromLens2 = new ArrayList<Integer>();
		// 1.2 Select the larger of the two arrayLists based on the counts.
		for (int i = 0; i < chroms.length; i++) {
			if (pool1Len > pool2Len) {
				selected = mut;
				other = norm;

				chromLens1.add(mut.get(chroms[i]).size());
				chromLens2.add(norm.get(chroms[i]).size());
			} else {
				selected = norm;
				other = mut;

				chromLens1.add(norm.get(chroms[i]).size());
				chromLens2.add(mut.get(chroms[i]).size());
			}
		}

		// 2. Use the chromosome keys to find the matching positions in the two parents.
		// The index here can also be used to iterate through the chromLens arrayList.
		for (int i = 0; i < chroms.length; i++) {

			ArrayList<String> posInfo = new ArrayList<String>();
			ArrayList<String> varInfo = new ArrayList<String>();
			ArrayList<String> freqInfo = new ArrayList<String>();

			for (int j = 0; j < chromLens1.get(i); j += 3) {
				posInfo.add(selected.get(chroms[i]).get(j));
				varInfo.add(selected.get(chroms[i]).get(j + 1));
				freqInfo.add(selected.get(chroms[i]).get(j + 2));
			}

			Iterator<String> posIter = posInfo.listIterator();
			Iterator<String> varIter = varInfo.listIterator();
			Iterator<String> freqIter = freqInfo.listIterator();

			String pos = posIter.next();
			String var = varIter.next();
			String freq = freqIter.next();

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

										parentMap.put((String) chroms[i], region);
									}
								}
							}
						}

						if (!(other.get(chroms[i]).get(j).equals(pos) && other.get(chroms[i]).get(j + 1).equals(var))) {
							if (!(region.contains(other.get(chroms[i]).get(j)))) {
								region = parentMap.get(chroms[i]);
								region.add(other.get(chroms[i]).get(j));
								region.add(other.get(chroms[i]).get(j + 1));
								region.add(other.get(chroms[i]).get(j + 2));

								parentMap.put((String) chroms[i], region);
							}
						}

					}
				}

				freq = freqIter.next();
				var = varIter.next();
				pos = posIter.next();
			}

			int cnt = 0;
			while(freqIter.hasNext()) {
				parentMap.get(chroms[i]).add(3, freq);
				freq = freqIter.next();
				cnt++;
			}
			
		}

		System.out.println(parentMap);
		return parentMap;
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