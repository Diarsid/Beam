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

import static diarsid.beam.core.base.util.Logs.debug;


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
        debug("[PROR CATALOG] [by direct] " + name);
        Optional<Program> program = this.toProgram(super.findFileInCatalogByDirectName(name));
        debug("[PROR CATALOG] [by direct] found : " + program.isPresent());
        return program;
    }

    @Override
    public List<Program> findProgramsByStrictName(String strictName) {
        debug("[PROR CATALOG] [by strict] " + strictName);
        List<Program> programs = this.toPrograms(super.findFileInCatalogByStrictName(strictName));
        debug("[PROR CATALOG] [by strict] " + programs);
        return programs;
    }

    @Override
    public List<Program> findProgramsByWholePattern(String pattern) {
        debug("[PROR CATALOG] [by whole pattern] " + pattern);
        // TODO
        List<Program> programs = this.toPrograms(super.findFileInCatalogByPattern(pattern));
        debug("[PROR CATALOG] [by whole pattern] " + programs);
        return programs;
    }

    @Override
    public List<Program> findProgramsByPatternSimilarity(String pattern) {
        debug("[PROR CATALOG] [by similarity] " + pattern);
        List<Program> programs = this.toPrograms(super.findFileInCatalogByPatternSimilarity(pattern));
        debug("[PROR CATALOG] [by similarity] " + programs);
        return programs;
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
