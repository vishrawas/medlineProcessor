/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cooccurrence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author vishrawa
 */
public class cooccurPreprocess {

    static int minYear = 10000000;
    static int maxYear = -10000000;
    static HashMap<String, HashMap<String, HashMap<Integer, Long>>> baseCoccur = new HashMap<>();

    private static void processIndividualXml(String filePath) {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            System.out.println(filePath);
            File f = new File(filePath.trim());
            org.w3c.dom.Document doc = dBuilder.parse(f);
            doc.getDocumentElement().normalize();
            NodeList nList = returnNodeList("MedlineCitation", doc);
            for (int citationCounter = 0; citationCounter < nList.getLength(); citationCounter++) {
                xmlHelper xHelper = new xmlHelper(filePath, nList, citationCounter);
                int date = xHelper.getDate();
                ArrayList<String> meshTerms = xHelper.getMeshTerms();
                if (!meshTerms.isEmpty()) {
                    createCooccurMatrix(meshTerms, date);
                }
            }

        } catch (SAXException ex) {
            Logger.getLogger(cooccurPreprocess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(cooccurPreprocess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(cooccurPreprocess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static NodeList returnNodeList(String tagName, org.w3c.dom.Document doc) {
        return doc.getElementsByTagName(tagName);
    }

    private static void createCooccurMatrix(ArrayList<String> meshTerms, int date) {
        for (String meshTerm : meshTerms) {
            HashMap<String, HashMap<Integer, Long>> cooccurrMatrix;
            if (baseCoccur.containsKey(meshTerm)) {
                cooccurrMatrix = baseCoccur.get(meshTerm);
            } else {
                cooccurrMatrix = new HashMap<>();
            }

            for (String coccurMeshTerm : meshTerms) {
                if (!meshTerm.equals(coccurMeshTerm)) {
                    if (cooccurrMatrix.containsKey(coccurMeshTerm)) {
                        HashMap<Integer, Long> yearCountMap = cooccurrMatrix.get(coccurMeshTerm);
                        if (yearCountMap.containsKey(date)) {
                            Long freq = yearCountMap.get(date);
                            freq++;
                            yearCountMap.put(date, freq);
                            if (minYear > date) {
                                minYear = date;
                            }
                            if (maxYear < date) {
                                maxYear = date;
                            }
                        } else {
                            if (minYear > date) {
                                minYear = date;
                            }
                            if (maxYear < date) {
                                maxYear = date;
                            }
                            yearCountMap.put(date, 1L);
                        }
                        cooccurrMatrix.put(coccurMeshTerm, yearCountMap);
                    } else {
                        HashMap<Integer, Long> yearCountMap = new HashMap<>();
                        yearCountMap.put(date, 1L);
                        cooccurrMatrix.put(coccurMeshTerm, yearCountMap);
                        if (minYear > date) {
                            minYear = date;
                        }
                        if (maxYear < date) {
                            maxYear = date;
                        }
                    }
                    baseCoccur.put(meshTerm, cooccurrMatrix);
                }
            }
        }
    }

    public static void main(String args[]) {
        String dirPath = "C:\\Users\\vishrawa\\Documents\\Research\\medline_prj\\base_files\\";//args[0];
        String completeCooccurPath = "C:\\Users\\vishrawa\\Documents\\Research\\medline_prj\\";// args[1];
        int fileCounter = 0;
          ArrayList<String>files = new ArrayList<>();
                listFilesForFolder(new File(dirPath),files);
        for (String filePath : files) {
            System.out.println(filePath);
            fileCounter++;
            processIndividualXml(filePath);
            if (fileCounter % 20 == 0) {
                writeCompleteCooccur(completeCooccurPath + "completeCoccur" + "_" + fileCounter, baseCoccur);
            }
        }

        writeCompleteCooccur(completeCooccurPath + "completeCoccur" + "_" + fileCounter, baseCoccur);

    }
public static ArrayList<String> listFilesForFolder(final File folder,ArrayList<String>files) {
    
 
    for (final File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
            listFilesForFolder(fileEntry,files);
        } else {
//            System.out.println(files.size());
            files.add(fileEntry.getAbsolutePath());
        }
    }
    return files;
}
    private static void writeCompleteCooccur(String completeCooccurPath, HashMap<String, HashMap<String, HashMap<Integer, Long>>> baseCoccur) {
        BufferedWriter bw = null;
        try {
            ArrayList<String> writeLines = new ArrayList<>();
            for (String s : baseCoccur.keySet()) {
                HashMap<String, HashMap<Integer, Long>> temp = baseCoccur.get(s);
                for (String ss : temp.keySet()) {
                    HashMap<Integer, Long> temp2 = temp.get(ss);
                    for (Integer date : temp2.keySet()) {
                        Long freq = temp2.get(date);
                        String toWrite = s + "\t" + ss + "\t" + date + "\t" + freq + "\n";
                        writeLines.add(toWrite);
                    }
                }
            }
            bw = new BufferedWriter(new FileWriter(new File(completeCooccurPath)));
            for (String line : writeLines) {
                bw.append(line);
            }
            baseCoccur.clear();
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
