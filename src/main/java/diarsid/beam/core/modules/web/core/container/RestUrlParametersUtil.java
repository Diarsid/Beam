/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Diarsid
 */
public class RestUrlParametersUtil {

    private RestUrlParametersUtil() {
    }
    
    public static boolean ifHasParams(String requestUrl) {
        return 
                requestUrl.contains("{") && 
                requestUrl.contains("}");
    }
    
    private static String extractParamName(String param) {
        param = param.trim();
        if ( isParam(param) ) {
            // {param} -> param 
            return param.substring(1, param.length() - 1);
        } else {
            return param;
        }
    }

    private static boolean isParam(String templateUrlPart) {
        return 
                templateUrlPart.startsWith("{") && 
                templateUrlPart.endsWith("}");
    }
    
    public static Map<String, String> parse(String urlTemplate, String actualUrl) {
        return v1parse(urlTemplate, actualUrl);
    } 

    private static Map<String, String> v1parse(String urlTemplate, String actualUrl) 
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
            if ( isParam(currentUrlTemplatePart) ) {
                resolvedPathParams.put(
                        extractParamName(currentUrlTemplatePart), 
                        actualUrlParts.get(i));
            }
        }    
        return resolvedPathParams;
    } 
}
