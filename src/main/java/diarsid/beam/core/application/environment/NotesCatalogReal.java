/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import diarsid.beam.core.base.os.search.FileSearcher;
import diarsid.beam.core.base.os.search.result.FileSearchResult;

import static java.awt.Desktop.getDesktop;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.exists;
import static java.util.Collections.emptyList;


public class NotesCatalogReal 
        extends SearcheableCatalog 
        implements NotesCatalog {

    NotesCatalogReal(String catalogPath, FileSearcher fileSearcher) {
        super(catalogPath, fileSearcher);        
    }

    @Override
    public void createAndOpenNoteWithName(String noteName) throws IOException {
        Path newNote = super.getPath().resolve(noteName);
        if ( exists(newNote) ) {
            getDesktop().open(newNote.toFile());
        } else {
            newNote.getParent().toFile().mkdirs();
            createFile(newNote);
            getDesktop().open(newNote.toFile());
        }
    }

    @Override
    public void open() throws IOException {
        getDesktop().open(super.getPath().toFile());
    }

    @Override
    public void open(String target) throws IOException {
        getDesktop().open(super.getPath().resolve(target).toFile());
    }

    @Override
    public List<String> findByNoteName(String noteName) {
        FileSearchResult result = super.findFileInCatalogByDirectName(noteName);
        if ( result.isOk() ) {
            return result.success().foundFiles();
        }
        
        result = super.findFileInCatalogByStrictName(noteName);
        if ( result.isOk() ) {
            return result.success().foundFiles();
        }
        
        result = super.findFileInCatalogByPattern(noteName);
        if ( result.isOk() ) {
            return result.success().foundFiles();
        }
        
        result = super.findFileInCatalogByPatternSimilarity(noteName);
        if ( result.isOk() ) {
            return result.success().foundFiles();
        }
        
        return emptyList();
    }

    @Override
    public List<String> findByPath(String pathPattern) {
        return super.findFileInCatalogByPatternSimilarity(pathPattern).foundFilesOrNothing();
    }
}
