/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.rest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import diarsid.beam.core.modules.web.core.exceptions.ResourceUrlParsingException;

/**
 *
 * @author Diarsid
 */
public class RestUrlParamsParser {

    public RestUrlParamsParser() {
    }
    
    public boolean ifHasParams(String requestUrl) {
        return 
                requestUrl.contains("{") && 
                requestUrl.contains("}");
    }
    
    String extractParamName(String param) {
        return param
                .replace("{", "")
                .replace("}", "")
                .trim();
    }

    boolean isParam(String templateUrlPart) {
        return 
                templateUrlPart.startsWith("{") && 
                templateUrlPart.endsWith("}");
    }
    
    public Map<String, String> parse(String urlTemplate, String actualUrl) {
        return this.v1parse(urlTemplate, actualUrl);
    } 

    private Map<String, String> v1parse(String urlTemplate, String actualUrl) 
            throws ResourceUrlParsingException {
        List<String> templateUrlParts = Arrays.asList(urlTemplate.split("/"));
        List<String> actualUrlParts = Arrays.asList(actualUrl.split("/"));
        Map<String, String> resolvedPathParams = new HashMap<>();
        String currentUrlTemplatePart;
        
        if ( templateUrlParts.size() != actualUrlParts.size() ) {
            throw new ResourceUrlParsingException("actual url doesnt match expcted template.");
        }        
        
        for (int i = 0; i < templateUrlParts.size(); i++) {
            currentUrlTemplatePart = templateUrlParts.get(i);
            if ( this.isParam(currentUrlTemplatePart) ) {
                resolvedPathParams.put(
                        this.extractParamName(currentUrlTemplatePart), 
                        actualUrlParts.get(i));
            }
        }    
        return resolvedPathParams;
    } 
}
