/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.core.jsonconversion;

import java.io.Reader;

/**
 *
 * @author Diarsid
 */
public interface ConverterJsonToObject {
    
    boolean isApplicableTo(Class clazz);
    
    Object convert(String json) throws JsonConversionException;
    
    Object convert(Reader readerWithJson) throws JsonConversionException;
}
