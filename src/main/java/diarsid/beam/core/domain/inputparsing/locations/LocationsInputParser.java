/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.locations;

import java.util.List;

import diarsid.beam.core.domain.inputparsing.ArgumentsInterceptor;

import static diarsid.beam.core.domain.inputparsing.ArgumentType.FILE_PATH;
import static diarsid.beam.core.domain.inputparsing.ArgumentType.SIMPLE_WORD;

/**
 *
 * @author Diarsid
 */
public class LocationsInputParser {
    
    public LocationsInputParser() {
    }
    
    public LocationNameAndPath parse(List<String> arguments) {
        ArgumentsInterceptor interceptor = new ArgumentsInterceptor();
        arguments
                .stream()                
                .filter(arg -> interceptor.interceptArgumentOfType(arg, FILE_PATH).ifContinue())
                .filter(arg -> interceptor.interceptArgumentOfType(arg, SIMPLE_WORD).ifContinue())
                .count();
        
        return new LocationNameAndPath(
                interceptor.argOfType(SIMPLE_WORD), 
                interceptor.argOfType(FILE_PATH));
    }
}
