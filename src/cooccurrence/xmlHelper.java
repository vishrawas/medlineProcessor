/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cooccurrence;


import java.util.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 *
 * @author SuperMachine
 */
public class xmlHelper {

    String id;
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public int getDate() {
        return date;
    }

    public ArrayList<String> getMeshTerms() {
        return meshTerms;
    }
    String title = "";
    String abstractText = "";
    int date;
    ArrayList<String> meshTerms;
      

    public static void main(String args[]) {
//        xmlHelper helper = new xmlHelper();
//        helper.xmlParser("/Users/professor87/Documents/Research/UB/Medline/medsamp2014-2.xml");
    }

    public xmlHelper(String path, NodeList nList, int temp) {

        Node nNode = nList.item(temp);
        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            
            NodeList nodeList = null;
            Element eElement = (Element) nNode;
            this.id = returnSingleNodeVal("PMID", nodeList, eElement);
            this.title = returnSingleNodeVal("ArticleTitle", nodeList, eElement);
            this.abstractText = returnAbstract(nodeList, eElement);
            this.date = returnDate(nodeList, eElement);
            this.meshTerms = new ArrayList<>();
        
            
            nodeList = eElement.getElementsByTagName("MeshHeadingList");
            if (nodeList.getLength() > 0) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    NodeList childList = nodeList.item(i).getChildNodes();
                    for (int j = 0; j < childList.getLength(); j++) {
                        Node childNode = childList.item(j);
                        if ("MeshHeading".equals(childNode.getNodeName())) {
                            NodeList names = childNode.getChildNodes();
                            for (int k = 0; k < names.getLength(); k++) {
                                Node tempNode = names.item(k);
                                if (tempNode.getTextContent().trim().equals("") == false) {
                                    String val = tempNode.getAttributes().getNamedItem("MajorTopicYN").getNodeValue();
                                    meshTerms.add(tempNode.getTextContent().trim());
                                }                                
                            }
                        }
                    }
                    
                }
            }

        }

    }

    private String returnSingleNodeVal(String name, NodeList nodeList, Element eElement) {
        nodeList = eElement.getElementsByTagName(name);
        if (nodeList.getLength() > 0) {
            return (eElement.getElementsByTagName(name).item(0).getTextContent());
        }
        return null;
    }

    private String returnAbstract(NodeList nodeList, Element eElement) {
        nodeList = eElement.getElementsByTagName("Abstract");
        String abstractText = "";
        if (nodeList.getLength() > 0) {
            Element innerAbstractElement = (Element) nodeList.item(0);
            if (innerAbstractElement.getElementsByTagName("AbstractText").getLength() > 0) {
                abstractText = innerAbstractElement.getElementsByTagName("AbstractText").item(0).getTextContent();
            }
        }
        return abstractText;
    }

    private int returnDate(NodeList nodeList, Element eElement) {
        nodeList = eElement.getElementsByTagName("PubDate");
        int Year = 0;

        if (nodeList.getLength() > 0) {
            Element innerAbstractElement = (Element) nodeList.item(0);
            if (innerAbstractElement.getElementsByTagName("Year").getLength() > 0) {
                Year = Integer.parseInt(innerAbstractElement.getElementsByTagName("Year").item(0).getTextContent());
            }
        }

        if (Year==0) {
            Year = 1900;
        }
        return Year;

    }

}
