/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.util.Set;

import diarsid.beam.core.modules.web.core.exceptions.ResourceRegistrationException;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

/**
 *
 * @author Diarsid
 */
public class Resources {
    
    private final Set<Resource> resources;
    
    public Resources(Resource... resources) {
        this.resources = stream(resources)
                .peek(resource -> this.validate(resource))
                .collect(toSet());
    }
    
    public Set<Resource> getAll() {
        return this.resources;
    }
    
    private void validate(Resource resource) {
        boolean schemaValid = resource.getUrlMappingSchema()
                .replaceAll("\\{[a-zA-Z0-9]+\\}", "param")
                .matches(resource.getUrlMappingSchemaRegexp());
        if ( ! schemaValid ) {
            throw new ResourceRegistrationException("Resource " + resource.getClass().getName() + 
                    "is invalid: URL mapping schema " + resource.getUrlMappingSchema() + 
                    "does not match mapping URL regexp.");
        }
    }
}
