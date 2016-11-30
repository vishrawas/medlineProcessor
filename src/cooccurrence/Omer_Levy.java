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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
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
public class Omer_Levy {

//    static long totalSteps = 1000000;
    static int D = 300;

    public static void main(String args[]) {
        String path = "";
        String writePath = "";
        BufferedReader br = null;
        ArrayList<String> files = new ArrayList<>();
        //reading all the files in the directory
        //each file is PPMI matrix for an year
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

            //creating the matrices for storing top rank-d matrices of SVD 
            RealMatrix matrixUd = MatrixUtils.createRealMatrix(D, D);
            RealMatrix matrixVd = MatrixUtils.createRealMatrix(D, D);
            RealMatrix coVarD = MatrixUtils.createRealMatrix(D, D);
            
            //populating the matrices based on the co-occur hashmap
            populateMatrixR(matrixR, cooccur, rowStrings, colStrings);
            
            //computing the svd
            SingularValueDecomposition svd = new SingularValueDecomposition(matrixR);
            
            //extracting the components of SVD factorization
            RealMatrix U = svd.getU();
            RealMatrix V = svd.getV();
            RealMatrix coVariance = svd.getCovariance(-1);
            
            //list to store indices of top-D singular values of coVar. 
            //Use this with rowsString (colStrings) to get the corresponding word and context
            ArrayList<Integer>indicesD = new ArrayList<>();
            //Extract topD singular value from covariance to store in coVarD and
            //extract corresponding columns from U and V to store in Ud and Vd
            getTopD(U, V, coVariance, matrixUd, matrixVd, coVarD,indicesD);
            //calulate the squareRoot of coVarD
            RealMatrix squareRootCoVarD = squareRoot(coVarD);
           RealMatrix W_svd = matrixUd.multiply(squareRootCoVarD);
           RealMatrix C_svd = matrixVd.multiply(squareRootCoVarD);
        }
    }
/**
 * Outer Method to read the file content and populate it in the cooccur hashmap
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
 * Method to insert a triple of from string, to String and the weight to the coocurrence hashMap
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
                matrixR.setEntry(i, j, val);
            }
            iter.remove();
        }
    }

/**
 * Method that will extract top D singular values from CoVariance Matrix 
 * It will then identify the corresponding columns from U and V and add it to new matrices 
 * @param U
 * @param V
 * @param coVariance
 * @param matrixUd
 * @param matrixVd
 * @param coVarD
 * @param indicesD 
 */
    private static void getTopD(RealMatrix U, RealMatrix V, RealMatrix coVariance, RealMatrix matrixUd, RealMatrix matrixVd, RealMatrix coVarD,ArrayList<Integer>indicesD) {
        TreeMap<Double, Set<Integer>> tmap = new TreeMap<>();
        for (int i = 0; i < coVariance.getRowDimension(); i++) {
            double val = coVariance.getEntry(i, i);
            if (tmap.containsKey(val)) {
                Set<Integer> temp = tmap.get(val);
                temp.add(i);
            } else {
                Set<Integer> temp = new HashSet<>();
                temp.add(i);
                tmap.put(val, temp);
            }
        }
        Iterator iter = tmap.keySet().iterator();
        while (iter.hasNext()) {
            Double val = (Double) iter.next();
            Set<Integer> indices = tmap.get(val);
            for (int i = 0; i < indices.size(); i++) {
                Iterator iterIndices = indices.iterator();
                while (iterIndices.hasNext()) {
                    int index = (Integer) iterIndices.next();
                    indicesD.add(index);
                    coVarD.addToEntry(index, index, val);
                    matrixUd.setColumnVector(index, U.getColumnVector(index));
                    matrixVd.setColumnVector(index, V.getColumnVector(index));
                }
            }
        }

    }
/**
 * Generic Method to take squareRoot of individual Values.
 * @param coVariance
 * @return 
 */
    private static RealMatrix squareRoot(RealMatrix coVariance) {
        RealMatrix squareRoot = MatrixUtils.createRealMatrix(coVariance.getRowDimension(), coVariance.getColumnDimension());
        for(int i=0;i<coVariance.getRowDimension();i++){
            for(int j=0;j<coVariance.getColumnDimension();j++){
                double val = coVariance.getEntry(i, j);
                val = Math.sqrt(val);
                squareRoot.addToEntry(i, j, val);
            }
        }
        return squareRoot;
    }

}
