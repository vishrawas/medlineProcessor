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
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vishrawa
 */
public class validateNegativity {
    public static void main(String args[]){
        String path = "C:\\Users\\vishrawa\\Documents\\Research\\medline_prj\\output\\pmi\\";
         ArrayList<String>files = new ArrayList<>();
        listFilesForFolder(new File(path), files);
        int fileCounter=0;
        for(String filePath:files){
            BufferedReader br = null;
            try {
                System.out.println(filePath);
                String from;
                String to;
                int year;
                double count;
                String line;
                br = new BufferedReader(new FileReader(filePath));
                while((line=br.readLine())!=null){
                    StringTokenizer tok = new StringTokenizer(line, "\t");
                    if (tok.countTokens() == 3) {
                        from = tok.nextToken();
                        to = tok.nextToken();                        
                        count = Double.parseDouble(tok.nextToken()); 
                        if(count<0)
                        {
                            System.out.println("Negative Found"+filePath);
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(validateNegativity.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(validateNegativity.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(validateNegativity.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
