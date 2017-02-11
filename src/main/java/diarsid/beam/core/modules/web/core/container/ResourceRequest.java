/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Diarsid
 */
public class ResourceRequest {
    
    private final HttpServletRequest servletRequest;
    private final Map<String, String> requestParams;
    
    public ResourceRequest(
            HttpServletRequest servletRequest, 
            Map<String, String> requestParams) {
        this.servletRequest = servletRequest;
        this.requestParams = requestParams;
    }
    
    Optional<String> getParam(String param) {
        return Optional.ofNullable(this.requestParams.get(param));
    }
}
