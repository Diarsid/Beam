/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.jsonconversion;

import diarsid.beam.core.modules.web.core.container.JsonConversionException;

import java.io.Reader;
import java.util.Set;


class ObjectivizerWorker implements Objectivizer {

    private final Set<ConverterJsonToObject> toObjectConverters;
    
    ObjectivizerWorker(Set<ConverterJsonToObject> toObjectConverters) {
        if ( toObjectConverters.isEmpty() ) {
            throw new JsonConversionException(
                    "There are no json-to-object converters for " + 
                            this.getClass().getCanonicalName());
        }
        this.toObjectConverters = toObjectConverters;
    }

    @Override
    public Object objectivize(Class clazz, Reader reader) {
        return this
                .findToObjectConverterFor(clazz)
                .convert(reader); 
    }
    
    private ConverterJsonToObject findToObjectConverterFor(Class clazz) {
        return this.toObjectConverters
                .stream()
                .filter(converter -> converter.isApplicableTo(clazz))
                .findFirst()
                .orElseThrow(() -> 
                        new JsonConversionException(
                                "Cannot find applicable object-to-json converter for class: " + 
                                        clazz.getCanonicalName())
                );
    }
}
