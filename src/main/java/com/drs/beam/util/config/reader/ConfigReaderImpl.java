/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.util.config.reader;

import com.drs.beam.core.Beam;

/**
 *
 * @author Diarsid
 */
class ConfigReaderImpl implements ConfigReader{
// ________________________________________________________________________________________
//                                       Fields                                            
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
    
    private static final ConfigReaderImpl reader = new ConfigReaderImpl();
    
    private final JaxpXmlReader xmlReader;
    
// ________________________________________________________________________________________
//                                    Constructors                                         
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
    
    private ConfigReaderImpl() {
        this.xmlReader = new JaxpXmlReader(Beam.getConfigFilePath());
    }

// ________________________________________________________________________________________
//                                       Methods                                           
// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
    
    static ConfigReader getReader(){
        return ConfigReaderImpl.reader;
    } 
    
    @Override
    public String getLibrariesLocation(){
        return this.xmlReader.readElementTextContent("/configuration/resources/libraries");
    }
    
    @Override
    public String getImagesLocation(){
        return this.xmlReader.readElementTextContent("/configuration/resources/images");        
    }
    
    @Override
    public String getProgramsLocation(){
        return this.xmlReader.readElementTextContent("/configuration/resources/programs");
    }
    
    @Override
    public int getBeamPort(){
        return this.xmlReader.readElementAttributeIntegerContent(
                "/configuration/rmi-info/beam", 
                "port");
    }
    
    @Override
    public String getBeamHost(){
        return this.xmlReader.readElementAttributeTextContent(
                "/configuration/rmi-info/beam", 
                "host");
    }
    
    @Override
    public int getSystemConsolePort(){
        return this.xmlReader.readElementAttributeIntegerContent(
                "/configuration/rmi-info/beam-system-console", 
                "port");
    }
    
    @Override
    public String getSystemConsoleHost(){
        return this.xmlReader.readElementAttributeTextContent(
                "/configuration/rmi-info/beam-system-console", 
                "host");        
    }
    
    @Override
    public String getTaskManagerName(){
        return this.xmlReader.readElementTextContent(
                "/configuration/rmi-info/beam/task-manager-rmi-name");        
    }
    
    @Override
    public String getExecutorName(){
        return this.xmlReader.readElementTextContent(
                "/configuration/rmi-info/beam/executor-rmi-name");        
    }
    
    @Override
    public String getAccessName(){
        return this.xmlReader.readElementTextContent(
                "/configuration/rmi-info/beam/access-rmi-name");
    }
    
    @Override
    public String getLocationsHandlerName(){
        return this.xmlReader.readElementTextContent(
                "/configuration/rmi-info/beam/locations-rmi-name");
    }
    
    @Override
    public String getWebPagesHandlerName(){
        return this.xmlReader.readElementTextContent(
                "/configuration/rmi-info/beam/web-pages-rmi-name");
    }
    
    @Override
    public String getSystemConsoleName(){
        return this.xmlReader.readElementTextContent(
                "/configuration/rmi-info/beam-system-console/console-rmi-name");
    }
    
    @Override
    public String getCoreDBDriver(){
        return this.xmlReader.readElementTextContent(
                "/configuration/databases/core/jdbc-driver");
    }
    
    @Override
    public String getCoreDBURL(){
        String url = this.xmlReader.readElementTextContent(
                "/configuration/databases/core/jdbc-url");
        String location = this.xmlReader.readElementTextContent(
                "/configuration/databases/core/db-location");
        return url + location;
    }
    
    @Override
    public String getCoreDBName(){
        return this.xmlReader.readElementTextContent("/configuration/databases/core/db-name");
    }
    
    @Override
    public String getDbDriverJar(){
        return this.xmlReader.readElementTextContent(
                "/configuration/databases/core/db-driver-jar");
    }
    
    @Override
    public String getJvmOptionsForBeam(){
        return this.xmlReader.readElementSetTextContent("/configuration/jvm-options/beam/option");        
    }
    
    @Override
    public String getJvmOptionsForSystemConsole(){
        return this.xmlReader.readElementSetTextContent(
                "/configuration/jvm-options/beam-system-console/option");        
    }
}
