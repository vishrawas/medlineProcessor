/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cooccurrence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vishrawa
 */
public class pmi {

    public static void main(String args[]) {

        String path = "C:\\Users\\vishrawa\\Documents\\Research\\medline_prj\\output\\cooccur\\"; //inputPath
        String writePath = "C:\\Users\\vishrawa\\Documents\\Research\\medline_prj\\output\\";//OutputPath
        ArrayList<String> files = new ArrayList<>();
        listFilesForFolder(new File(path), files);

        for (String filePath : files) {
            System.out.println(filePath);
            String fileName = new File(filePath).getName();

            HashMap<String, HashMap<String, Long>> cooccur = new HashMap<>();
            long totalCount = readFileContents(filePath, cooccur);
            HashMap<String, Double> prior = new HashMap<>();
            calculatePriorProbability(prior, cooccur, totalCount);
            HashMap<String, HashMap<String, Double>> pmi = new HashMap<>();
            calculatePMI(prior, cooccur, totalCount, pmi);
            writeCompleteCooccur(writePath + "pmi_" + fileName, pmi);
        }
    }

    public static ArrayList<String> listFilesForFolder(final File folder, ArrayList<String> files) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, files);
            } else {
                files.add(fileEntry.getAbsolutePath());
            }
        }
        return files;
    }

    private static long readFileContents(String filePath, HashMap<String, HashMap<String, Long>> cooccur) {
        long totalCount = 0;
        try {

            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = "";
            while ((line = br.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line, "\t");
                if (tok.countTokens() == 3) {
                    String from = tok.nextToken();
                    String to = tok.nextToken();
                    Long count = Long.parseLong(tok.nextToken());
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

    private static void calculatePriorProbability(HashMap<String, Double> prior, HashMap<String, HashMap<String, Long>> cooccur, long totalCount) {
        for (String outerKey : cooccur.keySet()) {
            long count = 0;
            HashMap<String, Long> innerHash = cooccur.get(outerKey);
            for (String innerKey : innerHash.keySet()) {
                count = count + innerHash.get(innerKey);
            }
            double prob = (double) count / totalCount;
            prior.put(outerKey, prob);
        }
    }

    private static void calculatePMI(HashMap<String, Double> prior, HashMap<String, HashMap<String, Long>> cooccur, long totalCount, HashMap<String, HashMap<String, Double>> pmi) {
        Iterator iter = cooccur.keySet().iterator();
        while (iter.hasNext()) {
            String outerKey = iter.next().toString();
            double outerProb = prior.get(outerKey);
            HashMap<String, Long> innerHashMap = cooccur.get(outerKey);
            for (String innerKey : innerHashMap.keySet()) {
                double innerProb = prior.get(innerKey);
                long count = innerHashMap.get(innerKey);
                double prob = (double) count / totalCount;
                double pmi_score = prob / (outerProb * innerProb);
                pmi_score = Math.log(pmi_score);
                if (pmi_score < 0) {
                    pmi_score = 0;
                }
                addToPMIMatrix(outerKey, innerKey, pmi_score, pmi);
            }
            iter.remove();
        }

    }

    private static void addToPMIMatrix(String from, String to, double prob, HashMap<String, HashMap<String, Double>> matrix) {
        HashMap<String, Double> innerMatrix;
        if (matrix.containsKey(from)) {
            innerMatrix = matrix.get(from);
        } else {
            innerMatrix = new HashMap<>();
        }
        innerMatrix.put(to, prob);
        matrix.put(from, innerMatrix);

    }

    private static void writeCompleteCooccur(String completeCooccurPath, HashMap<String, HashMap<String, Double>> baseCoccur) {
        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new FileWriter(new File(completeCooccurPath)));

            for (String s : baseCoccur.keySet()) {
                HashMap<String, Double> temp = baseCoccur.get(s);
                for (String ss : temp.keySet()) {
                    double freq = temp.get(ss);
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
