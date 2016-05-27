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
public interface IntelligentExecutorCommandContext {
    
    int resolve(String question, String originalPattern, List<String> variants);
    
    void discardCurrentlyExecutedCommandInPattern(String pattern);
    
    void adjustCurrentlyExecutedCommand(String... newCommandParams);
    
    void adjustCurrentlyExecutedCommand(String newCommand);
    
    boolean ifCanSaveConsoleCommand();
    
    void setContextActive(boolean active);
    
    boolean deleteChoicesForCommand(String command);
    
    List<String> getAllChoices();
    
    void setRememberChoiceAutomatically(boolean auto);
}
