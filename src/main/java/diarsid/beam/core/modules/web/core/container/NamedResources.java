/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;

/**
 *
 * @author Diarsid
 */
public class NamedResources {
    
    private final Map<String, String> resourcesNamesBySchemas;
    private final Set<String> urlSchemasRegexps;
    
    public NamedResources(Resources resources) {
        this.resourcesNamesBySchemas = resources
                .getAll()
                .stream()
                .collect(toMap(
                        Resource::getUrlMappingSchemaRegexp, 
                        Resource::getName));
        this.urlSchemasRegexps = this.resourcesNamesBySchemas.keySet();
    }  
    
    Optional<String> getResourceNameOf(String incomeRequestUrl) {
        for (String schema : this.urlSchemasRegexps) {
            if ( incomeRequestUrl.matches(schema) ) {
                return of(this.resourcesNamesBySchemas.get(schema));
            }
        }
        return empty();
    }
}
