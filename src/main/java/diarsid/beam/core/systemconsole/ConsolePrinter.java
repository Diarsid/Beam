/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

import java.io.BufferedWriter;
import java.io.IOException;

import diarsid.beam.core.control.io.base.Question;
import diarsid.beam.core.control.io.base.TextMessage;
import diarsid.beam.core.control.io.base.Variant;

import static java.lang.String.format;

/**
 *
 * @author Diarsid
 */
class ConsolePrinter {
    
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
                this.writer.write(format("       %d : %s", i + 1, variant.getDisplay()));
                this.writer.newLine();
            } else {
                this.writer.write(format("       %d : %s", i + 1, variant.get()));
                this.writer.newLine();
            }
        }
        printInDialogInviteLine("choose");
    }
    
    void printNonDialogMultilineReport(TextMessage message) throws IOException {
        for (String s : message.getText()) {
            this.writer.write(format("     > %s", s));
        }
        this.writer.newLine();
        this.writer.write("Beam[4] > ");
        this.writer.flush();
    }

    void printInDialogMultilineReport(TextMessage message) throws IOException {
        for (String s : message.getText()) {
            this.writer.write(format("     > %s", s));
        }
        this.writer.newLine();
        this.writer.flush();
    }
    
    void printNonDialogReport(String report) throws IOException {
        this.writer.write(format("Beam[3] > %s", report));
        this.writer.newLine();
        this.writer.write("Beam[3] > ");
        this.writer.flush();
    }

}
