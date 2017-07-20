/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.interaction;

import java.util.Collection;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import static diarsid.beam.core.base.util.JsonUtil.asJson;
import static diarsid.beam.core.base.util.JsonUtil.convertablesAsJsonArray;
import static diarsid.beam.core.base.util.JsonUtil.errorJson;
import static diarsid.beam.core.base.util.StringUtils.nonNullNonEmpty;

/**
 *
 * @author Diarsid
 */
public class WebResponse {
    
    private static final WebResponse OK_EMPTY_WEB_RESPONSE;
    private static final WebResponse BAD_REQUEST_EMPTY_WEB_RESPONSE;
    private static final WebResponse NOT_FOUND_EMPTY_WEB_RESPONSE;
    
    static {
        OK_EMPTY_WEB_RESPONSE = new WebResponse(SC_OK, null);
        BAD_REQUEST_EMPTY_WEB_RESPONSE = new WebResponse(SC_BAD_REQUEST, null);
        NOT_FOUND_EMPTY_WEB_RESPONSE = new WebResponse(SC_NOT_FOUND, null);
    }
    
    private final String body;
    private final int status;
    
    private WebResponse(int status, String body) {
        this.status = status;
        this.body = body;
    }
    
    public boolean hasBody() {
        return nonNullNonEmpty(this.body);
    }

    public String body() {
        return this.body;
    }

    public int status() {
        return this.status;
    }

    public static WebResponse badRequest() {
        return BAD_REQUEST_EMPTY_WEB_RESPONSE;
    }

    public static WebResponse badRequestWithJson(String json) {
        return new WebResponse(SC_BAD_REQUEST, errorJson(json));
    }

    public static WebResponse notFound() {
        return NOT_FOUND_EMPTY_WEB_RESPONSE;
    }

    public static WebResponse notFoundWithJson(String json) {
        return new WebResponse(SC_NOT_FOUND, errorJson(json));
    }

    public static WebResponse ok() {
        return OK_EMPTY_WEB_RESPONSE;
    }

    public static WebResponse okWithJsonMessage(String json) {
        return new WebResponse(SC_OK, asJson("message", json));
    }

    public static WebResponse okWithJson(ConvertableToJson convertable) {
        return new WebResponse(SC_OK, convertable.toJson());
    }

    public static WebResponse okWithJson(
            Collection<? extends ConvertableToJson> convertables) {
        return new WebResponse(SC_OK, convertablesAsJsonArray(convertables));
    }

    public static WebResponse optionalOkWithJson(
            Optional<? extends ConvertableToJson> convertable) {
        if ( convertable.isPresent() ) {
            return okWithJson(convertable.get());
        } else {
            return notFound();
        }
    }

    public static WebResponse status(int status) {
        return new WebResponse(status, null);
    }

    public static WebResponse statusWithJsonMessage(int status, String json) {
        return new WebResponse(status, asJson("message", json));
    }
}
