/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.commandscache;

import java.util.List;

import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoActionChoice;

import static diarsid.beam.core.modules.executor.commandscache.ActionChoice.formulateChoiceFor;
import static diarsid.beam.core.modules.executor.commandscache.ActionChoice.formulateNoChoiceFor;
import static diarsid.beam.core.modules.executor.commandscache.ActionChoice.ifUserDoNotWantToResolveTheeseActions;
import static diarsid.beam.core.util.Logs.debug;

/**
 *
 * @author Diarsid
 */
class ActionsResolver {
    
    private final IoInnerModule ioEngine;
    private final DaoActionChoice actionsDao;
    
    ActionsResolver(IoInnerModule ioEngine, DaoActionChoice actionsDao) {
        this.ioEngine = ioEngine;
        this.actionsDao = actionsDao;
    }
    
    String resolve(ActionRequest actionRequest) {
        debug("[CACHE RESOLVER] resolve: " + actionRequest);
        String debugMessage = "[CACHE RESOLVER] ";
        String actionChoice = this.actionsDao.getChoiceFor(actionRequest);
        
        if ( actionChoice.isEmpty() ) {
            debugMessage += "not found in memory, ";
            actionChoice = this.resolveQuestion("action?", actionRequest.getActionVariants());
            if ( actionChoice.isEmpty() ) {                
                debugMessage += "not resolved by user.";
                debug(debugMessage);
                return "";
            } else {
                debugMessage += "resolved by user (" + actionChoice + ").";
                debug(debugMessage);
                this.saveThisActionChoice(actionRequest, actionChoice);
                return actionChoice;
            }            
        } else {
            debugMessage += "found in memory (" + actionChoice + ").";
            debug(debugMessage);
            if ( ifUserDoNotWantToResolveTheeseActions(actionChoice) ) {
                return this.resolveQuestion("action?", actionRequest.getActionVariants());
            } else {
                return actionChoice;
            }
        }
    }

    private void saveThisActionChoice(ActionRequest actionRequest, String actionChoice) {
        if ( this.doesUserWantToUseThisChoiceInFuture() ) {
            this.actionsDao.saveChoice(formulateChoiceFor(actionRequest, actionChoice));
        } else {
            this.actionsDao.saveChoice(formulateNoChoiceFor(actionRequest));
        }
    }
    
    private boolean doesUserWantToUseThisChoiceInFuture() {
        return this.ioEngine.askUserYesOrNo("Use this choice in future?");
    }
    
    String resolveActionsCandidates(String operation, List<String> candidates) {
        return this.resolveQuestion("choose action for '" + operation + "' command:", candidates);        
    }
    
    private String resolveQuestion(String question, List<String> variants) {
        int chosen = this.ioEngine.resolveVariantsWithExternalIO(
                question, variants);
        if ( chosen > 0 ) {
            return variants.get(chosen - 1);
        } else {
            return "";
        }
    }
}
