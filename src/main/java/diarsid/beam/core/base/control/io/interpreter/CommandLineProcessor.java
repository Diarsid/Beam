/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter;

import java.util.List;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.commands.Command;

import static diarsid.beam.core.base.control.io.commands.CommandType.INCORRECT;
import static diarsid.beam.core.base.control.io.commands.CommandType.MULTICOMMAND;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.StringUtils.joinFromIndex;
import static diarsid.beam.core.base.util.StringUtils.splitBySpacesToList;

/**
 *
 * @author Diarsid
 */
public class CommandLineProcessor {
    
    private final Interpreter interpreter;
    private final CommandDispatcher dispatcher;
    
    public CommandLineProcessor(Interpreter interpreter, CommandDispatcher dispatcher) {
        this.interpreter = interpreter;
        this.dispatcher = dispatcher;
    }
    
    public void process(Initiator initiator, String commandLine) {
        Command command = this.interpreter.interprete(commandLine);
        if ( command.type().is(INCORRECT) || command.type().isUndefined() ) {
            debug("initiator:" + initiator.identity() + " commandType: " + command.type());
            return;
        }
        if ( command.type().isNot(MULTICOMMAND) && command.type().isDefined() ) {
            this.dispatcher.dispatch(initiator, command);
        } else {
            this.tryToInterpreteAsSentencesFromLeftToRight(
                    initiator, splitBySpacesToList(commandLine));
//            splitBySpacesToList(commandLine)
//                    .stream()
//                    .map(newCommandLine -> this.interpreter.interprete(newCommandLine))
//                    .forEach(newCommand -> this.dispatcher.dispatch(initiator, newCommand));            
        }        
    }
    
    private void tryToInterpreteAsSentencesFromLeftToRight(
            Initiator initiator, List<String> fragments) {
        Command command = this.interpreter.interprete(fragments.get(0));
        this.dispatcher.dispatch(initiator, command);
        for (int i = 1; i < fragments.size(); i++) {
            command = this.interpreter.interprete(joinFromIndex(i, fragments));
            if ( command.type().isDefined() && command.type().isNot(MULTICOMMAND) ) {
                this.dispatcher.dispatch(initiator, command);
                return;
            } else {
                command = this.interpreter.interprete(fragments.get(i));
                if ( command.type().isDefined() ) {
                    this.dispatcher.dispatch(initiator, command);
                }                
            }
        }
    }
}
