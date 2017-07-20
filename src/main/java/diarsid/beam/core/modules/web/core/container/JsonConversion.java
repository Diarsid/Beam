/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.core.container;


import java.io.IOException;
import java.io.Reader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 *
 * @author Diarsid
 */
class JsonConversion {
    
    private static final JSONParser JSON_PARSER;
    
    static {
        JSON_PARSER = new JSONParser();
    }

    private JsonConversion() {
    }
    
    static JSONObject toJsonObject(String json) throws JsonConversionException {
        try {
            return (JSONObject) JSON_PARSER.parse(json);
        } catch (ParseException e) {
            throw new JsonConversionException(e);
        }
    }
    
    static JSONObject toJsonObject(Reader reader) throws JsonConversionException {
        try {
            return (JSONObject) JSON_PARSER.parse(reader);
        } catch (IOException | ParseException e) {
            throw new JsonConversionException(e);
        }
    }
    
}
