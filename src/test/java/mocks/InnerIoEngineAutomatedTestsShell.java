/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mocks;

import java.util.List;

import diarsid.beam.core.base.analyze.variantsweight.Variants;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.base.interaction.HelpKey;
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
    public String askInput(Initiator initiator, String inputQuestion, Help help) {
        return this.resetableIoShell.askInput(initiator, inputQuestion, help);
    }

    @Override
    public Choice ask(Initiator initiator, String yesOrNoQuestion, Help help) {
        return this.resetableIoShell.ask(initiator, yesOrNoQuestion, help);
    }

    @Override
    public Answer ask(Initiator initiator, VariantsQuestion question, Help help) {
        return this.resetableIoShell.ask(initiator, question, help);
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

    @Override
    public Answer ask(
            Initiator initiator, Variants variants, Help help) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HelpKey addToHelpContext(String... help) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HelpKey addToHelpContext(List<String> help) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
