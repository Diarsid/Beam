/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.util;

import java.io.File;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;


/**
 *
 * @author Diarsid
 */
class ConfigReaderJDOM implements ConfigReader{
    // Fields =============================================================================    
    private Document doc;
    private XPathFactory xFactory;
    private XPathExpression exp;
    private Element element;
    private String result;
    
    // Constructor ========================================================================
    public ConfigReaderJDOM() {
        try {
            this.doc = new SAXBuilder().build(new File("./config.xml"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        this.xFactory = XPathFactory.instance();
    }    
    
    // Methods ============================================================================
    
    public static void main(String[] args) throws Exception{
        ConfigReaderJDOM config = new ConfigReaderJDOM();
        System.out.println(config.getCoreDBDriver());
        System.out.println(config.getCoreDBURL());
        
    }
    
    public int getOrganizerPort(){
        try{
            exp = xFactory.compile("//rmi-info/organizer");
            element = (Element) exp.evaluate(doc).get(0);
            result = element.getAttributeValue("port");
            return Integer.parseInt(result);
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return -1;
        }
    }
    public String getOrganizerHost(){
        try {
            exp = xFactory.compile("//rmi-info/organizer");
            element = (Element) exp.evaluate(doc).get(0);            
            return element.getAttributeValue("host");
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public int getConsolePort(){
        try{
            exp = xFactory.compile("//rmi-info/console");
            element = (Element) exp.evaluate(doc).get(0);
            result = element.getAttributeValue("port");            
            return Integer.parseInt(result);
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return -1;
        }
    }
    public String getConsoleHost(){
        try {
            exp = xFactory.compile("//rmi-info/console");
            element = (Element) exp.evaluate(doc).get(0);            
            return element.getAttributeValue("host");
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public String getTaskManagerName(){
        try {
            exp = xFactory.compile("//rmi-info/organizer/task-manager-rmi-name");
            element = (Element) exp.evaluate(doc).get(0);
            return element.getText();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public String getOSExecutorName(){
        try {
            exp = xFactory.compile("//rmi-info/organizer/os-executor-rmi-name");
            element = (Element) exp.evaluate(doc).get(0);
            return element.getText();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public String getOrgIOName(){
        try {
            exp = xFactory.compile("//rmi-info/organizer/org-io-rmi-name");
            element = (Element) exp.evaluate(doc).get(0);
            return element.getText();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public String getConsoleName(){
        try {
            exp = xFactory.compile("//rmi-info/console/console-rmi-name");
            element = (Element) exp.evaluate(doc).get(0);
            return element.getText();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public String getCoreDBDriver(){
        try {
            exp = xFactory.compile("/configuration/databases/core/jdbc-driver");
            element = (Element) exp.evaluate(doc).get(0);
            return element.getText();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public String getCoreDBURL(){
        try {
            exp = xFactory.compile("/configuration/databases/core/jdbc-url");
            element = (Element) exp.evaluate(doc).get(0);
            result = element.getText();
            
            exp = xFactory.compile("/configuration/databases/core/db-location");
            element = (Element) exp.evaluate(doc).get(0);
            result = result + element.getText();
            return result;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    public String getCoreDBName(){
        try {
            exp = xFactory.compile("/configuration/databases/core/db-name");
            element = (Element) exp.evaluate(doc).get(0);
            return element.getText();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}
