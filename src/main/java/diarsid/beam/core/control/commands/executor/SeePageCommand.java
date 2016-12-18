/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.commands.executor;

import diarsid.beam.core.control.commands.Argument;
import diarsid.beam.core.control.commands.CommandType;

import static diarsid.beam.core.control.commands.CommandType.SEE_WEBPAGE;

import diarsid.beam.core.control.commands.ArgumentedCommand;


public class SeePageCommand implements ArgumentedCommand {
    
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
    public CommandType getType() {
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
