/*
 * To change this license header, answerOf License Headers in Project Properties.
 * To change this template file, answerOf Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.console;

import java.util.List;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariant;
import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.OuterIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.HelpInfo;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.util.StringHolder;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import static diarsid.beam.core.base.control.io.base.interaction.Answers.answerOfVariant;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.helpRequestAnswer;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.rejectedAnswer;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.variantsDontContainSatisfiableAnswer;
import static diarsid.beam.core.base.control.io.base.interaction.Choice.choiceOfPattern;
import static diarsid.beam.core.base.control.io.base.interaction.Help.isHelpRequest;
import static diarsid.beam.core.base.control.io.base.interaction.UserReaction.isNo;
import static diarsid.beam.core.base.control.io.base.interaction.UserReaction.isRejection;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.findUnacceptableInText;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.textIsNotAcceptable;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.ConcurrencyUtil.awaitDo;
import static diarsid.beam.core.base.util.StringHolder.empty;
import static diarsid.beam.core.base.util.StringNumberUtils.isNumeric;
import static diarsid.beam.core.base.util.StringUtils.normalizeSpaces;

/**
 *
 * @author Diarsid
 */
public class ConsoleController 
        implements 
                OuterIoEngine, 
                Runnable { 

    private final ConsoleEngine engine;
    
    ConsoleController(ConsoleEngine engine) {
        this.engine = engine;        
    }
    
    @Override
    public void run() {
        StringHolder command = empty();   
        while ( this.engine.isWorking() ) {
            command.set(this.engine.readyAndWaitForLine()); 
            this.engine.interactionBegins();
            if ( command.isNotEmpty() ) {
                awaitDo(() -> {
                    this.engine.execute(command.get());
                });                        
            }                    
            this.engine.interactionEnds();
        }
    }
    
    @Override
    public Choice resolve(String yesOrNoQuestion)  {
        this.engine.printYesNoQuestion(yesOrNoQuestion);
        return choiceOfPattern(this.engine.read());
    }

    @Override
    public Answer resolve(VariantsQuestion question) {
        this.engine.print(question);
        return this.askForAnswer(question);
    }

    private Answer askForAnswer(VariantsQuestion question) {
        boolean notResolved = true;
        String line;
        int chosenVariantIndex;
        Answer answer = variantsDontContainSatisfiableAnswer();
        while ( notResolved ) {
            line = this.engine.read();            
            if ( isNumeric(line) ) {
                chosenVariantIndex = parseInt(line);
                if ( question.isChoiceInVariantsNaturalRange(chosenVariantIndex) ) {
                    notResolved = false;
                    answer = question.answerWith(chosenVariantIndex);
                } else {
                    this.engine.print("not in variants range.");
                    this.engine.printInvite("choose");
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
                        this.engine.print(format("cannot choose by '%s'.", line));
                        this.engine.printInvite("choose");
                    }                    
                }
            }
        }
        return answer;
    }

    @Override
    public Answer resolve(WeightedVariants variants) {        
        String line;
        Answer answer = variantsDontContainSatisfiableAnswer();
        Choice choice;
        int chosenVariantIndex;
        List<WeightedVariant> similarVariants;
        
        variantsChoosing: while ( variants.next() ) {         
            answer = variantsDontContainSatisfiableAnswer();   
            if ( variants.currentIsMuchBetterThanNext() ) {
                this.engine.printYesNoQuestion(variants.current().bestText());
                choice = choiceOfPattern(this.engine.read());
                switch ( choice ) {
                    case POSITIVE : {
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
                    case HELP_REQUEST : {
                        return helpRequestAnswer();
                    }
                    default : {
                        continue variantsChoosing;
                    }
                }
            } else {
                similarVariants = variants.nextSimilarVariants();
                this.engine.print(similarVariants);
                similarVariantsChoosing: while ( true ) {
                    line = this.engine.read();
                    if ( isNumeric(line) ) {
                        chosenVariantIndex = parseInt(line);
                        if ( variants.isChoiceInSimilarVariantsNaturalRange(chosenVariantIndex) ) {
                            return variants.answerWith(chosenVariantIndex);
                        } else {
                            this.engine.print("not in variants range.");
                            this.engine.printInvite("choose");
                            continue similarVariantsChoosing;
                        }
                    } else if ( isHelpRequest(line) ) {
                        return helpRequestAnswer();
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
                                this.engine.print(format("cannot choose by '%s'.", line));
                                this.engine.printInvite("choose");
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
    public String askForInput(String inputRequest) {        
        String input = "";
        boolean answerIsNotGiven = true;
        while ( answerIsNotGiven ) {
            this.engine.printInvite(inputRequest);
            input = normalizeSpaces(this.engine.read());
            if ( isRejection(input) ) {
                input = "";
                answerIsNotGiven = false;
            } else if ( textIsNotAcceptable(input) ) {
                this.engine.print(
                        format("character %s is not allowed.", findUnacceptableInText(input)));
            } else {
                answerIsNotGiven = false;
            }
        }
        return input;
    }

    @Override
    public void report(String report) {        
        this.engine.print(report);
    }

    @Override
    public void report(Message message) {
        this.engine.print(message);
    }

    @Override
    public void report(HelpInfo help) {
        this.engine.print(help);
    }

    @Override
    public void close() {
        this.engine.print("closing...");        
        asyncDo(() -> this.engine.stop());
    }

    @Override
    public void accept(Initiator initiator) {
        this.engine.acceptInitiator(initiator);
    }

    @Override
    public String name() {
        return this.engine.name();
    }
}
