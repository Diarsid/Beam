/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.processors;

import java.util.List;

/**
 *
 * @author Diarsid
 */
public interface ProcessorNotes {

    void openNote(List<String> commandParams);

    void openNotes();    
}
