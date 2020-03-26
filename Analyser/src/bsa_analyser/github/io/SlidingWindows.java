/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bsa_18032020;

import com.sun.javafx.property.adapter.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author tee_t
 */
public class SlidingWindows {

    private HashMap<String, HashMap<Integer, ArrayList<String>>> CreateRegionWindows(ArrayList<String> file, int regionSizeInput, int incrementInput) { //input chromosome sequence and listen for window size input by user.

        //Step 1. Store each variant line and get the chromosome lengths.
        ArrayList<String> variantList = new ArrayList<String>(); //create empty arraylist to store each line as a String[]
        Iterator fileIter = file.iterator(); //iterate through file
        HashMap<String, Integer> chromLengthList = new HashMap<String, Integer>(); //store contig length
        while (fileIter.hasNext()) {
            String variant = fileIter.next().toString();
            if (variant.toString().startsWith("##contig")) {
                String chromNo = variant.split("=|,")[2];

                int length = Integer.parseInt(variant.split("=|,|>")[4]);
                chromLengthList.put(chromNo, length);
            } else if (variant.toString().startsWith("#")) {
                continue;
            } else {
                variantList.add(variant);
            }
        }

        //Step 2. Get window size by converting first user input (regionSizeInput) to integer
        //String size = regionSizeInput.toString(); //convert action to string
        //int regionSize = Integer.parseInt(size); //convert string to integer
        //Step 3. Get increment size by converting second user input (incrementInput) to integer.
        //String inc = incrementInput.toString(); //convert input to string.
        //int incSize = Integer.parseInt(inc); //convert increment size to integer.
        //Step 4. Get the list of chromosomes.
        Iterator VLIter1 = variantList.iterator(); //iterate through variant list created Step 1.
        ArrayList chromList = new ArrayList(); //blank ArrayList to store chromosome numbers.

        while (VLIter1.hasNext()) {
            String[] variant = VLIter1.next().toString().split("\t");
            String chromNo = variant[0];
            if (!chromList.contains(chromNo)) { //if the chromosome is not already in the ArrayList.
                chromList.add(chromNo); //then add the chromosome number to the ArrayList.
            } else {

            }
        }
        //System.out.println(chromList);
        /*Step 5.
        * Create a HashMap (finalHS) that will store the variants in each region window
        * for each chromosome.
        * Create a HashMap to store the regionWindows in (Integer regionStart,
        * ArrayList innerVariantList).
        * The ArrayList innerVariantList will store each variant line from the
        * vcf file for a given chromosome and within the regionSizeInput range.
        * 
        *
         */
        HashMap<String, HashMap<Integer, ArrayList<String>>> finalHS = new HashMap<String, HashMap<Integer, ArrayList<String>>>(); //create new hashmap to store regions list for all chromosomes
        HashMap<Integer, ArrayList<String>> regionWindows = new HashMap<Integer, ArrayList<String>>(); //create HS to store the regionWindows in, with key as the start position of the window

        Iterator CNIt = chromList.iterator();
        while (CNIt.hasNext()) { //for each chromosome, while there is a next.
            String chromNo = CNIt.next().toString(); //store the chromosome number
            int chromLength = chromLengthList.get(chromNo);
            
            for (int regionStart = 0; regionStart + regionSizeInput <= chromLength; regionStart += incrementInput) {
                Iterator VLIter2 = variantList.iterator();
                ArrayList<String> innerVariantList = new ArrayList<String>(); //for a single window, create an empty arraylist to store the variants.
                    
                while (VLIter2.hasNext()) { //while there is a next item to iterate to in the variant list
                    String variant = VLIter2.next().toString(); //store the next variant string
                    int position = Integer.parseInt(variant.split("\t")[1]); // and get the position as an integer

                    //System.out.println(regionStart);
                    if (variant.startsWith(chromNo) && position >= regionStart && position <= regionStart + regionSizeInput) {

                        //add the variant to the ArrayList of regionWindows
                        //System.out.println(chromNo + " and " + regionStart + ":\t" + position + "\t" + variant.split("\t")[0]);
                        innerVariantList.add(variant);
                        
                        //regionWindows.putIfAbsent(regionStart, innerVariantList);
                        //System.out.println(innerVariantList);
                        //System.out.println(innerVariantList.get(innerVariantList.indexOf(variant)));
                    } else {
                        continue;
                    }
                    
                }
                    regionWindows.put(regionStart, innerVariantList);

                    //System.out.println(regionStart + regionWindows.get(regionStart).toString());
                
            }
            finalHS.put(chromNo, regionWindows);

            //System.out.println(chromNo + "\t" + regionWindows.toString().contains("SL2.50ch01"));
            //System.out.println(chromNo + "\t" + finalHS.get(chromNo));
        }

        return finalHS;
    }

    ;
    
     
     /*
     *
     *
     */
     
     
    private HashMap<String, HashMap<Integer, ArrayList<Double>>> freqFinder(HashMap<String, HashMap<Integer, ArrayList<String>>> pool) {

        //ArrayList<String> chromNoList = new ArrayList();
        Iterator chromNoIter = pool.entrySet().iterator(); //iterate through chromosome keys
        /*while (chromNoIter.hasNext() && !chromNoList.contains(chromNoIter.next())) {
                chromNoList.add(chromNoIter.next().toString()); //add chromosome string if not present in list already.
                }*/

        // to the HashMap list relating to that chromosome with the helper function.
        HashMap<String, HashMap<Integer, ArrayList<Double>>> regionMap = new HashMap<String, HashMap<Integer, ArrayList<Double>>>();
        // Holds the values to be placed in the HashMap.

        while (chromNoIter.hasNext()) {
            Entry chromAndWindows = (Entry) chromNoIter.next();
            String chrom = chromAndWindows.getKey().toString();
            //System.out.println(chrom);
            //Spliterator exampleHS = pool.entrySet().spliterator();
            HashMap innerHS = (HashMap) chromAndWindows.getValue(); //get a chromosomes HashMap of regions and their list of individual variants.
            //Set set = innerHS.entrySet();
            Iterator windowIterator = innerHS.entrySet().iterator(); //get entryset of window and list of values to maintain order
            HashMap<Integer, ArrayList<Double>> regionWindows = new HashMap<Integer, ArrayList<Double>>(); //holds list of windows and their values in an arraylist

            while (windowIterator.hasNext()) {
                Entry<Integer, ArrayList<String>> windowAndValues = (Entry) windowIterator.next();
                int windowNo = Integer.parseInt(windowAndValues.getKey().toString());
                //
                //String window = windowIterator.next().toString();
                //int windowNo = Integer.parseInt(window);
                ArrayList<String> variantList = (ArrayList) windowAndValues.getValue();

                ArrayList<Double> region = new ArrayList<Double>(); //holds individual MAF estimates for a single region window.
                Iterator varListIter = variantList.iterator();
                //System.out.println("Window start:" + windowNo);
                while (varListIter.hasNext()) {
                    String variant = varListIter.next().toString();

                    //System.out.println("key: " + windowNo + "\t value: " + variant);
                    String[] line = variant.split("\t");
                    //System.out.println(variant);
                    double vaf = 0;

                    if (line.length != 10) {
                        //System.out.println(variant);

                    } else if (line == null) {
                        //System.out.println("hit");

                    } else {

                        if (!line[9].equals("./.") && line.length == 10 && line[0].equals(chrom)) {

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
                                    //EDIT: BELOW LINE BELONGED TO KAT ORIGINALLY
                                    //GT = varInfo[GTindex];
                                    GT = line[9].split(":")[GTindex];
                                } else {
                                    return null;
                                }
                                if (PLindex != -1) {
                                    PL = line[9].split(":")[PLindex];
                                } else {
                                    return null;
                                }
                                if (ADindex != -1) {
                                    //EDIT: BELOW LINE BELONGED TO KAT ORIGINALLY
                                    //AD = varInfo[ADindex];

                                    //Add AD values
                                    AD = line[9].split(":")[ADindex];
                                } else {
                                    System.out.println("hit");
                                    return null;
                                }
                                if (DPindex != -1) {
                                    //EDIT: BELOW LINE BELONGED TO KAT ORIGINALLY
                                    //DP = varInfo[ADindex];

                                    //Add DP values
                                    DP = line[9].split(":")[DPindex];
                                } else {
                                    System.out.println("hit1");
                                    return null;
                                }

                                // The variant calculated from the PL variable taking into account both
                                // heterozygous counts and homozygous counts for the variant allele.
                                String minor = "";
                                //Tien's code starts here
                                //If the GT is homozygous reference (0/0) then store in noWTreads
                                if (GT == "0/0") {
                                    continue;
                                } else if (GT.split("/")[0].equals(GT.split("/")[1])) {
                                    double noWTreads = (Double.parseDouble(AD.split(",")[0])) / 2;
                                    double noMutantReads = (Double.parseDouble(AD.split(",")[1])) / 2;

                                    vaf = noMutantReads / (noWTreads + noMutantReads);
                                    region.add(vaf);
                                    //System.out.println(variant + "\n" + vaf);
                                } else {
                                    double noMutantReads = Double.parseDouble(AD.split(",")[1]);
                                    vaf = noMutantReads / Double.parseDouble(DP);
                                    region.add(vaf);
                                    //System.out.println(variant + "\n" + vaf);

                                }

                                //Below is Kat's code. Need to adjust for AD:DP instead
                                /*
                                                        if (Double.parseDouble(PL.split(",")[2]) > Double.parseDouble(PL.split(",")[0])) {
								Double var = Double.parseDouble(PL.split(",")[2])
										+ Double.parseDouble(PL.split(",")[1]) / 2;
								vaf = var / (Double.parseDouble(PL.split(",")[0]) + var);
							} else {
								Double var = Double.parseDouble(PL.split(",")[0])
										+ Double.parseDouble(PL.split(",")[1]) / 2;
								vaf = var / (Double.parseDouble(PL.split(",")[0]) + var);
							} */
                            } else {
                                vaf = 0;
                                //System.out.println(variant + "\n" + vaf);

                                region.add(vaf);
                            }
                        } else {
                            vaf = 0;
                            //System.out.println(variant + "\n" + vaf);

                            region.add(vaf);
                        }
                        regionWindows.put(windowNo, region);
                    }
                    //region.add(vaf);
                    //System.out.println(chrom + "\t" + windowNo + "\t" + regionWindows.get(windowNo));

                }

                regionMap.put(chrom, regionWindows);

            }
            HashMap windowExample = (HashMap) regionMap.get(chrom);
            ArrayList arrayExample1 = (ArrayList) windowExample.get(0/*200000*/);
            ArrayList arrayExample2 = (ArrayList) windowExample.get(16800000);

            System.out.println(chrom + "\nWindow 0: " + arrayExample1 + "\nWindow 16800000: " + arrayExample2);
        }
        /*
                    Iterate through chromosome keys, for each chromosome key, get each window
                     key and iterate through the list of variant lines for a given window
                     to calculate the values
         */
        return regionMap;

    }

    /*
    *The below method calculates the average for each window.
     */
    private HashMap<String, HashMap<Integer, Double>> CalculateAvgMAFEstimate(HashMap<String, HashMap<Integer, ArrayList<Double>>> regionMap) {
        //Iterate through each window and calculate the average of the values. Return the 

        //Iterate through chromosomes
        HashMap<String, HashMap<Integer, Double>> finalHS = new HashMap<>();
        Iterator chromIter = regionMap.entrySet().iterator();
        while (chromIter.hasNext()) { //for each chromosome
            Entry chromAndInnerHSset = (Entry) chromIter.next();

            String chromNo = chromAndInnerHSset.getKey().toString();
            HashMap<Integer, ArrayList<Double>> innerHS = (HashMap<Integer, ArrayList<Double>>) chromAndInnerHSset.getValue(); //get 

            Iterator innerHSIter = innerHS.entrySet().iterator(); //iterate through innerHS windows
            HashMap<Integer, Double> regionWindow = new HashMap<>(); //create new HS to store regions and avg value

            while (innerHSIter.hasNext()) {
                Entry entry = (Entry) innerHSIter.next();

                int windowStartPos = Integer.parseInt(entry.getKey().toString());
                ArrayList<Double> MAFValueList = (ArrayList<Double>) entry.getValue();

                int noOfMAFvalues = MAFValueList.size();
                double avgMAFValue = 0;
                Iterator valueIter = MAFValueList.iterator();
                double sumMAFValue = 0;

                for (double indMAFValue : MAFValueList) { //iterate through values in a single window
                    //double indMAFValue = MAFValueList.get(i);
                    //double indMAFValue = Double.parseDouble(valueIter.next().toString());
                    //System.out.println("individualMAFValue: " + indMAFValue);
                    sumMAFValue += indMAFValue;

                }
                avgMAFValue = sumMAFValue / noOfMAFvalues;

                //System.out.println("AvgMAFValue: " + avgMAFValue);
                regionWindow.put(windowStartPos, avgMAFValue);

            }
            finalHS.put(chromNo, regionWindow);
            System.out.println("chromNo: " + chromNo + "\tRegionWindow: " + finalHS.get(chromNo));
            
            HashMap exampleInnerHS = (HashMap) finalHS.get(chromNo);
            double exampleArray1 = (double) exampleInnerHS.get(0);
            double exampleArray2 = (double) exampleInnerHS.get(16800000);
            System.out.println("Average: " + chromNo + "\nWindow 0: " + exampleArray1 + "\nWindow 16800000: " + exampleArray2);

        }

        return finalHS;
    }

    /* THIS METHOD IS NOT COMPLETE YET.
    * The below method accepts a HashMap of sliding regionWindows for a vcf file with
    * the chromosome number as a key. The other input is a String returned from
    * a menu option picked by the user to describe the method they wanted.
    * If no method was picked, then the method will be set to default.
     */
    public HashMap<String, HashMap<Integer, Double>> CalculateWindows(ArrayList<String> file, int regionSizeInput, int incrementInput, String method) {

        HashMap<String, HashMap<Integer, Double>> values = new HashMap<String, HashMap<Integer, Double>>(); //to be returned

//Iterator finalHSiter = finalHS.keySet().iterator(); //iterate through the regionWindows chromosomes
        if (method.equals("Homozygosity Mapping")) {
            //use this Homozygosity Mapping method
            //INSERT METHOD HERE
            //Note that common variants do not need to be run for this.
        } else if (method.equals("Allelic Distance")) {
            //use Allelic Distance Mapping method
            //USE METHOD HERE
            //Use F2 WT and mutant pools.
        } else if (method.equals("MAF Estimation")) {
            //use MAF Estimation method
            //USE METHOD HERE
            //MAFCalculation mafe = new MAFCalculation(); //not complete yet
            SlidingWindows t = new SlidingWindows();
            HashMap<String, HashMap<Integer, ArrayList<String>>> indRegionWindowsHS = t.CreateRegionWindows(file, regionSizeInput, incrementInput);

            HashMap indValues = t.freqFinder(indRegionWindowsHS);
            //values = t.freqFinder(indRegionWindowsHS);
            values = t.CalculateAvgMAFEstimate(indValues);

        } else {
            //use default method for that mapping population.
            //USE METHOD HERE
            //Call MAF twice
        }

        /*for (int i = 0; i<=windowSize; i++) { //for each value in the window
                float value = (float) finalHS.get(i); //get the value of each item in the window
                windowAvg = windowAvg + value; //add the value to the running sum
         */
        System.out.println("finished");
        return values;
    }
}
