/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.console;

import java.io.IOException;
import java.util.List;

import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.patternsanalyze.WeightedVariant;

/**
 *
 * @author Diarsid
 */
public interface ConsolePrinter {

    void print(VariantsQuestion question) throws IOException;

    void print(List<WeightedVariant> variants) throws IOException;

    void print(IOException e);

    void printDuringInteraction(String report) throws IOException;

    void printDuringInteraction(Message message) throws IOException;

    void printInDialogInviteLine(String invite) throws IOException;

    void printNonDuringInteraction(String report) throws IOException;

    void printNonDuringInteraction(Message message) throws IOException;

    void printReadyForNewCommandLine() throws IOException;

    void printYesNoQuestion(String yesNoQuestion) throws IOException;
    
}
