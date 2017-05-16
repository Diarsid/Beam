/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mocks;

import java.util.ArrayDeque;
import java.util.Queue;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.domain.patternsanalyze.WeightedVariants;

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
    public String askInput(Initiator initiator, String inputQuestion) {
        return this.inputsQueue.remove();
    }

    @Override
    public Choice ask(Initiator initiator, String yesOrNoQuestion) {
        return this.choicesQueue.remove();
    }

    @Override
    public Answer ask(Initiator initiator, VariantsQuestion question) {
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
    public Answer chooseInWeightedVariants(Initiator initiator, WeightedVariants variants) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
