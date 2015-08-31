/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.util.config;

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
    private static ConfigReaderJAXP reader = new ConfigReaderJAXP();
    
    private Document config;
    private XPath xPath = XPathFactory.newInstance().newXPath();
    
    // Constructor ========================================================================
    public ConfigReaderJAXP() {
        try {
            config = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new File("./config/config.xml"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }        
    }
    
    // Methods ============================================================================
    
    
    static void cancel(){
        reader.cancelReader();
        reader = null;
    }    
    
    private void cancelReader(){
        this.config = null;
        this.xPath = null;
    }
    
    static ConfigReader getReader(){
        return reader;
    }    
    
    @Override
    public String getLibrariesLocation(){
        try {            
            return ((Node) xPath
                    .evaluate(
                            "/configuration/resources/libraries", 
                            config, 
                            XPathConstants.NODE))
                    .getTextContent();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    @Override
    public String getImagesLocation(){
        try {
            return ((Node) xPath
                    .evaluate(
                            "/configuration/resources/images", 
                            config, 
                            XPathConstants.NODE))
                    .getTextContent();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    @Override
    public String getGuiPlatform(){
        try {
            return ((Node) xPath
                    .evaluate(
                            "/configuration/ui/inner", 
                            config, 
                            XPathConstants.NODE))
                    .getTextContent();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    @Override
    public String getProgramsLocation(){
        try {
            return ((Node) xPath
                    .evaluate(
                            "/configuration/executor/programs", 
                            config, 
                            XPathConstants.NODE))
                    .getTextContent();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    @Override
    public int getOrganizerPort(){
        try{
            return Integer.parseInt(
                    ((Node) xPath
                            .evaluate(
                                    "/configuration/rmi-info/organizer", 
                                    config, 
                                    XPathConstants.NODE))
                            .getAttributes()
                            .getNamedItem("port")
                            .getTextContent()
            );
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return -1;
        }
    }
    
    @Override
    public String getOrganizerHost(){
        try {
            return ((Node) xPath
                    .evaluate(
                            "/configuration/rmi-info/organizer", 
                            config, 
                            XPathConstants.NODE))
                    .getAttributes()
                    .getNamedItem("host")
                    .getTextContent();            
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    @Override
    public int getConsolePort(){
        try{
            return Integer.parseInt(
                    ((Node) xPath
                            .evaluate(
                                    "/configuration/rmi-info/console", 
                                    config, 
                                    XPathConstants.NODE))
                            .getAttributes()
                            .getNamedItem("port")
                            .getTextContent()
            );
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return -1;
        }
    }
    
    @Override
    public String getConsoleHost(){
        try {
            return ((Node) xPath
                    .evaluate(
                            "/configuration/rmi-info/console", 
                            config, 
                            XPathConstants.NODE))
                    .getAttributes()
                    .getNamedItem("host")
                    .getTextContent();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    @Override
    public String getTaskManagerName(){
        try {
            return ((Node) xPath
                    .evaluate("/configuration/rmi-info/organizer/task-manager-rmi-name", 
                            config, 
                            XPathConstants.NODE))
                    .getTextContent();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    @Override
    public String getOSExecutorName(){
        try {
            return ((Node) xPath
                    .evaluate(
                            "/configuration/rmi-info/organizer/os-executor-rmi-name", 
                            config, 
                            XPathConstants.NODE))
                    .getTextContent();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    @Override
    public String getOrgIOName(){
        try {
            return ((Node) xPath
                    .evaluate(
                            "/configuration/rmi-info/organizer/org-io-rmi-name", 
                            config, 
                            XPathConstants.NODE))
                    .getTextContent();           
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    @Override
    public String getConsoleName(){
        try {
            return ((Node) xPath
                    .evaluate(
                            "/configuration/rmi-info/console/console-rmi-name", 
                            config, 
                            XPathConstants.NODE))
                    .getTextContent();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    @Override
    public String getCoreDBDriver(){
        try {
            return ((Node) xPath
                    .evaluate("/configuration/databases/core/jdbc-driver", 
                            config, 
                            XPathConstants.NODE))
                    .getTextContent();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    @Override
    public String getCoreDBURL(){
        try {
            String url = ((Node) xPath
                    .evaluate(
                            "/configuration/databases/core/jdbc-url", 
                            config, 
                            XPathConstants.NODE))
                    .getTextContent();
            String location = ((Node) xPath
                    .evaluate(
                            "/configuration/databases/core/db-location", 
                            config, 
                            XPathConstants.NODE))
                    .getTextContent();            
            return url + location;
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    @Override
    public String getCoreDBName(){
        try {
            return ((Node) xPath
                    .evaluate(
                            "/configuration/databases/core/db-name", 
                            config, 
                            XPathConstants.NODE))
                    .getTextContent();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    @Override
    public String getDbDriverJar(){
        try {
            return ((Node) xPath
                    .evaluate(
                            "/configuration/databases/core/db-driver-jar", 
                            config, 
                            XPathConstants.NODE))
                    .getTextContent();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    @Override
    public String getLoadingType(){
        try {
            return ((Node) xPath
                    .evaluate(
                            "/configuration/loading", 
                            config, 
                            XPathConstants.NODE))
                    .getTextContent();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}
