/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.console;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariant;
import diarsid.beam.core.base.control.io.base.console.ConsolePrinter;
import diarsid.beam.core.base.control.io.base.interaction.HelpInfo;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;

/**
 *
 * @author Diarsid
 */
class ConsoleBlockingIncome implements ConsolePrinter {
    
    private final BlockingQueue<String> intoConsole;
    
    ConsoleBlockingIncome() {
        this.intoConsole = new ArrayBlockingQueue<>(1, true);
    }

    @Override
    public void print(VariantsQuestion question) throws IOException {
        
    }

    @Override
    public void print(List<WeightedVariant> variants) throws IOException {
        
    }

    @Override
    public void print(Exception e) {
        
    }

    @Override
    public void printDuringInteraction(String report) throws IOException {
        
    }

    @Override
    public void printDuringInteraction(Message message) throws IOException {
        
    }

    @Override
    public void printDuringInteraction(HelpInfo help) throws IOException {
        
    }

    @Override
    public void printInDialogInviteLine(String invite) throws IOException {
        
    }

    @Override
    public void printNonDuringInteraction(String report) throws IOException {
        
    }

    @Override
    public void printNonDuringInteraction(Message message) throws IOException {
        
    }

    @Override
    public void printNonDuringInteraction(HelpInfo help) throws IOException {
        
    }

    @Override
    public void printReadyForNewCommandLine() throws IOException {
        
    }

    @Override
    public void printYesNoQuestion(String yesNoQuestion) throws IOException {
        
    }
    
}
