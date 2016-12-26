/*
 * To change this license header, answerOf License Headers in Project Properties.
 * To change this template file, answerOf Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import diarsid.beam.core.control.io.base.Answer;
import diarsid.beam.core.control.io.base.Choice;
import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.OuterIoEngine;
import diarsid.beam.core.control.io.base.Question;
import diarsid.beam.core.control.io.base.TextMessage;
import diarsid.beam.core.rmi.RemoteAccessEndpoint;

import static java.lang.Integer.parseInt;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.control.io.base.Answer.noAnswer;
import static diarsid.beam.core.control.io.base.Choice.choiceOfPattern;
import static diarsid.beam.core.systemconsole.SystemConsole.exitSystemConsole;
import static diarsid.beam.core.util.StringUtils.isNumeric;

/**
 *
 * @author Diarsid
 */
public class ConsoleController implements OuterIoEngine {
        
    private final ConsolePrinter printer;
    private final ConsoleReader reader;     
    private final AtomicBoolean isInDialogMode;
    private Initiator initiator;
    private RemoteAccessEndpoint remoteAccess;
    private String consoleName;
    
    public ConsoleController(ConsolePrinter printer, ConsoleReader reader) {
        this.printer = printer;
        this.reader = reader;        
        this.isInDialogMode = new AtomicBoolean(false);
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
                    this.printer.printReadyForNewCommandLine();
                    command = this.reader.readLine(); 
                    this.isInDialogMode.set(true);
                    if ( ! command.isEmpty() && nonNull(this.initiator) ) {
                        this.remoteAccess.executeCommand(this.initiator, command);
                    }                    
                    this.isInDialogMode.set(false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } 
            }
        }).start();
    }
    
    @Override
    public Choice resolveYesOrNo(String yesOrNoQuestion) throws IOException {
        this.printer.printInDialogReportLine(yesOrNoQuestion);
        this.printer.printInDialogInviteLine("yes / no");
        return choiceOfPattern(this.reader.readLine());
    }

    @Override
    public Answer resolveQuestion(Question question) throws IOException {
        this.printer.printQuestionAndVariants(question);
        return this.askForAnswer(question);
    }

    private Answer askForAnswer(Question question) throws IOException, NumberFormatException {
        boolean notResolved = true;
        String line = "";
        int chosenVariant = -1;
        Answer answer = noAnswer();
        while ( notResolved ) {
            line = this.reader.readLine();            
            if ( isNumeric(line) ) {
                chosenVariant = parseInt(line);
                if ( question.isChoiceInVariantsNaturalRange(chosenVariant) ) {
                    notResolved = false;
                    answer = question.answerWith(chosenVariant);
                } else {
                    this.printer.printInDialogReportLine("not in variants range.");
                    this.printer.printInDialogInviteLine("choose");
                }
            } else {
                this.printer.printInDialogInviteLine("choose");
            }
        }
        return answer;
    }

    @Override
    public void report(String report) throws IOException {        
        if ( this.isInDialogMode.get() ) {
            this.printer.printInDialogReportLine(report);
        } else {
            this.printer.printNonDialogReport(report);
        }
    }

    @Override
    public void reportMessage(TextMessage message) throws IOException {
        if ( this.isInDialogMode.get() ) {
            this.printer.printInDialogMultilineReport(message);
        } else {
            this.printer.printNonDialogMultilineReport(message);
        }        
    }

    @Override
    public void close() throws IOException {
        this.printer.printInDialogReportLine("closing...");
        exitSystemConsole();
    }

    @Override
    public void acceptInitiator(Initiator initiator) throws IOException {
        this.initiator = initiator;
        this.startConsoleRunner();
    }

    @Override
    public String getName() {
        return this.consoleName;
    }
}
