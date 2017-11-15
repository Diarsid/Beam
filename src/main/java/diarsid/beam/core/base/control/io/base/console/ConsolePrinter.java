/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.console;

import java.util.List;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariant;
import diarsid.beam.core.base.control.io.base.interaction.HelpInfo;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;

/**
 *
 * @author Diarsid
 */
public interface ConsolePrinter {

    void print(VariantsQuestion question) throws Exception;

    void print(List<WeightedVariant> variants) throws Exception;

    void print(Exception e);

    void printDuringInteraction(String report) throws Exception;

    void printDuringInteraction(Message message) throws Exception;
    
    void printDuringInteraction(HelpInfo help) throws Exception;

    void printInDialogInviteLine(String invite) throws Exception;

    void printNonDuringInteraction(String report) throws Exception;

    void printNonDuringInteraction(Message message) throws Exception;
    
    void printNonDuringInteraction(HelpInfo help) throws Exception;

    void printReadyForNewCommandLine() throws Exception;

    void printYesNoQuestion(String yesNoQuestion) throws Exception;
    
}
