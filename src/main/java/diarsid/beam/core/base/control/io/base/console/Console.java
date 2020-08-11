/*
 * To change this license header, answerOf License Headers in Project Properties.
 * To change this template file, answerOf Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.console;

import java.io.IOException;
import java.util.List;

import diarsid.beam.core.base.analyze.variantsweight.Variant;
import diarsid.beam.core.base.analyze.variantsweight.Variants;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.HelpInfo;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.util.MutableString;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import static diarsid.beam.core.base.control.io.base.interaction.Answers.answerOfVariant;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.helpRequestAnswer;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.helpRequestAnswerFor;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.rejectedAnswer;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.variantsDontContainSatisfiableAnswer;
import static diarsid.beam.core.base.control.io.base.interaction.Choice.NOT_MADE;
import static diarsid.beam.core.base.control.io.base.interaction.Choice.choiceOfPattern;
import static diarsid.beam.core.base.control.io.base.interaction.Help.isHelpRequest;
import static diarsid.beam.core.base.control.io.base.interaction.UserReaction.isNo;
import static diarsid.beam.core.base.control.io.base.interaction.UserReaction.isRejection;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.findUnacceptableInText;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.textIsNotAcceptable;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoIndependently;
import static diarsid.beam.core.base.util.ConcurrencyUtil.awaitDo;
import static diarsid.beam.core.base.util.MutableString.emptyMutableString;
import static diarsid.beam.core.base.util.StringNumberUtils.isNumeric;
import static diarsid.support.strings.StringUtils.normalizeSpaces;

/**
 *
 * @author Diarsid
 */
public class Console implements OuterIoEngine { 

    private final ConsolePlatformOperator consoleOperator;
    
    Console(ConsolePlatformOperator engine) {
        this.consoleOperator = engine;        
    }
    
    public static Console buildConsoleUsing(ConsolePlatform consolePlatform) {
        return new Console(new ConsolePlatformOperator(consolePlatform));
    }

    @Override
    public OuterIoEngineType type() {
        return this.consoleOperator.type();
    }
    
    public void launch() {
        asyncDoIndependently(
                this.consoleOperator.name(), 
                () -> this.go());
    }
    
    private void go() {
        MutableString mutableCommand = emptyMutableString();   
        Runnable mutableConsoleExecution = () -> {
            this.consoleOperator.blockingExecute(mutableCommand.get());
        };
        
        while ( this.consoleOperator.isWorking() ) {
            mutableCommand.muteTo(this.consoleOperator.readyAndWaitForLine());             
            if ( mutableCommand.isNotEmpty() ) {
                this.consoleOperator.interactionBegins();
                awaitDo(mutableConsoleExecution);       
                this.consoleOperator.interactionEnds();
            }  
        }
    }
    
    @Override
    public Choice resolve(String yesOrNoQuestion)  {
        this.consoleOperator.printYesNoQuestion(yesOrNoQuestion);
        return choiceOfPattern(this.consoleOperator.read());
    }

    @Override
    public Answer resolve(VariantsQuestion question) {
        this.consoleOperator.print(question);
        return this.askForAnswer(question);
    }

    private Answer askForAnswer(VariantsQuestion question) {
        boolean notResolved = true;
        String line;
        int chosenVariantIndex;
        Answer answer = variantsDontContainSatisfiableAnswer();
        while ( notResolved ) {
            line = this.consoleOperator.read();            
            if ( isNumeric(line) ) {
                chosenVariantIndex = parseInt(line);
                if ( question.isChoiceInVariantsNaturalRange(chosenVariantIndex) ) {
                    notResolved = false;
                    answer = question.answerWith(chosenVariantIndex);
                } else {
                    this.consoleOperator.print("not in variants range.");
                    this.consoleOperator.printInvite("choose");
                }
            } else {
                if ( isRejection(line) ) {
                    notResolved = false;
                    answer = rejectedAnswer();
                } else if ( isNo(line) ) {
                    notResolved = false;
                    answer = variantsDontContainSatisfiableAnswer();
                } else if ( isHelpRequest(line) ) { 
                    notResolved = false;
                    answer = helpRequestAnswer();
                } else {
                    answer = question.ifPartOfAnyVariant(line);
                    if ( answer.isGiven() ) {
                        notResolved = false;
                    } else {
                        this.consoleOperator.print(format("cannot choose by '%s'.", line));
                        this.consoleOperator.printInvite("choose");
                    }                    
                }
            }
        }
        return answer;
    }

    @Override
    public Answer resolve(Variants variants) {        
        String line;
        Answer answer = variantsDontContainSatisfiableAnswer();
        Choice choice = NOT_MADE;
        Choice previousChoice = NOT_MADE;
        int chosenVariantIndex;
        List<Variant> similarVariants;
        Variant currentVariant;
        
        variantsChoosing: while ( variants.next() ) {         
            answer = variantsDontContainSatisfiableAnswer();   
            if ( variants.currentIsMuchBetterThanNext() ) {
                currentVariant = variants.current();
                this.consoleOperator.printYesNoQuestion(currentVariant.nameOrValue());
                previousChoice = choice;
                choice = choiceOfPattern(this.consoleOperator.read());
                switch ( choice ) {
                    case POSITIVE : {
                        return answerOfVariant(currentVariant);
                    }
                    case NEGATIVE : {
//                        variants.removeHavingSameStartAs(currentVariant);
                        continue variantsChoosing;
                    }
                    case REJECT : {
                        return rejectedAnswer();
                    }
                    case NOT_MADE : {
                        return variantsDontContainSatisfiableAnswer();
                    }
                    case HELP_REQUEST : {
                        return helpRequestAnswerFor(variants.currentTraverseIndex());
                    }
                    default : {
                        continue variantsChoosing;
                    }
                }
            } else {
                similarVariants = variants.nextSimilarVariants();
                this.consoleOperator.print(similarVariants);
                similarVariantsChoosing: while ( true ) {
                    line = this.consoleOperator.read();
                    if ( isNumeric(line) ) {
                        chosenVariantIndex = parseInt(line);
                        if ( variants.isChoiceInSimilarVariantsNaturalRange(chosenVariantIndex) ) {
                            return variants.answerWith(chosenVariantIndex);
                        } else {
                            this.consoleOperator.print("not in variants range.");
                            this.consoleOperator.printInvite("choose");
                            continue similarVariantsChoosing;
                        }
                    } else if ( isHelpRequest(line) ) {
                        return helpRequestAnswerFor(variants.currentTraverseIndex());
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
                                this.consoleOperator.print(format("cannot choose by '%s'.", line));
                                this.consoleOperator.printInvite("choose");
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
    public String askInput(String inputRequest) {        
        String input = "";
        boolean answerIsNotGiven = true;
        while ( answerIsNotGiven ) {
            this.consoleOperator.printInvite(inputRequest);
            input = normalizeSpaces(this.consoleOperator.read());
            if ( isRejection(input) ) {
                input = "";
                answerIsNotGiven = false;
            } else if ( textIsNotAcceptable(input) ) {
                this.consoleOperator.print(
                        format("character %s is not allowed.", findUnacceptableInText(input)));
            } else {
                answerIsNotGiven = false;
            }
        }
        return input;
    }

    @Override
    public void report(String report) {        
        this.consoleOperator.print(report);
    }

    @Override
    public void report(Message message) {
        this.consoleOperator.print(message);
    }

    @Override
    public void report(HelpInfo help) {
        this.consoleOperator.print(help);
    }

    @Override
    public void close() {
        this.consoleOperator.print("closing...");        
        asyncDo(() -> {
            try {
                this.consoleOperator.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void accept(Initiator initiator) {
        this.consoleOperator.acceptInitiator(initiator);
    }

    @Override
    public String name() {
        return this.consoleOperator.name();
    }

    @Override
    public boolean isActiveWhenClosed() throws IOException {
        return this.consoleOperator.isActiveWhenClosed();
    }
}
