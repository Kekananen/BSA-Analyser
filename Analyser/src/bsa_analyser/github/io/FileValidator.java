/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bsa_analyser.github.io;

import java.io.*;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author jkell
 */


public class FileValidator {


    /**
     * Checks the content within the selected VCF to validate this file as a
     * VCF file
     *
     * @param user_file path to the user file
     */
    public static void fasta_content_checker(File user_file) {

        /**
         * Two checks are done here the first is to check if each contigs have ATGC
         * and the second check is for checking if the Contigs are Align with the fasta format
         */

        boolean b = false;
        //List<String> temps = new ArrayList<>();
        List<String> head = new ArrayList<>();
        List<String> contigSequences = new ArrayList<>();
        String letters = "";
        String valid = "ATGC";
        String check = "";
        BufferedReader read = null;

        // int user_option = -1;
        List<Integer> badContig = new ArrayList<>();
        if (user_file.getName().endsWith("fasta") || user_file.getName().endsWith("fa")) {
            if (user_file.length() == 0) {
                int user_option = JOptionPane.showConfirmDialog(null, "This Fasta File is Corrupt "
                        + " This means the content may not be a Fasta file format. Do you wish to upload another file?"
                        + "As such we can not allow you to upload this file, Please re-upload a new file "
                        + " (error code 1)");
                //allows the user to retry the upload with an alternate file
                if (user_option == 0) {
                    BSA_Visualisation.upload_files();

                    return;
                } else {
                    JOptionPane.showMessageDialog(null, "this Fasta file has not being verified and it might ruin your analysis");
                    return;
                }
            }
            try {
                BufferedReader br = new BufferedReader(new FileReader((user_file)));
                String line = "";

                read = new BufferedReader(new FileReader((user_file)));
                {

                    if ((!read.readLine().startsWith(">"))) {
                        int user_option = JOptionPane.showConfirmDialog(null, "This Fasta File is Corrupt "
                                + " This means the content may not be a Fasta file format. Do you wish to upload another file?"
                                + "As such we can not allow you to upload this file, Please re-upload a new file "
                                + " (error code 1)");
                        //allows the user to retry the upload with an alternate file
                        if (user_option == 0) {
                            BSA_Visualisation.upload_files();
                            return;
                        } else {
                            return;
                        }
                    }


                }
                read.close();

                // keep track of index of last scaffold in list, to link with index of corresponding contig sequence
                int i = -1;
                boolean lineRead = false;
                boolean first = true;
                while (((line = br.readLine()) != null)) {
                    if (line.isEmpty()) {
                        int user_option = JOptionPane.showConfirmDialog(null, "This Fasta File is Corrupt "
                                + " This means the content may not be a Fasta file format. Do you wish to upload another file?"
                                + "As such we can not allow you to upload this file, Please re-upload a new file "
                                + " (error code 1)");
                        //allows the user to retry the upload with an alternate file
                        if (user_option == 0) {
                            BSA_Visualisation.upload_files();
                            return;
                        } else {
                            return;
                        }
                    }

                    /**
                     * First check to see if the Contig sequence has ATGC, so this was done by storing each contigSequence
                     * into an arraylist, so that i can run a check in the element has ATGC. and if it has, then it add it to badContig list
                     *
                     */

                    if (line.substring(0, 1).equals(">")) {
                        if (i > -1) {
                            if (letters.length() != 4) {
                                badContig.add(contigSequences.size() - 1);
                            }
                        }

                        letters = "";

                        head.add((line));
                        lineRead = true;
                        i++;

                    } else {
                        //line.toUpperCase(); // changed it to uppercase so as to be able to deal with the small atgc
                        if (letters.length() != 4) {
                            for (int k = 0; k < line.length(); k++) {
                                check = line.substring(k, k + 1);
                                if (valid.contains(check)) {
                                    if (!letters.contains(check)) {
                                        letters += check;
                                    }
                                }
                            }
                            if (lineRead) {
                                contigSequences.add(line + "\n");

                                lineRead = false;
                            } else {
                                contigSequences.set(i, contigSequences.get(i) + line + "\n");
                            }
                        }
                    }
                    /**
                     * This check if the Fasta file align to the fasta file format of
                     * no white space and no other char apart from ATGC and N
                     */
                    if ((!line.contains("A|T|G|C|N")) && (!line.startsWith(">"))) {
                        //while (((line = br.readLine() != null)) {
                        for (int n = 0; n < line.length(); n++) {
                            b = true;
                            break;
                        }
                    }
                }
//                for (int k = 0; k < badContig.size(); k++) {
//                    System.out.println(k);
//                }

            } catch (FileNotFoundException ex) {
                System.out.println(ex);
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
        if (!badContig.isEmpty()) {
            int user_option = JOptionPane.showConfirmDialog(null, "This Fasta File is Corrupt "
                    + " This means the content may not be a Fasta file format. Do you wish to upload another file?"
                    + "As such we can not allow you to upload this file, Please re-upload a new file "
                    + " (error code 1)");
            //allows the user to retry the upload with an alternate file
            if (user_option == 0) {
                BSA_Visualisation.upload_files();
                return;
            } else {
                return;
            }
        }
            if ((head.size() != contigSequences.size()) || (head.isEmpty()) || (contigSequences.isEmpty())) {
               int user_option = JOptionPane.showConfirmDialog(null, "This Fasta File is Corrupt "
                        + " This means the content may not be a Fasta file format. Do you wish to upload another file?"
                        + "As such we can not allow you to upload this file, Please re-upload a new file "
                        + " (error code 1)");
                //allows the user to retry the upload with an alternate file
                if (user_option == 0) {
                    BSA_Visualisation.upload_files();
                    return;
                } else {
                    return;
                }

            }
        }



        public static void vcf_content_checker(String user_file) {
            int size_of_header = 0;
            //Keep track of what line the file is on.
            int line_counter =1;
       
        try {
                BufferedReader reader = new BufferedReader(new FileReader(user_file));
                //Check the first line of the file as this should contain fileformat information and place it into the variable 
                String file_format_name = reader.readLine();
                line_counter +=1;
                //if the first line does not contain the correct format warn the user
                if (!file_format_name.contains("##fileformat=VCF")) {
                    //initiise int to get the user's response from the file dialog box. If the user selects 'No' it returns the value 1
                    //if the user selects 'yes' it returns 0.
                    int user_option = JOptionPane.showConfirmDialog(null, "This file "
                            + " does not have fileformat information as the first line."
                            + " This means the content may not be a VCF file format. Do you wish to upload another file?"
                            + " (error code 2)");
                    //allows the user to retry the upload with an alternate file
                    if (user_option == 0) {
                        BSA_Visualisation.upload_files();
                    } else {
                        
                        System.out.println("WARNING - File not verified as being .vcf (error code 2)");
                    }
                    
                }
                Boolean header_line = FALSE;
                String line;
                while ((line = reader.readLine()).startsWith("#")) {
                    if (line.startsWith("#CHROM")) {
                        if (line.contains("POS\tID\tREF\tALT\tQUAL\tFILTER\tINFO"))
                            //change the boolean to TRUE to allow the program to continue as there is a header line present
                        header_line = TRUE;
                        //Initiate a new arraylist for the VCF content
                        List<String> vcf_line_content = new ArrayList<>(Arrays.asList(line.split("\t")));
                        //Get the size of this list into a new variable
                        size_of_header = vcf_line_content.size();
                        line_counter+=1;
                        break;
                    } 
                    line_counter+=1;
                }
                
                if (header_line == FALSE){
                    //initiise int to get the user's response from the file dialog box. If the user selects 'No' it returns the value 1
                    //if the user selects 'yes' it returns 0.
                    int user_option = JOptionPane.showConfirmDialog(null, "FATAL - Uploaded"
                            + "VCF file does not contain a header line. Do you want to upload"
                            + "another file or exit the program? ");
                    //allows the user to retry the upload with an alternate file
                    if (user_option == 0) {
                        //bring up new file chooser dialog box. (first implement file chooser method)
                    } else {
                        
                        System.out.println("FATAL - File not verified as being .vcf - not valid header. See error code 3");
                    }
                }
                if (reader.readLine().isBlank()) {
                    line_counter +=1;
                                       //initiise int to get the user's response from the file dialog box. If the user selects 'No' it returns the value 1
                    //if the user selects 'yes' it returns 0.
                    int user_option = JOptionPane.showConfirmDialog(null, "Warning - this file "
                            + "does not appear to contain any variants - do you want to continue? ");
                    //allows the user to retry the upload with an alternate file
                    if (user_option == 0) {
                        //bring up new file chooser dialog box. (first implement file chooser method)
                    } else {
                        
                        System.out.println("Warning - this VCF file does not appear to contain any variants");
                    }
                }
                String line_vcf_content;
                while ((line_vcf_content = reader.readLine()) != null){
                    List<String> vcf_line = new ArrayList<>(Arrays.asList(line_vcf_content.split("\t")));
                    int vcf_line_size = vcf_line.size();
                    if (vcf_line_size != size_of_header) {
                        System.out.println("Warning line" + line_counter + "does not appear to have to correct number of fields");
                        
                    }
                    line_counter +=1;
                    
                }
                
                
                
        }// this will read the first line// this will read the first line
        catch (Exception ex) {
            System.out.println(ex);
        }   
    }
        
    /**
	 * Checks the content within the selected GFF to validate this file as a
         * GFF file
	 * 
	 * @param user_file path to the user file
	 */
        public static void gff_content_checker(String user_file) {
            //Keep track of what line the file is on.
            int line_counter =1;
       
        try {
                BufferedReader reader = new BufferedReader(new FileReader(user_file));
                //Check the first line of the file as this should contain fileformat information and place it into the variable 

                
                String line_gff_content;
                int expected_fields = 9; // expected number of fields in a gff file are 9
                Boolean correct_fields = TRUE;
                
                
                while ((line_gff_content = reader.readLine()) != null){
                    if (!line_gff_content.startsWith("#")) {
                        
                
                    List<String> gff_line = new ArrayList<>(Arrays.asList(line_gff_content.split("\t")));
                    int gff_line_size = gff_line.size();
                    if (gff_line_size != expected_fields) { //checking number of fields
                        System.out.println("Warning line " + line_counter + " does not appear to have to correct number of fields");
                        correct_fields = FALSE;
                    }
                    line_counter +=1;
                    }    
                }               
                
                
        }
        catch (Exception ex) {
            System.out.println(ex);
        }   
    }
        
}
