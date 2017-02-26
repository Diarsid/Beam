/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.systemconsole;

import java.io.BufferedWriter;
import java.io.IOException;

import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.Question;
import diarsid.beam.core.base.control.io.base.interaction.Variant;

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
        this.writer.write("Beam[1] > ");
        this.writer.flush();
    }
    
    void printInDialogReportLine(String report) throws IOException {
        this.writer.write(format("     > %s", report));
        this.writer.newLine();
        this.writer.flush();
    }
    
    void printNonDialogReportLine(String report) throws IOException {
        this.writer.write(format("Beam[3] > %s", report));
        this.writer.newLine();
        this.writer.write("Beam[3] > ");
        this.writer.flush();
    }
    
    void printYesNoQuestion(String yesNoQuestion) throws IOException {
        this.writer.write(format("     > %s?", yesNoQuestion));
        this.writer.newLine();
        this.writer.write("     > yes / no : ");
        this.writer.flush();
    }

    void printInDialogInviteLine(String invite) throws IOException {
        this.writer.write(format("     > %s : ", invite));
        this.writer.flush();
    }

    void printQuestionAndVariants(Question question) throws IOException {
        Variant variant;
        this.writer.write(format("     > %s", question.getQuestion()));
        this.writer.newLine();
        for (int i = 0; i < question.getVariants().size(); i++) {
            variant = question.getVariants().get(i);
            if ( variant.hasDisplayText() ) {
                this.writer.write(format("       %d : %s", i + 1, variant.getDisplayText()));
                this.writer.newLine();
            } else {
                this.writer.write(format("       %d : %s", i + 1, variant.getText()));
                this.writer.newLine();
            }
        }
        this.printInDialogInviteLine("choose");
    }
    
    void printNonDialogMultilineReport(Message message) throws IOException {
        for (String s : message.toText()) {
            this.writer.write(format("     > %s", s));
        }
        this.writer.newLine();
        this.writer.write("Beam[4] > ");
        this.writer.flush();
    }

    void printInDialogMultilineReport(Message message) throws IOException {
        for (String s : message.toText()) {
            this.writer.write(format("     > %s", s));
        }
        this.writer.newLine();
        this.writer.flush();
    }

}