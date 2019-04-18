/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mocks;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
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
public class InnerIoEngineAutomatedTests implements InnerIoEngine {
    
    private final Queue<String> inputsQueue;
    private final Queue<Answer> answersQueue;
    private final Queue<Choice> choicesQueue;
    
    private InnerIoEngineAutomatedTests() {
        this.answersQueue = new ArrayDeque<>();
        this.inputsQueue = new ArrayDeque<>();
        this.choicesQueue = new ArrayDeque<>();
    }
    
    public static InnerIoEngineAutomatedTests automatedIoEngine() {
        return new InnerIoEngineAutomatedTests();
    }
    
    public InnerIoEngineAutomatedTests thenString(String string) {
        this.inputsQueue.add(string);
        return this;
    }
    
    public InnerIoEngineAutomatedTests thenAnswer(Answer answer) {
        this.answersQueue.add(answer);
        return this;
    }
    
    public InnerIoEngineAutomatedTests thenChoice(Choice choice) {
        this.choicesQueue.add(choice);
        return this;
    }

    @Override
    public String askInput(Initiator initiator, String inputQuestion, Help help) {
        return this.inputsQueue.remove();
    }

    @Override
    public Choice ask(Initiator initiator, String yesOrNoQuestion, Help help) {
        return this.choicesQueue.remove();
    }

    @Override
    public Answer ask(Initiator initiator, VariantsQuestion question, Help help) {
        return this.answersQueue.remove();
    }

    @Override
    public void report(Initiator initiator, String string) {
        //
    }

    @Override
    public void reportAndExitLater(Initiator initiator, String string) {
        //
    }

    @Override
    public void reportMessage(Initiator initiator, Message message) {
        //
    }

    @Override
    public void reportMessageAndExitLater(Initiator initiator, Message message) {
        //
    }

    @Override
    public Answer ask(
            Initiator initiator, WeightedVariants variants, Help help) {
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
