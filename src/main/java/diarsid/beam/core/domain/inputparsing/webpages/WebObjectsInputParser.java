/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.inputparsing.webpages;

import java.util.List;

import diarsid.beam.core.domain.inputparsing.common.ArgumentsInterceptor;

import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;
import static diarsid.beam.core.domain.inputparsing.common.ArgumentType.WEB_PATH;
import static diarsid.beam.core.domain.inputparsing.common.ArgumentType.WEB_PLACE;
import static diarsid.beam.core.domain.inputparsing.common.ArgumentType.DOMAIN_WORD;

/**
 *
 * @author Diarsid
 */
public class WebObjectsInputParser {
    
    public WebObjectsInputParser() {
    }
    
    public WebDirectoryNameAndPlace parseNameAndPlace(List<String> arguments) {
        ArgumentsInterceptor interceptor = new ArgumentsInterceptor();
        arguments
                .stream()
                .filter(arg -> interceptor.interceptArgumentOfType(arg, WEB_PLACE).ifContinue())
                .filter(arg -> interceptor.interceptArgumentOfType(arg, DOMAIN_WORD).ifContinue())
                .count();

        return new WebDirectoryNameAndPlace(
                interceptor.argOfType(DOMAIN_WORD), 
                parsePlace(interceptor.argOfType(WEB_PLACE))
        );           
    }
    
    public WebPageNameUrlAndPlace parseNameUrlAndPlace(List<String> arguments) {
        ArgumentsInterceptor interceptor = new ArgumentsInterceptor();
        arguments
                .stream()
                .filter(arg -> interceptor.interceptArgumentOfType(arg, WEB_PATH).ifContinue())
                .filter(arg -> interceptor.interceptArgumentOfType(arg, WEB_PLACE).ifContinue())
                .filter(arg -> interceptor.interceptArgumentOfType(arg, DOMAIN_WORD).ifContinue())
                .count();

        return new WebPageNameUrlAndPlace(
                interceptor.argOfType(DOMAIN_WORD), 
                interceptor.argOfType(WEB_PATH), 
                parsePlace(interceptor.argOfType(WEB_PLACE))
        );
    }
}
