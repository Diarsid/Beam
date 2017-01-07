/*
 * To change this license header, answerOf License Headers in Project Properties.
 * To change this template file, answerOf Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.systemconsole;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicBoolean;

import diarsid.beam.core.control.io.base.Choice;
import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.Message;
import diarsid.beam.core.control.io.base.OuterIoEngine;
import diarsid.beam.core.control.io.base.VariantAnswer;
import diarsid.beam.core.control.io.base.VariantsQuestion;
import diarsid.beam.core.exceptions.WorkflowBrokenException;
import diarsid.beam.core.rmi.RemoteCoreAccessEndpoint;
import diarsid.beam.core.util.StringHolder;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Objects.isNull;

import static diarsid.beam.core.control.io.base.Choice.choiceOfPattern;
import static diarsid.beam.core.control.io.base.UserReaction.isRejection;
import static diarsid.beam.core.control.io.base.VariantAnswer.noAnswerFromVariants;
import static diarsid.beam.core.control.io.interpreter.ControlKeys.findUnacceptableIn;
import static diarsid.beam.core.control.io.interpreter.ControlKeys.wordIsNotAcceptable;
import static diarsid.beam.core.systemconsole.SystemConsole.exitSystemConsole;
import static diarsid.beam.core.systemconsole.SystemConsole.getPassport;
import static diarsid.beam.core.util.ConcurrencyUtil.waitAndDo;
import static diarsid.beam.core.util.StringNumberUtils.isNumeric;
import static diarsid.beam.core.util.StringUtils.normalize;

/**
 *
 * @author Diarsid
 */
public class ConsoleController implements OuterIoEngine {
        
    private final ConsolePrinter printer;
    private final ConsoleReader reader;     
    private final AtomicBoolean isInDialogMode;
    private final AtomicBoolean isWorking;
    private Initiator initiator;
    private RemoteCoreAccessEndpoint remoteAccess;    
    
    public ConsoleController(ConsolePrinter printer, ConsoleReader reader) {
        this.printer = printer;
        this.reader = reader;        
        this.isInDialogMode = new AtomicBoolean(false);
        this.isWorking = new AtomicBoolean(true);
    }

    public void setRemoteAccess(RemoteCoreAccessEndpoint remoteAccess) {
        this.remoteAccess = remoteAccess;
    }
    
    private void startConsoleRunner() {
        if ( isNull(this.initiator) ) {
            throw new StartupFailedException(
                    "Attempt to start console while initiator has not been set.");
        }
        new Thread(() -> {
            StringHolder command = new StringHolder();   
            try {
                while ( this.isWorking.get() ) {
                    this.printer.printReadyForNewCommandLine();
                    command.set(this.reader.readLine()); 
                    this.isInDialogMode.set(true);
                    if ( command.isNotEmpty() ) {
                        waitAndDo(() -> {
                            try {
                                this.remoteAccess.executeCommand(this.initiator, command.get());
                            } catch (RemoteException ex) {
                                throw new WorkflowBrokenException(ex);
                            }
                        });                        
                    }                    
                    this.isInDialogMode.set(false);
                }
            } catch (IOException ex) {
                throw new WorkflowBrokenException(ex);
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
    public VariantAnswer resolveQuestion(VariantsQuestion question) throws IOException {
        this.printer.printQuestionAndVariants(question);
        return this.askForAnswer(question);
    }

    private VariantAnswer askForAnswer(VariantsQuestion question) 
            throws IOException, NumberFormatException {
        boolean notResolved = true;
        String line = "";
        int chosenVariantIndex = -1;
        VariantAnswer answer = noAnswerFromVariants();
        while ( notResolved ) {
            line = this.reader.readLine();            
            if ( isNumeric(line) ) {
                chosenVariantIndex = parseInt(line);
                if ( question.isChoiceInVariantsNaturalRange(chosenVariantIndex) ) {
                    notResolved = false;
                    answer = question.answerWith(chosenVariantIndex);
                } else {
                    this.printer.printInDialogReportLine("not in variants range.");
                    this.printer.printInDialogInviteLine("choose");
                }
            } else {
                answer = question.ifPartOfAnyVariant(line);
                if ( answer.isPresent() ) {
                    notResolved = false;
                } else if ( isRejection(line) ) {
                    notResolved = false;
                    answer = noAnswerFromVariants();
                } else {
                    this.printer.printInDialogInviteLine("choose");
                }
            }
        }
        return answer;
    }
    
    @Override
    public String askForInput(String inputRequest) throws IOException {        
        String input = "";
        boolean answerIsNotGiven = true;
        while ( answerIsNotGiven ) {
            this.printer.printInDialogInviteLine(inputRequest);
            input = normalize(this.reader.readLine());
            if ( isRejection(input) ) {
                input = "";
                answerIsNotGiven = false;
            } 
            if ( wordIsNotAcceptable(input) ) {
                this.printer.printInDialogReportLine(
                        format("character %s is not allowed.", findUnacceptableIn(input)));
            }
        }
        return input;
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
    public void reportMessage(Message message) throws IOException {
        if ( this.isInDialogMode.get() ) {
            this.printer.printInDialogMultilineReport(message);
        } else {
            this.printer.printNonDialogMultilineReport(message);
        }        
    }

    @Override
    public void close() throws IOException {
        this.isWorking.set(false);
        this.printer.printInDialogReportLine("closing...");
        exitSystemConsole();
    }

    @Override
    public void acceptInitiator(Initiator initiator) throws IOException {
        this.initiator = initiator;
        getPassport().setInitiatorId(initiator.getId());
        this.startConsoleRunner();
    }

    @Override
    public String getName() {
        return getPassport().getName();
    }
}
