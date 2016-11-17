/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.util.List;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.modules.executor.os.search.FileSearchUtils.containsFileSeparator;
import static diarsid.beam.core.modules.executor.os.search.FileSearchUtils.indexOfFirstFileSeparator;
import static diarsid.beam.core.modules.executor.os.search.FileSearchUtils.indexOfLastFileSeparator;
import static diarsid.beam.core.modules.executor.os.search.FileSearchUtils.normalizeSingleCommandParam;
import static diarsid.beam.core.modules.executor.os.search.FileSearchUtils.trimSeparatorsInBothEnds;

/**
 *
 * @author Diarsid
 */
public class PathAnalizer {
    
    public PathAnalizer() {
    }
    
    public List<String> normalizeArguments(List<String> commandParams) {
        return commandParams.stream()
                .map((param) -> normalizeSingleCommandParam(param))
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
        possiblePath = normalizeSingleCommandParam(possiblePath);
        if ( containsFileSeparator(possiblePath) ) {
            return this.pathIsMeaningfull(possiblePath);
        } else {
            return false;
        }
    }
    
    public boolean pathIsMeaningfull(String command) {
        return ( 
                indexOfFirstFileSeparator(command) > 1 && 
                indexOfLastFileSeparator(command) < command.length() - 2);
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