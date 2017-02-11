/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import static java.util.Objects.isNull;

import static diarsid.beam.core.modules.web.core.container.RestUrlParametersUtil.parse;

/**
 *
 * @author Diarsid
 */
public class ResourceRequest {
    
    private final HttpServletRequest servletRequest;
    private final String urlSchema;
    private Map<String, String> params;
    
    public ResourceRequest(HttpServletRequest servletRequest, String urlSchema) {
        this.servletRequest = servletRequest;
        this.urlSchema = urlSchema;
        this.params = null;
    }
    
    private void parseParams() {
        this.params = parse(this.urlSchema, this.servletRequest.getPathInfo());
    }
    
    public Optional<String> getParam(String param) {
        if ( isNull(this.params) ) {
            this.parseParams();
        }
        return Optional.ofNullable(this.params.get(param));
    }
}
