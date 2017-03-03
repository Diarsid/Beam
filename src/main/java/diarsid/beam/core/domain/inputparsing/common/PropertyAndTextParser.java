/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.common;

import java.util.List;

import static diarsid.beam.core.domain.entities.metadata.EntityProperty.propertyOf;
import static diarsid.beam.core.domain.inputparsing.common.ArgumentType.DOMAIN_WORD;
import static diarsid.beam.core.domain.inputparsing.common.ArgumentType.ENTITY_PROPERTY;

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
                .filter(arg -> interceptor.interceptArgumentOfType(arg, DOMAIN_WORD).ifContinue())
                .count();
        
        return new PropertyAndText(
                propertyOf(interceptor.argOfType(ENTITY_PROPERTY)), 
                interceptor.argOfType(DOMAIN_WORD));
    }
}
