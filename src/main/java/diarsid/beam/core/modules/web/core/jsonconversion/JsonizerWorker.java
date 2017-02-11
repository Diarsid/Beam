/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.jsonconversion;

import java.io.Reader;
import java.util.Collection;

import static java.util.stream.Collectors.joining;


class JsonizerWorker implements Jsonizer {

    private final ConvertersHolder convertersHolder;
    
    JsonizerWorker(ConvertersHolder convertersHolder) {
        this.convertersHolder = convertersHolder;
    }

    @Override
    public Object objectivize(Class clazz, Reader reader) {
        return this.convertersHolder
                .findToObjectConverterFor(clazz)
                .convert(reader); 
    }

    @Override
    public String jsonize(Object obj) {
        if ( obj instanceof Collection ) {
            return this.jsonizeCollection(obj);
        } else {
            return this.jsonizeSingleObject(obj);
        }
    }
    
    private String jsonizeCollection(Object obj) {
        Collection<Object> collection = (Collection) obj;
        if ( collection.isEmpty() ) {
            return "[]";
        } else {
            Object sample = collection.iterator().next();
            ConverterObjectToJson converter = this.convertersHolder.findToJsonConverterFor(sample);
            return collection
                    .stream()
                    .map(object -> converter.convert(object))
                    .collect(joining(",", "[", "]"));
        }
    }
    
    private String jsonizeSingleObject(Object obj) {
        return this.convertersHolder
                .findToJsonConverterFor(obj)
                .convert(obj);
    }
}
