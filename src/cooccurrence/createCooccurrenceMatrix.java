/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cooccurrence;

import static cooccurrence.cooccurPreprocess.baseCoccur;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vishrawa
 */
public class createCooccurrenceMatrix {

    public static void main(String args[]) {
        BufferedReader br = null;
        String path = "C:\\Users\\vishrawa\\Documents\\Research\\medline_prj\\output\\all.txt";

//        FileInputStream inputStream = null;
//Scanner sc = null;
        int maxYear = 2015;

        try {
//            inputStream = new FileInputStream(path);
           
            
            while (maxYear > 0) {
                
                 br = new BufferedReader(new FileReader(path));
//                sc = new Scanner(inputStream, "UTF-8");
                System.out.println(maxYear);
                HashMap<String, HashMap<String, Long>> matrix = new HashMap<>();
                String writePath = "C:\\Users\\vishrawa\\Documents\\Research\\medline_prj\\output\\";

                String from;
                String to;
                int year;
                long count;
                String line;
                
                while((line=br.readLine())!=null){
//                while (sc.hasNextLine()){
//                        line = sc.nextLine();
            
                    
                    StringTokenizer tok = new StringTokenizer(line, "\t");
                    if (tok.countTokens() == 4) {
                        from = tok.nextToken();
                        to = tok.nextToken();
                        year = Integer.parseInt(tok.nextToken());
                        count = Long.parseLong(tok.nextToken());
                        if (year <= maxYear) {
                            addToMatrix(from, to, count, matrix);
                        }
                      
                    }
                }
                System.out.println("writing--> "+maxYear);
                writeCompleteCooccur(writePath + "Coccur" + "_" + maxYear, matrix);
                maxYear--;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(createCooccurrenceMatrix.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(createCooccurrenceMatrix.class.getName()).log(Level.SEVERE, null, ex);
        }  finally {
            
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(createCooccurrenceMatrix.class.getName()).log(Level.SEVERE, null, ex);
            }
             
        }
    }

    private static void addToMatrix(String from, String to, long count, HashMap<String, HashMap<String, Long>> matrix) {
        HashMap<String, Long> innerMatrix;
        if (matrix.containsKey(from)) {
            innerMatrix = matrix.get(from);
        } else {
            innerMatrix = new HashMap<>();
        }
        if (innerMatrix.containsKey(to)) {
            long countTemp = innerMatrix.get(to);
            countTemp = countTemp + count;
            innerMatrix.put(to, countTemp);
            matrix.put(from, innerMatrix);
        } else {
            innerMatrix.put(to, count);
            matrix.put(from, innerMatrix);
        }

    }

    private static void writeCompleteCooccur(String completeCooccurPath, HashMap<String, HashMap<String, Long>> baseCoccur) {
        BufferedWriter bw = null;
                  
        try {
            bw = new BufferedWriter(new FileWriter(new File(completeCooccurPath)));
       
            for (String s : baseCoccur.keySet()) {
                HashMap<String, Long> temp = baseCoccur.get(s);
                for (String ss : temp.keySet()) {
                    Long freq = temp.get(ss);
                    bw.append(s + "\t" + ss + "\t" + freq + "\n");
                }
            }
          
        
        } catch (IOException ex) {
            Logger.getLogger(cooccurPreprocess.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bw.close();

            } catch (IOException ex) {
                Logger.getLogger(cooccurPreprocess.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
