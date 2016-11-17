/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import java.util.List;

import diarsid.beam.core.modules.executor.workflow.CurrentCommandState;

/**
 *
 * @author Diarsid
 */
public interface DaoExecutorIntelligentChoices {
    
    String getChoiceForCommandPart(
            String command, int attemptNumber, String pattern);
    
    boolean saveChoiceForCommandAndItsPart(CurrentCommandState commandState);
    
    boolean deleteChoicesForCommand(String command);
    
    boolean discardCommandByPattern(String pattern);
    
    boolean discardCommandByInvalidLocationInPath(String invalidLocationName);
    
    boolean discardCommandByInvalidTargetInPath(String target);
    
    boolean discardCommandByPatternAndOperation(String operation, String pattern);
    
//    boolean discardCommandByPathPatternAndOperation(String operation, String pathToTarget, String targetPattern);
    
    List<CurrentCommandState> getAllChoices();
    
    List<CurrentCommandState> getChoicesWhereCommandLike(String commandPart);

    List<String> formatCommandsForOutput(List<CurrentCommandState> commands);
}
