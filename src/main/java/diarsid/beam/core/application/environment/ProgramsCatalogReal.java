/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.os.search.FileSearcher;
import diarsid.beam.core.base.os.search.result.FileSearchResult;
import diarsid.beam.core.domain.entities.Program;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;


public class ProgramsCatalogReal 
        extends SearcheableCatalog
        implements ProgramsCatalog {
    
    private final Path catalogPath;
    
    ProgramsCatalogReal(String catalogPath, FileSearcher fileSearcher) {
        super(catalogPath, fileSearcher);
        this.catalogPath = Paths.get(catalogPath).toAbsolutePath().normalize();
    }
    
    private Program fileNameToProgram(String fileName) {
        return new Program(this, fileName);
    }

    private List<Program> toPrograms(FileSearchResult result) {
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                return asList(this.fileNameToProgram(result.success().foundFile()));
            } else {
                return result
                        .success()
                        .foundFiles()
                        .stream()
                        .map(file -> this.fileNameToProgram(file))
                        .collect(toList());
            }
        } else {
            return emptyList();
        }
    }
    
    private Optional<Program> toProgram(FileSearchResult result) {
        if ( result.isOk() && result.success().hasSingleFoundFile() ) {
            return Optional.of(this.fileNameToProgram(result.success().foundFile()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Program> findProgramByDirectName(String name) {
        return this.toProgram(super.findFileInCatalogByDirectName(name));
    }

    @Override
    public List<Program> findProgramsByStrictName(String strictName) {
        return this.toPrograms(super.findFileInCatalogByStrictName(strictName));
    }

    @Override
    public List<Program> findProgramsByWholePattern(String pattern) {
        return this.toPrograms(super.findFileInCatalogByPattern(pattern));
    }

    @Override
    public List<Program> findProgramsByPatternSimilarity(String pattern) {
        return this.toPrograms(super.findFileInCatalogByPatternSimilarity(pattern));
    }

    @Override
    public File asFile(Program program) {
        return super.getPath().resolve(program.name()).toFile();
    }

    @Override
    public Path path() {
        return this.catalogPath;
    }
}
