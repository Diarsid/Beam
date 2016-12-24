/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.config;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Diarsid
 */
class XmlSaxRunner extends DefaultHandler {
    
    private final StringBuilder elementRoot;
    private final Map<String, String> elementsText;
    private final Map<String, Map<String, String>> elementsAttrs;
    
    XmlSaxRunner() {
        this.elementRoot = new StringBuilder();
        this.elementsText = new HashMap<>();
        this.elementsAttrs = new HashMap<>();
    }    
    
    @Override    
    public void startElement(
            String uri, String local, String element, Attributes attributes) 
            throws SAXException {        
        
        elementRoot.append("/").append(element);
        if( attributes.getLength() > 0 ) {
            Map<String, String> attrs = new HashMap<>();
            for (int i = 0; i < attributes.getLength(); i++) {
                attrs.put(attributes.getQName(i), attributes.getValue(i));
            }
            elementsAttrs.put(elementRoot.toString(), attrs);
        }        
    }
    
    @Override
    public void endElement(String uri, String local, String element) 
                throws SAXException {
        
        elementRoot.delete(
                elementRoot.length()-element.length()-1, elementRoot.length());
    }
    
    @Override
    public void characters(char[] ch, int start, int length)
                throws SAXException {
        String text = String.valueOf(ch, start, length).trim();
        if (text.length() >= 0) {
            if (elementsText.containsKey(elementRoot.toString())) {
                String newContent = elementsText.get(
                        elementRoot.toString()) + " " + text;
                elementsText.put(elementRoot.toString(), newContent);
            } else {
                elementsText.put(elementRoot.toString(), text);
            }
        }        
    }
    
    Map<String, String> getElementsText() {
        return this.elementsText;
    }
    
    Map<String, Map<String, String>> getElementsAttrs() {
        return this.elementsAttrs;
    }
}
