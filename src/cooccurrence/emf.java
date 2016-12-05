/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cooccurrence;

import static cooccurrence.pmi.listFilesForFolder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author vishrawa
 */
public class emf {

    public static void main(String args[]) {
        String path = "";
        String writePath = "";
        BufferedReader br = null;
        ArrayList<String> files = new ArrayList<>();
        listFilesForFolder(new File(path), files);
        for (String filePath : files) {
            System.out.println(filePath);
            String fileName = new File(filePath).getName();

            //data structure to store the PPMI matrix in the file
            HashMap<String, HashMap<String, Double>> cooccur = new HashMap<>();
            readFileContents(filePath, cooccur); //reading the file and storing the content in the hashmap
            //Because Matrices are identified by row and col id, the following 
            //lists maps id to corresponding string. Note that matrix is symmetric. 
            ArrayList<String> rowStrings = new ArrayList<>(cooccur.keySet());
            ArrayList<String> colStrings = new ArrayList<>(cooccur.keySet());

            //creating matrix with given dimensions and initializing it to 0
            RealMatrix matrixR = MatrixUtils.createRealMatrix(rowStrings.size(), colStrings.size());
             //populating the matrices based on the co-occur hashmap
            populateMatrixR(matrixR, cooccur, rowStrings, colStrings);
            
            
            
        }
    }

    /**
     * Outer Method to read the file content and populate it in the cooccur
     * hashmap
     *
     * @param filePath
     * @param cooccur
     * @return
     */
    private static long readFileContents(String filePath, HashMap<String, HashMap<String, Double>> cooccur) {
        long totalCount = 0;
        try {

            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = "";
            while ((line = br.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line, "\t");
                if (tok.countTokens() == 3) {
                    String from = tok.nextToken();
                    String to = tok.nextToken();
                    Double count = Double.parseDouble(tok.nextToken());
                    totalCount += count;
                    addToMatrix(from, to, count, cooccur);

                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(pmi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(pmi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return totalCount;
    }

    /**
     * Method to insert a triple of from string, to String and the weight to the
     * coocurrence hashMap
     *
     * @param from
     * @param to
     * @param count
     * @param matrix
     */
    private static void addToMatrix(String from, String to, Double count, HashMap<String, HashMap<String, Double>> matrix) {
        HashMap<String, Double> innerMatrix;
        if (matrix.containsKey(from)) {
            innerMatrix = matrix.get(from);
        } else {
            innerMatrix = new HashMap<>();
        }
        if (innerMatrix.containsKey(to)) {
            Double countTemp = innerMatrix.get(to);
            countTemp = countTemp + count;
            innerMatrix.put(to, countTemp);
            matrix.put(from, innerMatrix);
        } else {
            innerMatrix.put(to, count);
            matrix.put(from, innerMatrix);
        }

    }

    /**
     * Method to populate the apache matrix from cooccur hashmap
     *
     * @param matrixR
     * @param cooccur
     * @param rowStrings
     * @param colStrings
     */
    private static void populateMatrixR(RealMatrix matrixR, HashMap<String, HashMap<String, Double>> cooccur, ArrayList<String> rowStrings, ArrayList<String> colStrings) {
        Iterator iter = cooccur.keySet().iterator();

        while (iter.hasNext()) {
            String row = iter.next().toString();
            int i = rowStrings.indexOf(row);
            HashMap<String, Double> inner = cooccur.get(row);
            for (String col : inner.keySet()) {
                int j = colStrings.indexOf(col);
                double val = inner.get(col);
                matrixR.setEntry(j, i, val); // each column in D represents the vector w-> d_w
            }
            iter.remove();
        }

    }
}
