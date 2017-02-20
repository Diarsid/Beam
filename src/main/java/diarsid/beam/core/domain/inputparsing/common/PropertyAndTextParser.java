/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.common;

import java.util.List;

import static diarsid.beam.core.domain.entities.metadata.EntityProperty.argToProperty;
import static diarsid.beam.core.domain.inputparsing.common.ArgumentType.ENTITY_PROPERTY;
import static diarsid.beam.core.domain.inputparsing.common.ArgumentType.FILE_PATH;
import static diarsid.beam.core.domain.inputparsing.common.ArgumentType.SIMPLE_WORD;

/**
 *
 * @author Diarsid
 */
public class PropertyAndTextParser {
    
    public PropertyAndTextParser() {
    }
    
    public PropertyAndText parse(List<String> arguments) {
        ArgumentsInterceptor interceptor = new ArgumentsInterceptor();
        arguments
                .stream()                
                .filter(arg -> interceptor.interceptArgumentOfType(arg, ENTITY_PROPERTY).ifContinue())
                .filter(arg -> interceptor.interceptArgumentOfType(arg, SIMPLE_WORD).ifContinue())
                .count();
        
        return new PropertyAndText(
                argToProperty(interceptor.argOfType(ENTITY_PROPERTY)), 
                interceptor.argOfType(FILE_PATH));
    }
}
