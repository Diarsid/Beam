/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.core.jsonconversion;

import java.io.Reader;

import diarsid.beam.core.modules.web.core.jsonconversion.ConverterJsonToObject;
import diarsid.beam.core.modules.web.core.jsonconversion.JsonConversionException;
import diarsid.beam.core.modules.web.service.json.entities.JsonPayload;

import static diarsid.beam.core.modules.web.core.jsonconversion.JsonConversion.toJsonObject;

/**
 *
 * @author Diarsid
 */
class ConverterFromJsonToJsonPayload implements ConverterJsonToObject {

    @Override
    public boolean isApplicableTo(Class clazz) {
        return clazz.equals(JsonPayload.class);
    }

    @Override
    public Object convert(String json) throws JsonConversionException {
        return new JsonPayload((String) toJsonObject(json).get("payload"));
    }

    @Override
    public Object convert(Reader readerWithJson) throws JsonConversionException {
        return new JsonPayload((String) toJsonObject(readerWithJson).get("payload"));
    }
    
}
