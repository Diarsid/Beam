/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.shared.modules.config;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import diarsid.beam.core.exceptions.ModuleInitializationException;

/**
 *
 * @author Diarsid
 */
class ConfigFileReader {
    
    ConfigFileReader() {
    }
    
    Map<Config, String> readConfigs(File configFile) {
        XmlContent content = this.loadReaderAndReadFile(configFile);
        return this.processXmlContent(content);        
    }
    
    private XmlContent loadReaderAndReadFile(File configFile) {
        try {
            ClassLoader defaultLoader = this.getClass().getClassLoader();
            CustomClassLoader customLoader = new CustomClassLoader(defaultLoader);
            Class xmlReaderClass = customLoader.loadClass(
                    "diarsid.beam.shared.modules.config.XmlSaxReaderWorker");
            Constructor xmlReaderConstr = xmlReaderClass.getDeclaredConstructor(ClassLoader.class);
            xmlReaderConstr.setAccessible(true);
            Object obj = xmlReaderConstr.newInstance(customLoader);
            XmlReader reader = (XmlReader) obj;
            XmlContent content = reader.read(configFile);
            return content;
        } catch (ClassNotFoundException e) {
            throw new ModuleInitializationException(
                    "Exception during dynamic " +
                    "XmlSaxReaderWorker class loading: invalid class name.");
        } catch (Exception e) {
            throw new ModuleInitializationException(
                    "Exception during dynamic " +
                    "XmlSaxReaderWorker class loading: " + 
                    " constructor or instantiation failure.");
        }
    }
    
    private Map<Config, String> processXmlContent(XmlContent content) {
        Map<Config, String> configs = new HashMap<>();
        
        for (Config param : Config.values()) {
            if ( param.xmlAttribute().isEmpty() ) {
                configs.put(param, content.getElementsText(param.xmlPath()));
            } else {
                configs.put(param, content.getElementsAttribute(
                        param.xmlPath(), 
                        param.xmlAttribute()));
            }
        }
                
        if ( configs.size() != Config.values().length ) {
            throw new ModuleInitializationException(
                    "exception in ConfigFileReader::processXmlContent() - " +
                    " not all ConfigParams have been read from XmlContent.");
        }       
        
        return configs;
    }
}
