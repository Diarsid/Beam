/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import diarsid.beam.core.base.os.search.FileSearcher;
import diarsid.beam.core.base.os.search.result.FileSearchResult;
import diarsid.beam.core.domain.entities.Program;


public class ProgramsCatalogReal 
        extends SearcheableCatalog
        implements ProgramsCatalog {
    
    private final Path catalogPath;
    
    public ProgramsCatalogReal(String catalogPath, FileSearcher fileSearcher) {
        super(catalogPath, fileSearcher);
        this.catalogPath = Paths.get(catalogPath).toAbsolutePath().normalize();
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
        return super.getPath().resolve(program.fullName()).toFile();
    }

    @Override
    public Path path() {
        return this.catalogPath;
    }
}
