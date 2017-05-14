/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.environment;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Diarsid
 */
public interface NotesCatalog {
    
    void open() throws IOException;
    
    void open(String target) throws IOException;
    
    void createAndOpenNoteWithName(String noteName) throws IOException;
    
    List<String> findByNoteName(String noteName);
    
    List<String> findByPath(String pathPattern);
}
