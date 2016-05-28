/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.Set;

/**
 *
 * @author Diarsid
 */
public interface DaoExecutorConsoleCommands {
       
    Set<String> getAllConsoleCommands();
    
    boolean saveNewConsoleCommand(String command);
    
    boolean remove(String command);
    
    Set<String> getImprovedCommandsForPattern(String pattern); 

    Set<String> getRawCommandsForPattern(String pattern);     
}
