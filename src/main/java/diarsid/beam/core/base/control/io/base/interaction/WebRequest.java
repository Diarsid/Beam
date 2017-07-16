/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import diarsid.beam.core.modules.web.core.container.ResourceUrlParsingException;

/**
 *
 * @author Diarsid
 */
public interface WebRequest {

    Object bodyOf(Class clazz) throws IOException;

    String pathParam(String param) throws ResourceUrlParsingException;

    void sendBadRequest() throws IOException;

    void sendBadRequestWithJson(String json) throws IOException;

    void sendNotFound() throws IOException;

    void sendNotFoundWithJson(String json) throws IOException;

    void sendOk() throws IOException;

    void sendOkWithJson(String json) throws IOException;

    void sendOkWithJson(ConvertableToJson convertable) throws IOException;

    void sendOkWithJson(Collection<? extends ConvertableToJson> convertables) throws IOException;

    void sendOptionalOkWithJson(Optional<? extends ConvertableToJson> convertable) throws IOException;

    void sendStatus(int status) throws IOException;

    void sendStatusWithJson(String json, int status) throws IOException;
    
}
