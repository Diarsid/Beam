/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.util.List;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.util.PathUtils.indexOfFirstFileSeparator;
import static diarsid.beam.core.util.PathUtils.normalizeArgument;
import static diarsid.beam.core.util.PathUtils.trimSeparatorsInBothEnds;
import static diarsid.beam.core.util.PathUtils.isAcceptableRelativePath;

/**
 *
 * @author Diarsid
 */
public class PathAnalizer {
    
    public PathAnalizer() {
    }
    
    public List<String> normalizeArguments(List<String> commandParams) {
        return commandParams.stream()
                .map((param) -> normalizeArgument(param))
                .map((param) -> trimSeparatorsInBothEnds(param))
                .collect(toList());
    }
    
    public boolean ifLooksLikePath(List<String> commandParams) {
        if ( commandParams.size() == 1 ) {
            return this.isResolvablePath(commandParams.get(0));
        } else {
            return false;
        }
    }

    private boolean isResolvablePath(String possiblePath) {
        possiblePath = normalizeArgument(possiblePath);
        if ( containsPathSeparator(possiblePath) ) {
            return isAcceptableRelativePath(possiblePath);
        } else {
            return false;
        }
    }
    
    public String extractSubPathFrom(String path) {
        return path.substring(
                indexOfFirstFileSeparator(path) + 1, path.length());
    }
    
    public String extractRootPathFrom(String path) {
        path = path.substring(0, indexOfFirstFileSeparator(path));
        return path;
    }
}
