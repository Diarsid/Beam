/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.context;

import java.util.List;

/**
 *
 * @author Diarsid
 */
public interface ExecutorContext {
    
    int resolve(String question, String originalPattern, List<String> variants);
    
    void discardCurrentlyExecutedCommandInPattern(String pattern);
    
    void discardCurrentlyExecutedCommandInPatternAndOperation(
            String operation, String pattern);
    
//    public void discardCurrentlyExecutedCommandInPathPatternAndOperation(
//            String operation, String path, String pattern);
    
    void adjustCurrentlyExecutedCommand(String... newCommandParams);
    
    void adjustCurrentlyExecutedCommand(String newCommand);
    
    String getCurrentCommandFromContext();
    
    boolean ifCanSaveConsoleCommand();
    
    void setContextActive(boolean active);
    
    boolean deleteChoicesForCommand(String command);
    
    List<String> getAllChoices();
    
    void setRememberChoiceAutomatically(boolean auto);
}
