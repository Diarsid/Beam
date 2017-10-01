/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import diarsid.beam.core.base.analyze.variantsweight.WeightedVariants;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Choice;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.base.interaction.HelpKey;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.Variant;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import static diarsid.beam.core.base.control.io.base.interaction.Answers.rejectedAnswer;
import static diarsid.beam.core.base.control.io.base.interaction.Answers.variantsDontContainSatisfiableAnswer;
import static diarsid.beam.core.base.control.io.base.interaction.Choice.REJECT;
import static diarsid.beam.core.base.control.io.base.interaction.Choice.choiceOfPattern;
import static diarsid.beam.core.base.control.io.base.interaction.UserReaction.isRejection;
import static diarsid.beam.core.base.util.StringNumberUtils.isNumeric;
import static diarsid.beam.core.base.util.StringUtils.normalizeSpaces;

/**
 *
 * @author Diarsid
 */
public class InnerIoEngineForManualTests implements InnerIoEngine {
    
    private final BufferedReader reader;
    
    public InnerIoEngineForManualTests(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public String askInput(Initiator initiator, String inputQuestion, Help help) {
        try {
            String input = "";
            boolean answerIsNotGiven = true;
            while ( answerIsNotGiven ) {                
                System.out.print("fake io > " + inputQuestion + " : ");
                input = normalizeSpaces(this.reader.readLine());
                if ( isRejection(input) ) {
                    input = "";
                    answerIsNotGiven = false;
                } else {
                    answerIsNotGiven = false;
                }               
            }
        return input;
        } catch (IOException ex) {
            return "";
        }
    }

    @Override
    public Choice ask(Initiator initiator, String yesOrNoQuestion, Help help) {
        try {
            System.out.println("fake io > " + yesOrNoQuestion);
            System.out.print("fake io > ");
            return choiceOfPattern(reader.readLine());
        } catch (IOException ex) {
            return REJECT;
        }
    }

    @Override
    public Answer ask(Initiator initiator, VariantsQuestion question, Help help) {
        try {
            Variant variant;
            System.out.println(format("fake io > %s", question.getQuestion()));
            for (int i = 0; i < question.getVariants().size(); i++) {
                variant = question.getVariants().get(i);
                if ( variant.hasDisplayText() ) {
                    System.out.println(format("fake io >     %d : %s", i + 1, variant.displayText()));
                } else {
                    System.out.println(format("fake io >     %d : %s", i + 1, variant.text()));
                }
            }
            System.out.print("fake io > choose ");
            boolean notResolved = true;
            String line = "";
            int chosenVariantIndex = -1;
            Answer answer = variantsDontContainSatisfiableAnswer();
            while ( notResolved ) {
                line = reader.readLine();                
                if ( isNumeric(line) ) {
                    chosenVariantIndex = parseInt(line);
                    if ( question.isChoiceInVariantsNaturalRange(chosenVariantIndex) ) {
                        notResolved = false;
                        answer = question.answerWith(chosenVariantIndex);
                    } else {
                        System.out.println("fake io > not in variants range.");
                        System.out.print("fake io > choose ");
                    }
                } else {
                    answer = question.ifPartOfAnyVariant(line);
                    if ( answer.isGiven() ) {
                        notResolved = false;
                    } else if ( isRejection(line) ) {
                        notResolved = false;
                        answer = rejectedAnswer();
                    } else {
                        System.out.print("choose ");
                    }
                }
            }
            return answer;
        } catch (Exception e) {
            return rejectedAnswer();
        } 
    }

    @Override
    public void report(Initiator initiator, String string) {
        System.out.println("fake io > " + string);
    }

    @Override
    public void reportAndExitLater(Initiator initiator, String string) {
        System.out.println("fake io > " + string);
        System.out.println("[exit]");
    }

    @Override
    public void reportMessage(Initiator initiator, Message message) {
        for (String s : message.toText()) {
            System.out.println("fake io > " + s);
        }
    }

    @Override
    public void reportMessageAndExitLater(Initiator initiator, Message message) {
        for (String s : message.toText()) {
            System.out.println("fake io > " + s);
        }
        System.out.println("[exit]");
    }

    @Override
    public Answer chooseInWeightedVariants(Initiator initiator, WeightedVariants variants, Help help) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HelpKey addToHelpContext(String... help) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HelpKey addToHelpContext(List<String> help) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
