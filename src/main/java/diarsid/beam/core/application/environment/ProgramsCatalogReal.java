/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.os.treewalking.search.FileSearcher;
import diarsid.beam.core.base.os.treewalking.search.result.FileSearchResult;
import diarsid.beam.core.domain.entities.Program;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.StringUtils.lower;


class ProgramsCatalogReal 
        extends SearcheableCatalog
        implements ProgramsCatalog {
    
    private static final List<String> PROGRAM_EXTENSIONS;
    static {
        PROGRAM_EXTENSIONS = asList(".exe", ".lnk", ".bat", ".sh");
    }
    
    private final Path catalogPath;
    
    ProgramsCatalogReal(String catalogPath, FileSearcher fileSearcher) {
        super(catalogPath, fileSearcher);
        this.catalogPath = Paths.get(catalogPath).toAbsolutePath().normalize();
    }
    
    private static String removeProgramExtensionFrom(String fileName) {
        for (String extension : PROGRAM_EXTENSIONS) {
            if ( lower(fileName).endsWith(extension) ) {
                return fileName.substring(0, fileName.length() - extension.length());
            }
        }
        return fileName;
    }
    
    private Program fileNameToProgram(String fileName) {
        return new Program(removeProgramExtensionFrom(fileName), fileName, this);
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
        FileSearchResult result = super.findFileInCatalogByStrictName(name);
        Optional<Program> program = Optional.empty();
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                String foundFile = result.success().foundFile();
                if ( this.isNameMatchesFoundFile(name, foundFile) ) {
                    program = Optional.of(this.fileNameToProgram(foundFile));
                } else {
                    program = Optional.empty();
                }               
            } else {
                for (String foundFile : result.success().foundFiles()) {
                    if ( this.isNameMatchesFoundFile(name, foundFile) ) {
                        program = Optional.of(this.fileNameToProgram(foundFile));
                        break;
                    }
                    program = Optional.empty();
                }
            }
        } else {
            program = Optional.empty();
        }
        debug("[PROR CATALOG] [by direct] found : " + program.isPresent());
        return program;
    }
    
    private boolean isNameMatchesFoundFile(String name, String foundFile) {
        if ( lower(foundFile).endsWith(lower(name)) ) {
            return true;
        }
        for (String extension : PROGRAM_EXTENSIONS) {
            if ( lower(foundFile).endsWith(lower(name.concat(extension))) ) {
                return true;
            }
        }
        return false;
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
    public Path path() {
        return this.catalogPath;
    }

    @Override
    public List<Program> getAll() {
        return this.toPrograms(super.findAllFilesInCatalog());
    }
}
