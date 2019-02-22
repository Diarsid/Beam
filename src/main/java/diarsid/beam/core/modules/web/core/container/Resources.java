/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Arrays.stream;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;

import static diarsid.support.strings.StringUtils.isEmpty;

/**
 *
 * @author Diarsid
 */
public class Resources {
    
    private final String resourcesPath;
    private final List<Resource> resources;
    
    public Resources(String path, Resource... resources) {
        this.resourcesPath = path;
        this.resources = stream(resources)
                .collect(toList());
        sort(this.resources);
    }
    
    public String path() {
        return resourcesPath;
    }
    
    Optional<Resource> getMatchingResourceFor(String url, String method) {
        if ( isEmpty(url) ) {
            return Optional.empty();
        }
        return this.resources
                .stream()
                .filter(resource -> resource.matchesTo(url, method))
                .findFirst();
    }
    
    public void doForEach(Consumer<Resource> action) {
        this.resources.forEach(action);
    }
}
