/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor;

import java.util.List;

/**
 *
 * @author Diarsid
 */
interface CurrentlyExecutedCommandContainer {
    
    void saveCurrentlyExecutedCommand(List<String> commandParams);
    
    void clearCurrentlyExecutedCommand();    
}
