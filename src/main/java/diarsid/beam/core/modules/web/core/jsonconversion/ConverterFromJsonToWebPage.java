/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.core.jsonconversion;

import java.io.Reader;

import org.json.simple.JSONObject;

import diarsid.beam.core.domain.entities.WebPage;

import static diarsid.beam.core.domain.entities.WebPages.newWebPage;
import static diarsid.beam.core.modules.web.core.jsonconversion.JsonConversion.toJsonObject;

/**
 *
 * @author Diarsid
 */
public class ConverterFromJsonToWebPage implements ConverterJsonToObject {

    @Override
    public boolean isApplicableTo(Class clazz) {
        return WebPage.class.equals(clazz);
    }

    @Override
    public Object convert(String json) throws JsonConversionException {
        return jsonObjectToNewWebPage(toJsonObject(json));
    }

    @Override
    public Object convert(Reader readerWithJson) throws JsonConversionException {
        return jsonObjectToNewWebPage(toJsonObject(readerWithJson));
    }

    private Object jsonObjectToNewWebPage(JSONObject page) throws NumberFormatException {
        return newWebPage(
                (String) page.get("name"),
                "",
                (String) page.get("url"), 
                Integer.valueOf((String) page.get("dirId")));
    }
    
}
