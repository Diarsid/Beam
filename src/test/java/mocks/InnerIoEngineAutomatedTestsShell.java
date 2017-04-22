/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mocks;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;

/**
 *
 * @author Diarsid
 */
public class InnerIoEngineAutomatedTestsShell implements InnerIoEngine {
    
    private InnerIoEngineAutomatedTests resetableIoShell;
    
    public InnerIoEngineAutomatedTestsShell() {
    }
    
    public void reset(InnerIoEngineAutomatedTests newIo) {
        this.resetableIoShell = newIo;
    }

    @Override
    public String askInput(Initiator initiator, String inputQuestion) {
        return this.resetableIoShell.askInput(initiator, inputQuestion);
    }

    @Override
    public Choice ask(Initiator initiator, String yesOrNoQuestion) {
        return this.resetableIoShell.ask(initiator, yesOrNoQuestion);
    }

    @Override
    public Answer ask(Initiator initiator, VariantsQuestion question) {
        return this.resetableIoShell.ask(initiator, question);
    }

    @Override
    public void report(Initiator initiator, String string) {
        this.resetableIoShell.report(initiator, string);
    }

    @Override
    public void reportAndExitLater(Initiator initiator, String string) {
        this.resetableIoShell.reportAndExitLater(initiator, string);
    }

    @Override
    public void reportMessage(Initiator initiator, Message message) {
        this.resetableIoShell.reportMessage(initiator, message);
    }

    @Override
    public void reportMessageAndExitLater(Initiator initiator, Message message) {
        this.resetableIoShell.reportMessageAndExitLater(initiator, message);
    }
}
