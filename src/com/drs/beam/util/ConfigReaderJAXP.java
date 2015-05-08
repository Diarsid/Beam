/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.util;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author Diarsid
 */
class ConfigReaderJAXP implements ConfigReader{
    // Fields =============================================================================    
    private Document config;
    private XPath xPath = XPathFactory.newInstance().newXPath();
    private String exp;
    private String result;    
    
    // Constructor ========================================================================

    public ConfigReaderJAXP() {
        try {
            config = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new File("./config.xml"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
    }
    
    public static void main(String[] args) {
        ConfigReaderJAXP conf = new ConfigReaderJAXP();
        System.out.println(conf.getCoreDBDriver());
        System.out.println(conf.getCoreDBURL());
    }
    
    // Methods ============================================================================
    
    public int getOrganizerPort(){
        try{
            exp = "//rmi-info/organizer";
            Node org = (Node) xPath.evaluate(exp, config, XPathConstants.NODE);
            int port = Integer.parseInt(org.getAttributes().getNamedItem("port").getTextContent()); 
            return port;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return -1;
        }
    }
    public String getOrganizerHost(){
        try {
            exp = "//rmi-info/organizer";
            Node org = (Node) xPath.evaluate(exp, config, XPathConstants.NODE);
            result = org.getAttributes().getNamedItem("host").getTextContent();
            return result;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public int getConsolePort(){
        try{
            exp = "//rmi-info/console";
            Node console = (Node) xPath.evaluate(exp, config, XPathConstants.NODE);
            int port = Integer.parseInt(console.getAttributes().getNamedItem("port").getTextContent());
            return port;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return -1;
        }
    }
    public String getConsoleHost(){
        try {
            exp = "//rmi-info/console";
            Node console = (Node) xPath.evaluate(exp, config, XPathConstants.NODE);            
            result = console.getAttributes().getNamedItem("host").getTextContent();
            return result;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public String getTaskManagerName(){
        try {
            exp = "//rmi-info/organizer/task-manager-rmi-name";
            result = ((Node) xPath.evaluate(exp, config, XPathConstants.NODE)).getTextContent();
            return result;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public String getOSExecutorName(){
        try {
            exp = "//rmi-info/organizer/os-executor-rmi-name";
            result = ((Node) xPath.evaluate(exp, config, XPathConstants.NODE)).getTextContent();
            return result;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public String getOrgIOName(){
        try {
            exp = "//rmi-info/organizer/org-io-rmi-name";
            result = ((Node) xPath.evaluate(exp, config, XPathConstants.NODE)).getTextContent();
            return result;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public String getConsoleName(){
        try {
            exp = "//rmi-info/console/console-rmi-name";
            Node node = (Node) xPath.evaluate(exp, config, XPathConstants.NODE);
            result = node.getTextContent();
            return result;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public String getCoreDBDriver(){
        try {
            exp = "/configuration/databases/core/jdbc-driver";
            Node node = (Node) xPath.evaluate(exp, config, XPathConstants.NODE);
            result = node.getTextContent();
            return result;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public String getCoreDBURL(){
        try {
            exp = "/configuration/databases/core/jdbc-url";
            Node node = (Node) xPath.evaluate(exp, config, XPathConstants.NODE);
            result = node.getTextContent();
            
            exp = "/configuration/databases/core/db-location";
            node = (Node) xPath.evaluate(exp, config, XPathConstants.NODE);
            result = result + node.getTextContent();
            return result;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public String getCoreDBName(){
        try {
            exp = "/configuration/databases/core/db-name";
            Node node = (Node) xPath.evaluate(exp, config, XPathConstants.NODE);
            result = node.getTextContent();
            return result;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    
}
