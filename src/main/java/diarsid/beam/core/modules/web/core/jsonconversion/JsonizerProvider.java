/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.jsonconversion;

import java.util.Set;

/**
 *
 * @author Diarsid
 */
public class JsonizerProvider {
    
    private JsonizerProvider() {
    }
    
    public static Jsonizer buildJsonizer(
            Set<ConverterObjectToJson> toJsonConverters, 
            Set<ConverterJsonToObject> toObjectConverters) {
        ConvertersHolder convertersHolder = 
                new ConvertersHolder(toJsonConverters, toObjectConverters);
        return new JsonizerWorker(convertersHolder);
    }
}
