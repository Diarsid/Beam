/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import diarsid.beam.core.base.exceptions.WorkflowBrokenException;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.PathUtils.asName;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCaseAnyFragment;


public class LibrariesCatalogReal 
        implements LibrariesCatalog {
    
    private final Path applicationPath;
    private final Path librariesPath;
    
    LibrariesCatalogReal(String applicationPath, String catalogPath) {
        this.librariesPath = Paths.get(catalogPath).toAbsolutePath().normalize();
        this.applicationPath = Paths.get(applicationPath).toAbsolutePath().normalize();
    }

    @Override
    public List<String> getLibraries() {        
        try {
            return Files.list(this.librariesPath)
                    .map(path -> this.applicationPath.relativize(path).toString())
                    .collect(toList());
        } catch (IOException ex) {
            logError(this.getClass(), ex);
            throw new WorkflowBrokenException("unable to obtain libraries.");
        }
    }

    @Override
    public List<String> getLibrariesWithAny(String... fragments) {
        List<String> libFragments = asList(fragments);
        try {
            return Files.list(this.librariesPath)
                    .filter(path -> containsIgnoreCaseAnyFragment(asName(path), libFragments))
                    .map(path -> this.applicationPath.relativize(path).toString())
                    .collect(toList());
        } catch (IOException ex) {
            logError(this.getClass(), ex);
            throw new WorkflowBrokenException(
                    "unable to obtain libraries with: " + join(", ", fragments));
        }
    }

    @Override
    public Path path() {
        return this.librariesPath;
    }
}
