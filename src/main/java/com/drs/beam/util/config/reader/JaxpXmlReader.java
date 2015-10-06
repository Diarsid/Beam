/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.util.config.reader;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Diarsid
 */
class JaxpXmlReader {
    // Fields =============================================================================
    private Document configFile;
    private XPath xPath;
    
    // Constructors =======================================================================
    JaxpXmlReader(String configFilePath) {
        try {
            this.xPath = XPathFactory.newInstance().newXPath();
            this.configFile = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new File(configFilePath));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    // Methods ============================================================================
    
    /**
     * 
     * @param xPathToElement xPath describes path to element in XML configuration file. 
     * @return Text content of XML configuration file element represented by specified xPath. 
     */    
    String readElementTextContent(String xPathToElement){
        try {
            return ((Node) this.xPath
                    .evaluate(
                            xPathToElement, 
                            this.configFile, 
                            XPathConstants.NODE))
                    .getTextContent();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    String readElementAttributeTextContent(String xPathToElement, String attributeName){
        try {
            return ((Node) this.xPath
                    .evaluate(
                            xPathToElement, 
                            this.configFile, 
                            XPathConstants.NODE))
                    .getAttributes()
                    .getNamedItem(attributeName)
                    .getTextContent();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    int readElementAttributeIntegerContent(String xPathToElement, String attributeName){
        try{
            return Integer.parseInt(
                    ((Node) this.xPath
                            .evaluate(
                                    xPathToElement, 
                                    this.configFile, 
                                    XPathConstants.NODE))
                            .getAttributes()
                            .getNamedItem(attributeName)
                            .getTextContent()
            );
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return -1;
        }
    }
    
    String readElementSetTextContent(String xPathToElements){
        try {
            NodeList xmlArgsNodes = (NodeList) this.xPath
                    .evaluate(
                            xPathToElements, 
                            this.configFile, 
                            XPathConstants.NODESET);
            StringBuilder args = new StringBuilder();
            int argsQty = xmlArgsNodes.getLength();            
            for (int i = 0; i < argsQty; i++){                
                args.append(" ").append(xmlArgsNodes.item(i).getTextContent());
            }
            return args.toString();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    

}
