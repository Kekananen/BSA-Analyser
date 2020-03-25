package bsa_analyser.github.io;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.BorderLayout;
import java.awt.Panel;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;


public class Create_BSA_Graph {
    
    public  HashMap<String, HashMap> hashmap_creator(){
    HashMap<Integer, Double> example_hashmap = new HashMap<Integer, Double>();

    
    example_hashmap.put(100, 0.5);
    example_hashmap.put(200, 0.7);   
    example_hashmap.put(300, 0.5);    
    example_hashmap.put(400, 0.3);
    
        HashMap<Integer, Double> example_hashmap2 = new HashMap<Integer, Double>();
    
    
    example_hashmap2.put(100, 0.1);
    example_hashmap2.put(200, 0.3);   
    example_hashmap2.put(300, 0.5);    
    example_hashmap2.put(400, 0.3);
    HashMap<String, HashMap> draft_bsa = new HashMap<String, HashMap>();
    draft_bsa.put("1", example_hashmap);
    draft_bsa.put("2", example_hashmap2);
    return draft_bsa;
    }
    
    public JFreeChart createGraph(HashMap<String, HashMap> bsa_hash, String chromosome){
    //public JFreeChart createGraph(){
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
   HashMap<Integer, Double> plot_hashmap = new HashMap<Integer, Double>();
    plot_hashmap = bsa_hash.get(chromosome);
    plot_hashmap = bsa_hash.get(chromosome);
    String phenotype = "wildtype";
    for (Map.Entry<Integer, Double> entry : plot_hashmap.entrySet()) {
    int key = entry.getKey();
    double value = entry.getValue();
    dataset.addValue(key, phenotype, String.valueOf(value));
  
}
    
    

        JFreeChart chart = ChartFactory.createLineChart("Method graph", "Position", "Number of SNPs", dataset);
        
        ChartPanel CP = new ChartPanel(chart);
        //CP.repaint();
        //add chart to panel
        BSA_Visualisation.jPanel9.setLayout(new java.awt.BorderLayout());
        BSA_Visualisation.jPanel9.add(CP, BorderLayout.CENTER);
        BSA_Visualisation.jPanel9.setVisible(true);
        BSA_Visualisation.jPanel9.validate();
        return chart;
        //return chart;
        
    }
    
}