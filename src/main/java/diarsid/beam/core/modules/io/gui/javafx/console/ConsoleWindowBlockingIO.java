/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx.console;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariant;
import diarsid.beam.core.base.control.io.base.console.ConsoleIO;
import diarsid.beam.core.base.control.io.base.interaction.HelpInfo;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;

import static java.lang.String.format;

import static diarsid.support.log.Logging.logFor;

/**
 *
 * @author Diarsid
 */
class ConsoleWindowBlockingIO implements ConsoleIO {
    
    private final BlockingQueue<String> fromConsole;
    private final BlockingQueue<String> intoConsole;
    
    ConsoleWindowBlockingIO() {
        this.intoConsole = new ArrayBlockingQueue<>(1, true);
        this.fromConsole = new ArrayBlockingQueue<>(1, true);
    }
    
    String blockingGetPrintedString() throws InterruptedException {
        return this.intoConsole.take();
    }
    
    void blockingSetString(String line) throws InterruptedException{
        this.fromConsole.put(line);   
    }

    @Override
    public String readLine() throws Exception {
        try {
            return this.fromConsole.take();
        } catch (InterruptedException e) {
            throw new Exception(e);
        }
    }
    
    @Override
    public void printReadyForNewCommandLine() throws Exception {
        this.intoConsole.put("Beam > ");
    }
    
    @Override
    public void printDuringInteraction(String report) throws Exception {
        this.intoConsole.put(format("     > %s\n", report));
    }
    
    @Override
    public void printNonDuringInteraction(String report) throws Exception {
        this.intoConsole.put(
                format(
                        "\nBeam > %s" +
                        "\nBeam > ", report.replace("\n", "")));
    }
    
    @Override
    public void printYesNoQuestion(String yesNoQuestion) throws Exception {
        this.intoConsole.put(
                format(
                        "     > %s ?" +
                        "\n     > yes/no : ", yesNoQuestion));
    }

    @Override
    public void printInDialogInviteLine(String invite) throws Exception {
        this.intoConsole.put(format("     > %s : ", invite));
    }

    @Override
    public void print(VariantsQuestion question) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(format("     > %s", question.getQuestion()));
        for (int i = 0; i < question.getVariants().size(); i++) {
            sb.append(
                    format("\n       %d : %s", i + 1, question.getVariants().get(i).bestText()));
        }
        sb.append("\n     > choose : ");
        this.intoConsole.put(sb.toString());
    }
    
    @Override
    public void print(List<WeightedVariant> variants) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("     > is one of ?");
        for (int i = 0; i < variants.size(); i++) {
            sb.append(format("\n       %d : %s", i + 1, variants.get(i).bestText()));
        }
        sb.append("\n     > choose : ");
        this.intoConsole.put(sb.toString());
    }
    
    @Override
    public void printNonDuringInteraction(Message message) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String s : message.allLines()) {
            sb.append(format("       %s\n", s));
        }
        sb.append("Beam > ");
        this.intoConsole.put(sb.toString());
    }
    
    @Override
    public void printNonDuringInteraction(HelpInfo help) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String helpLine : help.getLines()) {
            sb.append(format("\n  %s", helpLine));
        }
        sb.append("\n\nBeam > ");
        this.intoConsole.put(sb.toString());
    }

    @Override
    public void printDuringInteraction(Message message) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String s : message.allLines()) {
            sb.append(format("       %s\n", s));
        }
        this.intoConsole.put(sb.toString());
    }
    
    @Override
    public void printDuringInteraction(HelpInfo help) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String helpLine : help.getLines()) {
            sb.append(format("\n  %s", helpLine));
        }
        sb.append("\n\n");
        this.intoConsole.put(sb.toString());
    }
    
    @Override
    public void print(Exception e) {
        try {
            logFor(this).error("Exception occured", e);
            this.intoConsole.put("\n" + e.getMessage() + "\n");
        } catch (InterruptedException ex) {
            logFor(this).error("console blocking IO print() has been interruped: ", ex);
        }
    }
    
}
