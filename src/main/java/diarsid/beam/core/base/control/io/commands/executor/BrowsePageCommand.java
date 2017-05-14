/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.commands.executor;

import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.domain.entities.NamedEntityType;

import static diarsid.beam.core.domain.entities.NamedEntityType.WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPAGE;


public class BrowsePageCommand extends InvocationCommand {
    
    public BrowsePageCommand(String pageName) {
        super(pageName);
    }
    
    public BrowsePageCommand(
            String pageName, 
            String extendedPageName, 
            InvocationCommandLifePhase lifePhase, 
            InvocationCommandTargetState targetState) {
        super(pageName, extendedPageName, lifePhase, targetState);
    }

    @Override
    public CommandType type() {
        return BROWSE_WEBPAGE;
    }

    @Override
    public String stringify() {
        return "browse " + super.bestArgument();
    }

    @Override
    public String stringifyOriginal() {
        return "browse " + super.originalArgument();
    }

    @Override
    public NamedEntityType subjectedEntityType() {
        return WEBPAGE;
    }
}
