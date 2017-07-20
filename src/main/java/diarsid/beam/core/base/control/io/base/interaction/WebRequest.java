/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;

import java.io.IOException;

import diarsid.beam.core.modules.web.core.container.ResourceUrlParsingException;

/**
 *
 * @author Diarsid
 */
public interface WebRequest {

    Json json() throws IOException;

    String pathParam(String param) throws ResourceUrlParsingException;
    
    void send(WebResponse webResponse) throws IOException;
}
