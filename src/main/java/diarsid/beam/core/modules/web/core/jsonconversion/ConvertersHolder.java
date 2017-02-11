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
class ConvertersHolder {
    
    private final Set<ConverterObjectToJson> toJsonConverters;
    private final Set<ConverterJsonToObject> toObjectConverters;
    
    ConvertersHolder(
            Set<ConverterObjectToJson> toJsonConverters, 
            Set<ConverterJsonToObject> toObjectConverters) {
        if ( toJsonConverters.isEmpty() ) {
            throw new JsonConversionException(
                    "There are no object-to-json converters for " + 
                            this.getClass().getCanonicalName());
        }
        if ( toObjectConverters.isEmpty() ) {
            throw new JsonConversionException(
                    "There are no json-to-object converters for " + 
                            this.getClass().getCanonicalName());
        }
        this.toJsonConverters = toJsonConverters;
        this.toObjectConverters = toObjectConverters;
    }

    ConverterObjectToJson findToJsonConverterFor(Object sample) {   
        return this.toJsonConverters
                .stream()
                .filter(converter -> converter.isApplicableTo(sample))
                .findFirst()
                .orElseThrow(() -> 
                        new JsonConversionException(
                                "Cannot find applicable object-to-json converter for class: " + 
                                        sample.getClass().getCanonicalName())
                );
    }

    ConverterJsonToObject findToObjectConverterFor(Class clazz) {
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
