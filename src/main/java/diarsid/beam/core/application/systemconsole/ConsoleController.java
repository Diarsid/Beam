/*
 * To change this license header, answerOf License Headers in Project Properties.
 * To change this template file, answerOf Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.systemconsole;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.exceptions.WorkflowBrokenException;
import diarsid.beam.core.base.rmi.RemoteCoreAccessEndpoint;
import diarsid.beam.core.base.util.StringHolder;
import diarsid.beam.core.domain.patternsanalyze.WeightedVariant;
import diarsid.beam.core.domain.patternsanalyze.WeightedVariants;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Objects.isNull;

import static diarsid.beam.core.application.systemconsole.SystemConsole.exitSystemConsole;
import static diarsid.beam.core.application.systemconsole.SystemConsole.getPassport;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.answerOfVariant;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.rejectedAnswer;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.variantsDontContainSatisfiableAnswer;
import static diarsid.beam.core.base.control.io.base.interaction.Choice.choiceOfPattern;
import static diarsid.beam.core.base.control.io.base.interaction.UserReaction.isNo;
import static diarsid.beam.core.base.control.io.base.interaction.UserReaction.isRejection;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.findUnacceptableInText;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.textIsNotAcceptable;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoIndependently;
import static diarsid.beam.core.base.util.ConcurrencyUtil.awaitDo;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.StringNumberUtils.isNumeric;
import static diarsid.beam.core.base.util.StringUtils.normalizeSpaces;

/**
 *
 * @author Diarsid
 */
public class ConsoleController implements OuterIoEngine {  // + Runnable
    
    private static RemoteCoreAccessEndpoint remoteAccess;  // need to be more abstract, save remote-in-static in another place
        
    private final ConsolePrinter printer;         //  
    private final ConsoleReader reader;           //  move to AbstractConsoleEnginge 
    private final AtomicBoolean isInDialogMode;   // 
    private final AtomicBoolean isWorking;        // 
    private Initiator initiator;                  // 
    
    // TODO rework it!
    public ConsoleController(ConsolePrinter printer, ConsoleReader reader) {
        this.printer = printer;
        this.reader = reader;        
        this.isInDialogMode = new AtomicBoolean(false);
        this.isWorking = new AtomicBoolean(true);
    }

    public void setRemoteAccess(RemoteCoreAccessEndpoint access) {
        remoteAccess = access;
    }
    
    // @Override
    // ..... run() {
    private void startConsoleRunner() {
        if ( isNull(this.initiator) ) {
            throw new StartupFailedException(
                    "Attempt to start console while initiator has not been set.");
        }
        asyncDoIndependently(() -> {   // move to upper abstraction layer due to this is Runnable.
            StringHolder command = new StringHolder();   
            try {
                while ( this.isWorking.get() ) {
                    this.printer.printReadyForNewCommandLine();
                    command.set(this.reader.readLine()); 
                    this.isInDialogMode.set(true);   // -> .interactionBegins();
                    if ( command.isNotEmpty() ) {
                        awaitDo(() -> {
                            try {
                                remoteAccess.executeCommand(this.initiator, command.get());
                            } catch (RemoteException ex) {
                                throw new WorkflowBrokenException(ex);
                            }
                        });                        
                    }                    
                    this.isInDialogMode.set(false);  // -> .interactionEnds();
                }
            } catch (IOException ex) {
                logError(this.getClass(), ex);
            } 
        });
    }
    
    @Override
    public Choice resolve(String yesOrNoQuestion) throws IOException {
        this.printer.printYesNoQuestion(yesOrNoQuestion);
        return choiceOfPattern(this.reader.readLine());
    }

    @Override
    public Answer resolve(VariantsQuestion question) throws IOException {
        this.printer.printQuestionAndVariants(question);
        return this.askForAnswer(question);
    }

    private Answer askForAnswer(VariantsQuestion question) 
            throws IOException, NumberFormatException {
        boolean notResolved = true;
        String line;
        int chosenVariantIndex;
        Answer answer = variantsDontContainSatisfiableAnswer();
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
                if ( isRejection(line) ) {
                    notResolved = false;
                    answer = rejectedAnswer();
                } else if ( isNo(line) ) {
                    notResolved = false;
                    answer = variantsDontContainSatisfiableAnswer();
                } else {
                    answer = question.ifPartOfAnyVariant(line);
                    if ( answer.isGiven() ) {
                        notResolved = false;
                    } else {
                        this.printer.printInDialogInviteLine("choose");
                    }                    
                }
            }
        }
        return answer;
    }

    @Override
    public Answer resolve(WeightedVariants variants) throws IOException {
        if ( variants.hasOne() ) {
            return variants.singleAnswer();
        }
        
        String line;
        Answer answer = variantsDontContainSatisfiableAnswer();
        Choice choice;
        int chosenVariantIndex;
        List<WeightedVariant> similarVariants;
        
        variantsChoosing: while ( variants.next() ) {         
            answer = variantsDontContainSatisfiableAnswer();   
            if ( variants.currentIsMuchBetterThanNext() ) {
                this.printer.printYesNoQuestion(variants.current().bestText());
                choice = choiceOfPattern(this.reader.readLine());
                switch ( choice ) {
                    case POSTIVE : {
                        return answerOfVariant(variants.current());
                    }
                    case NEGATIVE : {
                        continue variantsChoosing;
                    }
                    case REJECT : {
                        return rejectedAnswer();
                    }
                    case NOT_MADE : {
                        return variantsDontContainSatisfiableAnswer();
                    }    
                    default : {
                        continue variantsChoosing;
                    }
                }
            } else {
                similarVariants = variants.nextSimilarVariants();
                this.printer.printInDialogWeightedVariants(similarVariants);
                similarVariantsChoosing: while ( true ) {
                    line = this.reader.readLine();
                    if ( isNumeric(line) ) {
                        chosenVariantIndex = parseInt(line);
                        if ( variants.isChoiceInSimilarVariantsNaturalRange(chosenVariantIndex) ) {
                            return variants.answerWith(chosenVariantIndex);
                        } else {
                            this.printer.printInDialogReportLine("not in variants range.");
                            this.printer.printInDialogInviteLine("choose");
                            continue similarVariantsChoosing;
                        }
                    } else {
                        if ( isNo(line) ) {
                            continue variantsChoosing;
                        } else if ( isRejection(line) ) {
                            return rejectedAnswer();
                        } else {
                            answer = variants.ifPartOfAnySimilarVariant(line);
                            if ( answer.isGiven() ) {
                                return answer;
                            } else {
                                this.printer.printInDialogInviteLine("choose");
                                continue similarVariantsChoosing;
                            }                            
                        }
                    }
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
            input = normalizeSpaces(this.reader.readLine());
            if ( isRejection(input) ) {
                input = "";
                answerIsNotGiven = false;
            } else if ( textIsNotAcceptable(input) ) {
                this.printer.printInDialogReportLine(
                        format("character %s is not allowed.", findUnacceptableInText(input)));
            } else {
                answerIsNotGiven = false;
            }
        }
        return input;
    }

    @Override
    public void report(String report) throws IOException {        
        if ( this.isInDialogMode.get() ) {   // move inDialog logic to AbstractConsoleEngine
            this.printer.printInDialogReportLine(report);
        } else {
            this.printer.printNonDialogReportLine(report);
        }
    }

    @Override
    public void report(Message message) throws IOException {
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
        exitSystemConsole();  // move to AbstractConsoleEngine
    }

    @Override
    public void accept(Initiator initiator) throws IOException {
        this.initiator = initiator;
        getPassport().setInitiatorId(initiator.identity());   // move to another abstraction layer
        this.startConsoleRunner();  // invert console controller startup flow to higher abstraction layer
    }

    @Override
    public String getName() {
        return getPassport().getName();   // move to another abstraction layer
    }
}
