/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.catalogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.Logs.logError;


public class LibrariesCatalogReal 
        implements LibrariesCatalog {
    
    private final String catalogPath;
    
    public LibrariesCatalogReal(String catalogPath) {
        this.catalogPath = catalogPath;
    }

    @Override
    public String getCatalogPath() {
        return this.catalogPath;
    }

    @Override
    public List<String> getLibraries() {
        Path libPath = Paths.get(this.catalogPath);
        List<String> libs;
        try {
            libs = Files.list(libPath)
                    .map(path -> this.addCatalogSubpath(path.toString()))
                    .collect(toList());
        } catch (IOException ex) {
            logError(this.getClass(), ex);
            return emptyList();
        }
        return libs;
    }
    
    private String addCatalogSubpath(String library) {
        return this.catalogPath + "/" + library;
    }
}
