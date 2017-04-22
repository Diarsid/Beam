/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.domain.entities.NamedEntityType;

import static diarsid.beam.core.base.control.io.commands.CommandType.SEE_WEBPAGE;
import static diarsid.beam.core.domain.entities.NamedEntityType.WEBPAGE;


public class SeePageCommand extends InvocationCommand {
    
    public SeePageCommand(String pageName) {
        super(pageName);
    }
    
    public SeePageCommand(
            String pageName, 
            String extendedPageName, 
            InvocationCommandLifePhase lifePhase, 
            InvocationCommandTargetState targetState) {
        super(pageName, extendedPageName, lifePhase, targetState);
    }

    @Override
    public CommandType type() {
        return SEE_WEBPAGE;
    }

    @Override
    public String stringify() {
        return "see " + super.bestArgument();
    }

    @Override
    public NamedEntityType subjectedEntityType() {
        return WEBPAGE;
    }
}
