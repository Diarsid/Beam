/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.core.jsonconversion;

import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Diarsid
 */
public interface Objectivizer {

    Object objectivize(Class clazz, Reader reader);
    
    public static Objectivizer buildObjectivizer() {
        Set<ConverterJsonToObject> jsonToObjects = new HashSet<>();
        
        return new ObjectivizerWorker(jsonToObjects);
    }
}
