/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.util.Optional;
import java.util.Set;

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
                .collect(toSet());
    }
    
    Optional<String> getMatchingResourceNameFor(String url) {
        return this.resources
                .stream()
                .filter(resource -> resource.matchesUrl(url))
                .map(resource -> resource.name())
                .findFirst();
    }
}
