/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.systemconsole;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariant;
import diarsid.beam.core.base.control.io.base.console.ConsolePrinter;
import diarsid.beam.core.base.control.io.base.interaction.HelpInfo;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;

import static java.lang.String.format;

import static diarsid.beam.core.base.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
class SystemConsolePrinter implements ConsolePrinter {
    
    private final BufferedWriter writer;     
    
    SystemConsolePrinter(BufferedWriter writer) {
        this.writer = writer;
    }
    
    @Override
    public void printReadyForNewCommandLine() throws IOException {
        this.writer.write("Beam > ");
        this.writer.flush();
    }
    
    @Override
    public void printDuringInteraction(String report) throws IOException {
        this.writer.write(format("     > %s", report));
        this.writer.newLine();
        this.writer.flush();
    }
    
    @Override
    public void printNonDuringInteraction(String report) throws IOException {
        this.writer.newLine();
        this.writer.write(format("Beam[ndr] > %s", report));
        this.writer.newLine();
        this.writer.write("Beam[ndr] > ");
        this.writer.flush();
    }
    
    @Override
    public void printYesNoQuestion(String yesNoQuestion) throws IOException {
        this.writer.write(format("     > %s ?", yesNoQuestion));
        this.writer.newLine();
        this.writer.write("     > yes/no : ");
        this.writer.flush();
    }

    @Override
    public void printInDialogInviteLine(String invite) throws IOException {
        this.writer.write(format("     > %s : ", invite));
        this.writer.flush();
    }

    @Override
    public void print(VariantsQuestion question) throws IOException {
        this.writer.write(format("     > %s", question.getQuestion()));
        this.writer.newLine();
        for (int i = 0; i < question.getVariants().size(); i++) {
            this.writer.write(
                    format("       %d : %s", i + 1, question.getVariants().get(i).bestText()));
            this.writer.newLine();
        }
        this.printInDialogInviteLine("choose");
    }
    
    @Override
    public void print(List<WeightedVariant> variants) throws IOException {
        this.writer.write("     > is one of ?");
        this.writer.newLine();
        for (int i = 0; i < variants.size(); i++) {
            this.writer.write(format("       %d : %s", i + 1, variants.get(i).bestText()));
            this.writer.newLine();
        }
        this.printInDialogInviteLine("choose");
    }
    
    @Override
    public void printNonDuringInteraction(Message message) throws IOException {
        for (String s : message.toText()) {
            this.writer.write(format("       %s", s));
            this.writer.newLine();
        }
        this.writer.write("Beam[ndmr] > ");
        this.writer.flush();
    }
    
    @Override
    public void printNonDuringInteraction(HelpInfo help) throws IOException {
        this.writer.newLine();
        for (String helpLine : help.getLines()) {
            this.writer.write(format("  %s", helpLine));
            this.writer.newLine();
        }
        this.writer.newLine();
        this.writer.write("Beam > ");
        this.writer.flush();
    }

    @Override
    public void printDuringInteraction(Message message) throws IOException {
        for (String s : message.toText()) {
            this.writer.write(format("       %s", s));
            this.writer.newLine();
        }
        this.writer.flush();
    }
    
    @Override
    public void printDuringInteraction(HelpInfo help) throws IOException {
        this.writer.newLine();
        for (String helpLine : help.getLines()) {
            this.writer.write(format("  %s", helpLine));
            this.writer.newLine();
        }
        this.writer.newLine();
        this.writer.flush();
    }
    
    @Override
    public void print(Exception e) {
        try {
            this.writer.newLine();
            this.writer.write(e.getMessage());
            this.writer.newLine();
            this.writer.flush();
        } catch (IOException ex) {
            logError(this.getClass(), ex);
        }
    }
}
