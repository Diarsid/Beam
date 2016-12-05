/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.commands.executor;

import diarsid.beam.core.domain.commands.Argument;
import diarsid.beam.core.domain.commands.ExecutorCommand;
import diarsid.beam.core.domain.commands.OperationType;

import static diarsid.beam.core.domain.commands.OperationType.SEE_WEBPAGE;


public class SeePageCommand implements ExecutorCommand {
    
    private final Argument pageArgument;
    
    public SeePageCommand(String pageName) {
        this.pageArgument = new Argument(pageName);
    }
    
    public SeePageCommand(String pageName, String extendedPageName, String... attrs) {
        this.pageArgument = new Argument(pageName, extendedPageName);
    }
    
    public Argument page() {
        return this.pageArgument;
    }

    @Override
    public OperationType getOperation() {
        return SEE_WEBPAGE;
    }

    @Override
    public String stringifyOriginal() {
        return this.pageArgument.getOriginal();
    }

    @Override
    public String stringifyExtended() {
        return this.pageArgument.getExtended();
    }
}
