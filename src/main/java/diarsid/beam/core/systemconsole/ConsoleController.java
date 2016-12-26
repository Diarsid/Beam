/*
 * To change this license header, answerOf License Headers in Project Properties.
 * To change this template file, answerOf Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import diarsid.beam.core.control.io.base.Answer;
import diarsid.beam.core.control.io.base.Choice;
import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.TextMessage;
import diarsid.beam.core.control.io.base.OuterIoEngine;
import diarsid.beam.core.control.io.base.Question;
import diarsid.beam.core.control.io.base.Variant;
import diarsid.beam.core.rmi.RemoteAccessEndpoint;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.control.io.base.Answer.noAnswer;
import static diarsid.beam.core.control.io.base.Choice.choiceOfPattern;
import static diarsid.beam.core.systemconsole.SystemConsole.exitSystemConsole;
import static diarsid.beam.core.systemconsole.SystemIO.provideReader;
import static diarsid.beam.core.systemconsole.SystemIO.provideWriter;
import static diarsid.beam.core.util.StringUtils.isNumeric;

/**
 *
 * @author Diarsid
 */
public class ConsoleController implements OuterIoEngine {
        
    private final BufferedReader reader;
    private final BufferedWriter writer;  
    private final AtomicBoolean operationInProcess;
    private Initiator initiator;
    private RemoteAccessEndpoint remoteAccess;
    private String consoleName;
    
    public ConsoleController() {
        this.reader = provideReader();
        this.writer = provideWriter();
        this.operationInProcess = new AtomicBoolean(false);
        this.consoleName = "default";
    }

    public void setRemoteAccess(RemoteAccessEndpoint remoteAccess) {
        this.remoteAccess = remoteAccess;
    }
    
    public void setName(String name) {
        this.consoleName = name;
    }
    
    private void startConsoleRunner() {        
        new Thread(() -> {
            String command;            
            while ( true ) {
                try {
                    this.printReadyForNewCommandLine();
                    command = this.readLine(); 
                    this.operationInProcess.set(true);
                    if ( ! command.isEmpty() && nonNull(this.initiator) ) {
                        this.remoteAccess.executeCommand(this.initiator, command);
                    }                    
                    this.operationInProcess.set(false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } 
            }
        }).start();
    }

    private void printReadyForNewCommandLine() throws IOException {
        this.writer.write("Beam[1] > ");
        this.writer.flush();
    }

    @Override
    public Choice resolveYesOrNo(String yesOrNoQuestion) throws IOException {
        this.printYesOrNoQuestion(yesOrNoQuestion);
        return choiceOfPattern(this.readLine());
    }

    private String readLine() throws IOException {
        return this.reader.readLine().trim();
    }

    private void printYesOrNoQuestion(String yesOrNoQuestion) throws IOException {
        this.printInDialogReportLine(yesOrNoQuestion);
        this.printInDialogInviteLine("yes / no");
    }

    @Override
    public Answer resolveQuestion(Question question) throws IOException {
        this.printQuestionAndVariants(question);
        return this.askForAnswer(question);
    }

    private Answer askForAnswer(Question question) throws IOException, NumberFormatException {
        boolean notResolved = true;
        String line = "";
        int chosenVariant = -1;
        Answer answer = noAnswer();
        while ( notResolved ) {
            line = this.readLine();            
            if ( isNumeric(line) ) {
                chosenVariant = parseInt(line);
                if ( question.isChoiceInVariantsNaturalRange(chosenVariant) ) {
                    notResolved = false;
                    answer = question.answerWith(chosenVariant);
                } else {
                    printInDialogReportLine("not in variants range.");
                    printInDialogInviteLine("choose");
                }
            } else {
                printInDialogInviteLine("choose");
            }
        }
        return answer;
    }

    private void printInDialogReportLine(String report) throws IOException {
        this.writer.write(format("     > %s", report));
        this.writer.newLine();
        this.writer.flush();
    }

    private void printInDialogInviteLine(String invite) throws IOException {
        this.writer.write(format("     > %s : ", invite));
        this.writer.flush();
    }

    private void printQuestionAndVariants(Question question) throws IOException {
        Variant variant;
        this.writer.write(format("     > %s", question.getQuestion()));
        for (int i = 0; i < question.getVariants().size(); i++) {
            variant = question.getVariants().get(i);
            if ( variant.hasDisplayText() ) {
                this.writer.write(format("       %d : %s", i, variant.getDisplay()));
            } else {
                this.writer.write(format("       %d : %s", i, variant.get()));
            }
        }
        printInDialogInviteLine("choose");
    }

    @Override
    public void report(String string) throws IOException {        
        if ( this.operationInProcess.get() ) {
            this.writer.write(format("     > %s", string));
            this.writer.newLine();
            this.writer.flush();
        } else {
            this.writer.write(format("Beam[3] > %s", string));
            this.writer.newLine();
            this.writer.write("Beam[3] > ");
            this.writer.flush();
        }
    }

    @Override
    public void reportMessage(TextMessage message) throws IOException {
        for (String s : message.getText()) {
            this.writer.write(format("     > %s", s));
        }
        this.writer.newLine();
        this.writer.write("Beam[4] > ");
        this.writer.flush();
    }

    @Override
    public void close() throws IOException {
        System.out.println("closing...");
        exitSystemConsole();
        System.out.println("closed.");
    }

    @Override
    public void acceptInitiator(Initiator initiator) throws IOException {
        System.out.println("ConsoleController accepting initiator: " + initiator.getId());
        this.initiator = initiator;
        this.startConsoleRunner();
    }

    @Override
    public String getName() {
        return this.consoleName;
    }
}
