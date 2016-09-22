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
import diarsid.beam.core.util.Logs;
import diarsid.beam.core.util.classloading.CustomClassLoader;

import static java.util.Objects.isNull;

import static diarsid.beam.core.util.classloading.CustomClassLoader.getCustomLoader;

/**
 *
 * @author Diarsid
 */
class ConfigFileReader {
    
    ConfigFileReader() {
    }
    
    Map<Config, String> readConfigs(File configFile) {
        XmlContent content = null;
        content = this.loadReaderDynamicallyAndReadFile(configFile);
        if ( isNull(content) ) {
            content = this.readFile(configFile);
        }
        return this.processXmlContent(content);        
    }
    
    private XmlContent loadReaderDynamicallyAndReadFile(File configFile) {
        try {
            ClassLoader defaultLoader = this.getClass().getClassLoader();
            CustomClassLoader customLoader = getCustomLoader(defaultLoader);
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
        } catch (ModuleInitializationException e) {
            return null;
        } catch (Exception e) {
            Logs.logError(this.getClass(), "", e);
            throw new ModuleInitializationException(
                    "Exception during dynamic " +
                    "XmlSaxReaderWorker class loading: " + 
                    " constructor or instantiation failure.");
        }
    }
    
    private XmlContent readFile(File configFile) {
        XmlReader reader = new XmlSaxReaderWorker(this.getClass().getClassLoader());
        XmlContent content = reader.read(configFile);
        return content;
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
