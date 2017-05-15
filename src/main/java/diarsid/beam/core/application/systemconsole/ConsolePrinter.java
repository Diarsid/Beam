/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.systemconsole;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.domain.patternsanalyze.WeightedVariant;

import static java.lang.String.format;

/**
 *
 * @author Diarsid
 */
public class ConsolePrinter {
    
    private final BufferedWriter writer;     
    
    public ConsolePrinter(BufferedWriter writer) {
        this.writer = writer;
    }
    
    void printReadyForNewCommandLine() throws IOException {
        this.writer.write("Beam > ");
        this.writer.flush();
    }
    
    void printInDialogReportLine(String report) throws IOException {
        this.writer.write(format("     > %s", report));
        this.writer.newLine();
        this.writer.flush();
    }
    
    void printNonDialogReportLine(String report) throws IOException {
        this.writer.write(format("Beam[ndr] > %s", report));
        this.writer.newLine();
        this.writer.write("Beam[ndr] > ");
        this.writer.flush();
    }
    
    void printYesNoQuestion(String yesNoQuestion) throws IOException {
        this.writer.write(format("     > %s ?", yesNoQuestion));
        this.writer.newLine();
        this.writer.write("     > yes/no : ");
        this.writer.flush();
    }

    void printInDialogInviteLine(String invite) throws IOException {
        this.writer.write(format("     > %s : ", invite));
        this.writer.flush();
    }

    void printQuestionAndVariants(VariantsQuestion question) throws IOException {
        this.writer.write(format("     > %s", question.getQuestion()));
        this.writer.newLine();
        for (int i = 0; i < question.getVariants().size(); i++) {
            this.writer.write(
                    format("       %d : %s", i + 1, question.getVariants().get(i).bestText()));
            this.writer.newLine();
        }
        this.printInDialogInviteLine("choose");
    }
    
    void printInDialogWeightedVariants(List<WeightedVariant> variants) throws IOException {
        this.writer.write("     > is one of ?");
        this.writer.newLine();
        for (int i = 0; i < variants.size(); i++) {
            this.writer.write(format("       %d : %s", i + 1, variants.get(i).bestText()));
            this.writer.newLine();
        }
        this.printInDialogInviteLine("choose");
    }
    
    void printNonDialogMultilineReport(Message message) throws IOException {
        for (String s : message.toText()) {
            this.writer.write(format("     > %s", s));
            this.writer.newLine();
        }
        this.writer.write("Beam[ndmr] > ");
        this.writer.flush();
    }

    void printInDialogMultilineReport(Message message) throws IOException {
        for (String s : message.toText()) {
            this.writer.write(format("     > %s", s));
            this.writer.newLine();
        }
        this.writer.flush();
    }

}
