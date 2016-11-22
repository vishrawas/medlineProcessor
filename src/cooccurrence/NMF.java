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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.fitting.leastsquares.GaussNewtonOptimizer;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;


/**
 *
 * @author vishrawa
 */
public class NMF {
    static long totalSteps = 1000000;
    static int K = 300;
    public static void main(String args[]){
        String path = "";
        String writePath = "";
        BufferedReader br = null;
        ArrayList<String> files = new ArrayList<>();
        listFilesForFolder(new File(path), files);
         for (String filePath : files) {
            System.out.println(filePath);
            String fileName = new File(filePath).getName();

            HashMap<String, HashMap<String, Double>> cooccur = new HashMap<>();
            long totalCount = readFileContents(filePath, cooccur);
            ArrayList<String>rowStrings = new ArrayList<>(cooccur.keySet());
            ArrayList<String>colStrings = new ArrayList<>(cooccur.keySet());
           
           RealMatrix matrixR = MatrixUtils.createRealMatrix(rowStrings.size(),colStrings.size());
          
            RealMatrix matrixP = MatrixUtils.createRealMatrix(rowStrings.size(),K);
             RealMatrix matrixQ = MatrixUtils.createRealMatrix(K,colStrings.size());
             populateMatrixR(matrixR,cooccur,rowStrings,colStrings);
            init(matrixP);
            init(matrixQ);
              SingularValueDecomposition svd = new SingularValueDecomposition(matrixR);
              RealMatrix U = svd.getU();
              RealMatrix V = svd.getV();
              RealMatrix coVariance = svd.getCovariance(-1);
              // take square root of covariance
              // take dot product of U with sqrt.covar ==> W and take dot product of V with sqrt.covar
              
        
//            factorize(matrixR,matrixP,matrixQ,rowStrings,colStrings);
            
        }
         
        
    }
    
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

    private static void init(RealMatrix matrix) {
        Random R = new Random();
        for(int i=0;i<matrix.getRowDimension();i++){
            double row[]=matrix.getRow(i);
            Arrays.fill(row, Math.random()*Double.MAX_VALUE);
       }
    }

//    private static void factorize(RealMatrix matrixR, RealMatrix matrixP, RealMatrix matrixQ, ArrayList<String>rowStrings, ArrayList<String>colStrings) {
//        for(int steps=0;steps<totalSteps;steps++){
//           RealMatrix tempmatrixR = matrixP.multiply(matrixQ);
//           RealMatrix errorMatrixR = computeError(tempmatrixR,matrixR);
//           
//        }
//    }

    private static void populateMatrixR(RealMatrix matrixR, HashMap<String, HashMap<String, Double>> cooccur, ArrayList<String> rowStrings, ArrayList<String> colStrings) {
        Iterator iter = cooccur.keySet().iterator();
        
        while(iter.hasNext()){
            String row = iter.next().toString();
            int i = rowStrings.indexOf(row);
            HashMap<String,Double>inner = cooccur.get(row);
            for(String col:inner.keySet()){
                int j=colStrings.indexOf(col);
                double val = inner.get(col);
                matrixR.setEntry(i, j, val);
            }
            iter.remove();
        }
    }

//    private static RealMatrix computeError(RealMatrix tempmatrixR, RealMatrix matrixR) {
//      RealMatrix error =  matrixR.subtract(tempmatrixR);
//      error.
//      return error;
//    }

  
}
