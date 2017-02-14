/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.catalogs;

import java.io.File;

import diarsid.beam.core.domain.entities.Program;
import diarsid.beam.core.os.search.FileSearcher;
import diarsid.beam.core.os.search.result.FileSearchResult;


public class ProgramsCatalogReal 
        extends SearcheableCatalog
        implements ProgramsCatalog {
    
    private final String catalogPath;
    
    public ProgramsCatalogReal(String catalogPath, FileSearcher fileSearcher) {
        super(catalogPath, fileSearcher);
        this.catalogPath = catalogPath;
    }

    @Override
    public String getCatalogPath() {
        return this.catalogPath;
    }

    @Override
    public FileSearchResult findProgramByStrictName(String strictName) {
        return super.findFileInCatalogByStrictName(strictName);
    }

    @Override
    public FileSearchResult findProgramByPattern(String nameOrPattern) {
        return super.findFileInCatalogByPattern(nameOrPattern);
    }

    @Override
    public File asFile(Program program) {
        return super.getPath().resolve(program.getFullName()).toFile();
    }
}
