/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data;

import diarsid.beam.core.modules.executor.commandscache.ActionChoice;
import diarsid.beam.core.modules.executor.commandscache.ActionRequest;

/**
 *
 * @author Diarsid
 */
public interface DaoActionChoice {
    
    boolean saveChoice(ActionChoice actionChoice);
    
    String getChoiceFor(ActionRequest actionRequest);
    
    boolean deleteChoiceFor(String actionArgument);
}
