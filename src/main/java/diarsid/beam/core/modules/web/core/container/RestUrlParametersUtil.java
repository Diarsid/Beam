/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.util.HashMap;
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
    
    private static Map<String, String> v2parse(String urlTemplate, String actualUrl) 
            throws ResourceUrlParsingException {
        Map<String, String> pathParams = new HashMap<>();
        int indexOfCurrentTemplatePathPart = 0;
        int indexOfCurrentActualPathPart = 0;
        int indexOfTemplateSeparator = urlTemplate.indexOf("/");
        int indexOfActualSeparator = actualUrl.indexOf("/");
        String currentTemplatePathPart;
        String currentActualPathPart;
        
        do {
            if ( indexOfTemplateSeparator == -1 || indexOfActualSeparator == -1 ) {
                currentTemplatePathPart = urlTemplate.substring(indexOfCurrentTemplatePathPart, urlTemplate.length());
                currentActualPathPart = actualUrl.substring(indexOfCurrentActualPathPart, actualUrl.length());
            } else {
                currentTemplatePathPart = urlTemplate.substring(indexOfCurrentTemplatePathPart, indexOfTemplateSeparator);
                currentActualPathPart = actualUrl.substring(indexOfCurrentActualPathPart, indexOfActualSeparator);
            }            
            if ( isParam(currentTemplatePathPart) ) {
                pathParams.put(extractParamName(currentTemplatePathPart), currentActualPathPart);
            }
            indexOfCurrentTemplatePathPart = indexOfTemplateSeparator + 1;
            indexOfCurrentActualPathPart = indexOfActualSeparator + 1;
            indexOfTemplateSeparator = urlTemplate.indexOf("/", indexOfTemplateSeparator + 1);
            indexOfActualSeparator = actualUrl.indexOf("/", indexOfActualSeparator + 1);
        } while ( indexOfCurrentTemplatePathPart != 0 && indexOfCurrentActualPathPart != 0 );
        
        return pathParams;
    }

    private static Map<String, String> v1parse(String urlTemplate, String actualUrl) 
            throws ResourceUrlParsingException {
        String[] templateUrlParts = urlTemplate.split("/");
        String[] actualUrlParts = actualUrl.split("/");
        Map<String, String> pathParams = new HashMap<>();
        
        if ( templateUrlParts.length != actualUrlParts.length ) {
            throw new ResourceUrlParsingException("actual url doesnt match expcted template.");
        }   
        int partsQty = templateUrlParts.length;
        
        for (int i = 0; i < partsQty; i++) {
            if ( isParam(templateUrlParts[i]) ) {
                pathParams.put(extractParamName(templateUrlParts[i]), actualUrlParts[i]);
            }
        }    
        return pathParams;
    } 
}
