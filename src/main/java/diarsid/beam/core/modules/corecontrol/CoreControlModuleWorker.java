/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.corecontrol;

import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.interaction.Question;
import diarsid.beam.core.base.control.io.interpreter.CommandLineProcessor;
import diarsid.beam.core.modules.CoreControlModule;
import diarsid.beam.core.modules.IoModule;

import static diarsid.beam.core.Beam.exitBeamCoreNow;
import static diarsid.beam.core.base.control.io.base.interaction.Question.question;
import static diarsid.beam.core.base.util.Logs.debug;

/**
 *
 * @author Diarsid
 */
public class CoreControlModuleWorker implements CoreControlModule {
    
    private final IoModule ioModule;
    private final CommandLineProcessor cliProcessor;
    
    public CoreControlModuleWorker(IoModule ioModule, CommandLineProcessor cliProcessor) {
        this.ioModule = ioModule;
        this.cliProcessor = cliProcessor;
    }

    @Override
    public void exitBeam() {
        new Thread(() -> {
            exitBeamCoreNow();
        }).start();
    }

    @Override
    public void executeCommand(Initiator initiator, String commandLine) {
        debug("initiator:" + initiator.getId() + " command: " + commandLine );
        if ( this.ioModule.isInitiatorLegal(initiator) ) {
            if ( commandLine.length() % 2 == 1 ) {
                if ( this.ioModule.getInnerIoEngine().ask(initiator, "proceed?").isPositive() ) {
                    this.ioModule.getInnerIoEngine().report(initiator, "...proceeded!");
                }
            }
            if ( commandLine.equals("ask") ) {
                Question question = question("choose")
                        .withAnswerStrings("one", "two", "three");
                Answer answer = this.ioModule
                        .getInnerIoEngine()
                        .ask(initiator, question);
                if ( answer.isGiven() ) {
                    this.ioModule.getInnerIoEngine().report(initiator, "your choice is : " + answer.getText());
                } else {
                    this.ioModule.getInnerIoEngine().report(initiator, "you have not chosen anything.");
                }
            }
            this.cliProcessor.process(initiator, commandLine);
        }
        debug("executed...");
    }
}
