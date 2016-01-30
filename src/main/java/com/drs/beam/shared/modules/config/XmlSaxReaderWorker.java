/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.shared.modules.config;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

/**
 *
 * @author Diarsid
 */
class XmlSaxReaderWorker implements XmlReader {
    
    private final ClassLoader loader;
    
    XmlSaxReaderWorker(ClassLoader loader) {
        this.loader = loader;
    }
    
    @Override
    public XmlContent read(File configFile) {
        try {
        SAXParserFactory factory = SAXParserFactory.newInstance(
                "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl", 
                loader);
        SAXParser saxParser = factory.newSAXParser();
        XmlSaxRunner runner = new XmlSaxRunner();
        saxParser.parse(configFile, runner);
        XmlContent content = new XmlContent(
                runner.getElementsText(), runner.getElementsAttrs()); 
        return content;
        } catch (SAXException e) {
            
        } catch (ParserConfigurationException e ) {
            
        } catch (IOException e) {
            
        }
        return null;
    }
}
