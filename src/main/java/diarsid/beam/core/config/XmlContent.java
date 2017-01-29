/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.config;

import java.util.Map;

/**
 *
 * @author Diarsid
 */
final class XmlContent {
    
    private final Map<String, String> elementsText;
    private final Map<String, Map<String, String>> elementsAttrs;
    
    XmlContent(
            Map<String, String> texts, 
            Map<String, Map<String, String>> attrs) {
        
        this.elementsText = texts;
        this.elementsAttrs = attrs;
    }

    String getElementsAttribute(String pathToElement, String attributeName) {
        return elementsAttrs.get(pathToElement).get(attributeName);
    }

    String getGroupElementsText(String pathToElements) {
        return elementsText.get(pathToElements);
    }
   
    String getElementsText(String pathToElement) {
        return elementsText.get(pathToElement);
    }
}
